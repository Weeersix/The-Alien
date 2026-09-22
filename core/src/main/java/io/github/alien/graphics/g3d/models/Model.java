package io.github.alien.graphics.g3d.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import io.github.alien.Constants;
import io.github.alien.utils.FileUtils;

import java.util.List;
import java.util.Map;

public class Model {
    public String name;
    public Vector3 position;
    private float multiplier = 1;
    protected boolean drawParts = true;
    protected boolean disposed = false;
    public List<Map<String, Object>> animatedParts;

    public Vector3 axis;
    public float minAngle, maxAngle, angle, rotationSpeed;
    public boolean loopedRotation = false;
    private int direction = 1;

    public float modelSize;
    public int textureSize;

    public ModelLoader loader;
    public ModelInstance model;

    public Model(String name, Vector3 position, float modelSize, int textureSize){
        this.modelSize = modelSize;
        this.textureSize = textureSize;

        load(name, position);
    }

    protected void load(String name, Vector3 position) {
        loader = new ModelLoader();

        this.name = name;
        this.position = position;

        loader.setName(name);
        loader.setPostfix("-model");
        loader.setTextureSize(textureSize);
        loader.load();

        model = ModelRenders.modelRender(
                loader.hasModelSprite ?
                        FileUtils.find(name + loader.postfix, ".png") :
                        FileUtils.find(name, ".png"),
                null,
                loader.parts,
                loader.typedPositions,
                loader.customPositions,
                loader.partPositionType,
                drawParts,
                modelSize
        );

        if (position != null) {
            model.transform.translate(this.position);
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

    public void render(ModelBatch batch) {
        if(!disposed) {
            batch.render(model);

            rotate();
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
