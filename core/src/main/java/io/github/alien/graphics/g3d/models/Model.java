package io.github.alien.graphics.g3d.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import io.github.alien.Constants;
import io.github.alien.utils.FileUtils;

public class Model {
    public String name;
    public Vector3 position;
    private float multiplier = 1;
    protected boolean drawParts = true;
    protected boolean disposed = false;

    public Vector3 axis;
    public float angle;
    public float scaleSpeed;
    public boolean loopedRotation = false;

    public float modelSize;
    public int textureSize;

    public ModelLoader loader;
    public ModelInstance model;

    public Model(String name, Vector3 position, float modelSize, int textureSize){
        this.modelSize = modelSize;
        this.textureSize = textureSize;

        load(name, position);
    }

    protected void load(String name, Vector3 position){
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

        if(position != null){
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
    public Model setRotation(float angle, Vector3 axis, boolean looped){
        this.angle = angle;
        this.axis = axis;
        this.loopedRotation = looped;

        if(!looped) {
            model.transform.rotate(axis, angle);
        }

        return this;
    }
    protected void rotate(){
        model.transform.rotate(axis, angle * Gdx.graphics.getDeltaTime());
    }

    public void render(ModelBatch batch) {
        if(!disposed) {
            batch.render(model);

            if (loopedRotation) rotate();
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
