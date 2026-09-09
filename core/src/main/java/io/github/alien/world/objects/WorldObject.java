package io.github.alien.world.objects;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Null;
import io.github.alien.graphics.g3d.models.Model;

import java.lang.reflect.Constructor;
import java.util.*;

public class WorldObject {
    protected String name;
    protected Vector3 position;
    protected boolean canMove;
    protected float modelSize;
    protected int textureSize;
    protected int variationsCount;
    protected boolean haveVariants;
    public boolean deleted = false, hided = false;
    public List<Model> parts = new ArrayList<>();

    public static Map<Vector3, WorldObject> allObjects = new HashMap<>();
    public static List<Vector3> objectPositions = new ArrayList<>();

    protected Model model;

    public WorldObject(String name, float modelSize, int textureSize, @Null Integer variationsCount){
        load(name, null, modelSize, textureSize, variationsCount);
    }

    public WorldObject(String name, Vector3 position, float modelSize, int textureSize, @Null Integer variationsCount, List<Model> parts){
        load(name, position, modelSize, textureSize, variationsCount);

        this.parts = parts != null ? parts : new ArrayList<>();
        if(position != null) model.setPosition(position);

        allObjects.put(this.position, this);
        objectPositions.add(this.position);
    }

    protected void load(String name, @Null Vector3 position, float modelSize, int textureSize, @Null Integer variationsCount){
        this.position = position;
        this.modelSize = modelSize;
        this.textureSize = textureSize;
        this.variationsCount = variationsCount;
        haveVariants = variationsCount != -1;

        this.name = name;
        canMove = false;
        model = new Model(haveVariants ? getVariant(variationsCount) : this.name, position != null ? position : this.position, modelSize, textureSize);
    }

    public static Map<Vector3, WorldObject> getAllObjects(){
        return allObjects;
    }
    public static List<Vector3> getObjectPositions(){
        return objectPositions;
    }

    public WorldObject attachPart(List<Model> parts){
        this.parts.addAll(parts);
        return this;
    }

    public void set(ModelBatch batch){
        if(!hided) {
            model.render(batch);

            for (Model part : parts) {
                part.render(batch);
            }
        }
    }

    public void hide(){
        if(!hided) hided = true;
    }

    public void delete(){
        if(!deleted) {
            deleted = true;
        }
    }
    public void dispose(){
        if(deleted) {
            model.dispose();
        }
    }

    public void checkAround(){}

    public String getName(){
        return this.name;
    }

    public float getModelSize(){
        return this.modelSize;
    }

    public Vector3 getPosition(){
        return this.position;
    }

    protected String getVariant(int variationsCount){
        return getName() + "-" + new Random().nextInt(1, variationsCount + 1);
    }


    protected Constructor<?> constructor;
    protected List<Object> properties = new ArrayList<>();

    public void reload(Class<?> clazz, Object... newParameters) {
        Constructor<?> currentConstructor;
        for (int i = 0; i < clazz.getConstructors().length; i++) {
            currentConstructor = clazz.getConstructors()[i];

            if(currentConstructor.getParameterCount() > 4 &&
                    currentConstructor.getParameters()[1].getType() == Vector3.class &&
                    currentConstructor.getParameters()[5].getType() == List.class) {
                constructor = currentConstructor;

                properties.add(name);
                properties.add(modelSize);
                properties.add(textureSize);
                properties.add(variationsCount);
                properties.addAll(Arrays.asList(newParameters));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T createNew(Vector3 position) throws Exception {
        List<Model> newParts = new ArrayList<>();

        if(!this.parts.isEmpty()){
            for(Model part : parts){
                newParts = new ArrayList<>();

                newParts.add(
                        new Model(
                                part.name,
                                position,
                                part.modelSize,
                                part.textureSize
                        )
                                .setPosition(part.position)
                                .setRotation(part.angle, part.axis, part.loopedRotation)
                                .setMovements(part.movements, part.movementSpeed, part.loopedMovement)
                );
            }
        }

        properties.add(1, position);
        properties.add(5, newParts);
        T instance = (T) constructor.newInstance(properties.toArray());
        properties.remove(5);
        properties.remove(1);

        return instance;
    }


}
