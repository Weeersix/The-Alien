package io.github.alien.graphics.g3d.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import io.github.alien.Constants;
import io.github.alien.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class Model {
    public String name;
    public Vector3 position;
    private float multiplier = 1;
    protected boolean disposed = false;
    protected String modelAtlas;

    public Vector3 axis;
    public float minAngle, maxAngle, angle, rotationSpeed;
    public boolean loopedRotation = false;
    private int direction = 1;

    public float modelSize;
    public int textureSize;

    public List<Part<Model>> parts = new ArrayList<>();
    protected final ModelBuilder builder = new ModelBuilder();
    public ModelLoader loader;
    public ModelInstance model;

    public Model(String id, Vector3 position, float modelSize, int textureSize){
        this.modelSize = modelSize;
        this.textureSize = textureSize;

        load(id, position);
    }

    protected void load(String id, Vector3 position) {
        loader = new ModelLoader();
        builder.begin();

        this.name = id;
        this.position = position;

        loader.setName(id);
        loader.setPostfix("-model");
        loader.setTextureSize(textureSize);
        loader.load();

        modelAtlas = loader.hasModelSprite ?
                FileUtils.find(id + loader.postfix, ".png") :
                FileUtils.find(id, ".png");

        for (int i = 0; i < loader.model.size(); i++) {
            String partId = modelAtlas + "-" + i;
            float[] rect = loader.typedPositions.isEmpty() ?
                    loader.customPositions.get(i) :
                    Part.Shape.getRect(
                            loader.typedPositions.get(i),
                            loader.partPositionType.get(i),
                            modelSize
                    );

            this.parts.add(new Part<>(
                    partId,
                    this,
                    builder,
                    loader.model.get(i),
                    rect,
                    loader.axes.get(i),
                    loader.partRotations.get(i)
            ));
        }

        model = new ModelInstance(builder.end());

        if (position != null) {
            model.transform.translate(this.position);
        }
        model.transform.scale(modelSize, modelSize, modelSize);
    }

    public void setPartSettings(Part<?> part){
        part.attribute =
                VertexAttributes.Usage.Position
                        | VertexAttributes.Usage.Normal
                        | VertexAttributes.Usage.TextureCoordinates;
        part.material = new Material(
                part.id + "-material",
                TextureAttribute.createDiffuse(FileUtils.textureCache(modelAtlas)),
                new BlendingAttribute(GL20.GL_ONE_MINUS_SRC_ALPHA)
        );
        part.material.set(new FloatAttribute(FloatAttribute.AlphaTest, 0.1f));
        part.material.set(new IntAttribute(IntAttribute.CullFace, GL20.GL_NONE));
    }

    public void render(ModelBatch batch) {
        if(!disposed) {
            batch.render(model);

            for (Part<Model> part : parts){
                part.update();
            }

            if(loopedRotation) rotate();
        }
    }

    public Model asPart(){
        this.multiplier = Constants.PIXEL_SIZE;
        return this;
    }

    public Model setPosition(Vector3 position){
        this.position = new Vector3(position.x * multiplier, position.y * multiplier, position.z * multiplier);
        model.transform.translate(this.position);
        return this;
    }

    public Model setRotation(Vector3 axis, float angle, boolean looped){
        this.axis = axis;
        this.angle = angle;
        this.loopedRotation = looped;

        if(!looped) {
            model.transform.rotate(axis, angle);
        }

        return this;
    }
    public Model setRotation(Vector3 axis, float startAngle, float finalAngle, float rotationSpeed){
        this.axis = axis;
        this.minAngle = startAngle;
        this.maxAngle = finalAngle;
        this.rotationSpeed = rotationSpeed;

        model.transform.rotate(axis, minAngle);

        return this;
    }
    protected void rotate(){
        float rotSpeed;

        if(loopedRotation || rotationSpeed != 0) {
            if(angle != 0){
                rotSpeed = angle * Gdx.graphics.getDeltaTime();
                model.transform.rotate(axis, rotSpeed);
            } else {
                float currentAngle = model.transform.getRotation(new Quaternion()).getAngle();
                rotSpeed = rotationSpeed * direction * Gdx.graphics.getDeltaTime();

                if(currentAngle <= minAngle) direction = 1;
                if(currentAngle >= maxAngle) direction = -1;

                model.transform.rotate(axis, rotSpeed);
            }
        }
    }

    public void dispose(){
        if(!disposed) {
            model.model.dispose();
            loader.unload();
            disposed = true;
        }
    }
}
