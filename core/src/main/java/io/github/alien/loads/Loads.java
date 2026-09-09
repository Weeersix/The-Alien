package io.github.alien.loads;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.SerializationException;
import io.github.alien.Vars;
import io.github.alien.graphics.g3d.models.Model;
import io.github.alien.utils.FileUtils;
import io.github.alien.world.objects.SolidShape;
import io.github.alien.world.objects.EditableShape;
import io.github.alien.world.objects.WorldObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class Loads {
    private static final Map<String, SolidShape> SOLID_SHAPE_MODELS = new HashMap<>();
    private static final Map<String, EditableShape> EDITABLE_SHAPE_MODELS = new HashMap<>();

    private static final Map<Class<?>, Map<String, ?>> contentTypes = new HashMap<>();

    public static void loadContent() throws Exception {
        load("solid-shape-models", SOLID_SHAPE_MODELS, SolidShape.class);
        load("editable-shape-models", EDITABLE_SHAPE_MODELS, EditableShape.class);
    }

    @SuppressWarnings("unchecked")
    protected static <T> void load(String loadableContent, Map<String, T> objects, Class<T> objectClass) throws Exception{
        Constructor<T> constructor = (Constructor<T>) getConstructor(objectClass);
        JsonValue content = FileUtils.readValue(loadableContent, loadableContent);

        contentTypes.put(objectClass, objects);

        try {
            HashMap<String, Object> properties = new HashMap<>();

            for (int i = 0; i < content.size; i++) {
                List<Object> anotherProperties = new ArrayList<>();
                List<Object> args = new ArrayList<>();

                JsonValue object = content.get(i);

                getProperties(object, properties, anotherProperties);
                args.add(object.name);
                for (int j = 0; j < 3; j++) {
                    args.add(properties.get(Keys.values()[j].getKey()));
                }
                args.addAll(anotherProperties);

                T obj = constructor.newInstance(args.toArray());
                objects.put(object.name, obj);

                boolean hasParts = false;
                for (JsonValue v : object) {
                    if (v.name.contains(Keys.ADD_.name())) {
                        hasParts = true;
                        break;
                    }
                }

                if(hasParts && WorldObject.class.isAssignableFrom(objectClass)){
                    Method attachPart = objectClass.getMethod("attachPart", List.class);

                    attachPart.invoke(obj, getParts(object, (Float) properties.get(Keys.MODEL_SIZE_KEY.getKey()), (Integer) properties.get(Keys.TEXTURE_SIZE_KEY.getKey())));
                }
            }
        } catch (SerializationException ignored){}
    }

    private static <T> Constructor<?> getConstructor(Class<T> objectClass) throws Exception {
        Constructor<?> constructor;

        for (int i = 0; i < objectClass.getConstructors().length; i++) {
            constructor = objectClass.getConstructors()[i];

            if(constructor.getParameters()[1].getType() != Vector3.class) {
                return  constructor;
            }
        }

        return objectClass.getConstructor(String.class, float.class, int.class, Integer.class);
    }

    private static void getProperties(JsonValue object, HashMap<String, Object> properties, List<Object> anotherProperties){
        if(object.size != 0) {
            for (int i = 0; i < object.size; i++) {
                try {
                    switch (Keys.toKey(object.get(i).name)) {
                        case MODEL_SIZE_KEY: properties.put(Keys.MODEL_SIZE_KEY.getKey(), object.getFloat(i));break;
                        case TEXTURE_SIZE_KEY: properties.put(Keys.TEXTURE_SIZE_KEY.getKey(), object.getInt(i));break;
                        case VARIATIONS_COUNT_KEY: properties.put(Keys.VARIATIONS_COUNT_KEY.getKey(), object.getInt(i));break;
                    }
                    if(properties.size() < 3) {
                        if (!properties.containsKey(Keys.MODEL_SIZE_KEY.getKey())) properties.put(Keys.MODEL_SIZE_KEY.getKey(), 1f);
                        if (!properties.containsKey(Keys.TEXTURE_SIZE_KEY.getKey())) properties.put(Keys.TEXTURE_SIZE_KEY.getKey(), 16);
                        if (!properties.containsKey(Keys.VARIATIONS_COUNT_KEY.getKey())) properties.put(Keys.VARIATIONS_COUNT_KEY.getKey(), -1);
                    }

                } catch (IllegalArgumentException e) {
                    if (!object.get(i).name.contains(Keys.ADD_.name())) {
                        if (object.get(i).type() == JsonValue.ValueType.array) {
                            switch (object.get(i).get(0).type()) {
                                case longValue: {
                                    anotherProperties.add(object.get(i).asIntArray());
                                    break;
                                }
                                case doubleValue: {
                                    anotherProperties.add(object.get(i).asFloatArray());
                                    break;
                                }
                                case booleanValue: {
                                    anotherProperties.add(object.get(i).asBooleanArray());
                                    break;
                                }
                                case stringValue: {
                                    anotherProperties.add(object.get(i).asStringArray());
                                    break;
                                }
                                default:
                                    System.out.println("Unknown type of values in the array");
                            }
                        } else {
                            switch (object.get(i).type()) {
                                case longValue: {
                                    anotherProperties.add(object.get(i).asInt());
                                    break;
                                }
                                case doubleValue: {
                                    anotherProperties.add(object.get(i).asFloat());
                                    break;
                                }
                                case booleanValue: {
                                    anotherProperties.add(object.get(i).asBoolean());
                                    break;
                                }
                                case stringValue: {
                                    anotherProperties.add(object.get(i).asString());
                                    break;
                                }
                                default:
                                    System.out.println("Unknown value type");
                            }
                        }
                    }
                }
            }
        } else {
            properties.put(Keys.MODEL_SIZE_KEY.getKey(), 1f);
            properties.put(Keys.TEXTURE_SIZE_KEY.getKey(), 16);
            properties.put(Keys.VARIATIONS_COUNT_KEY.getKey(), -1);
        }
    }

    private static List<Model> getParts(JsonValue object, @Null float modelSize, @Null int textureSize) {
        List<Model> parts = new ArrayList<>();
        Vector3 position = new Vector3(0, 0, 0);
        Vector3[] movements = new Vector3[]{new Vector3(0, 0, 0)};
        Vector3 axis = new Vector3(0, 0, 0);
        float angle = 0;
        float startSize = 0, finalSize = 0;
        float movementSpeed = 5;
        boolean loopedRotation = false, loopedScaling = false, loopedMovement = false;

        for (int i = 0; i < object.size; i++) {
            if(object.get(i).name.contains(Keys.ADD_.name())){
                for (int j = 0; j < object.get(i).size; j++) {
                    JsonValue obj = object.get(i).get(j);

                    switch (obj.name){
                        case "position":{
                            position = new Vector3(obj.getFloat(0), obj.getFloat(1), obj.getFloat(2));
                            break;
                        }
                        case "rotation": {
                            angle = obj.getFloat(0);
                            axis = new Vector3(obj.getFloat(1), obj.getFloat(2), obj.getFloat(3));
                            loopedRotation = obj.getBoolean(4);
                            break;
                        }
                        case "scaling": {
                            startSize = obj.getFloat(0);
                            finalSize = obj.getFloat(1);
                            loopedScaling = obj.getBoolean(2);
                            break;
                        }
                        case "moving": {
                            int movementsCount = obj.size / 3;
                            movements = new Vector3[movementsCount];

                            for (int k = 0; k < movementsCount; k++) {
                                movements[k] = new Vector3(obj.getFloat(k * 3), obj.getFloat(k * 3 + 1), obj.getFloat(k * 3 + 2));
                            }
                            movementSpeed = obj.getInt(obj.size - 2);
                            loopedMovement = obj.getBoolean(obj.size - 1);
                        }
                    }
                }

                parts.add(
                        new Model(
                                object.get(i).name.replaceAll(Keys.ADD_.name(), ""),
                                new Vector3(0, 0, 0),
                                modelSize,
                                textureSize
                        ).asPart()
                                .setPosition(position)
                                .setRotation(angle, axis, loopedRotation)
                                .setMovements(movements, movementSpeed, loopedMovement)
                );
            }
        }

        return parts;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> contentType, String objectName){
        if(contentType != null){
            T object = (T) contentTypes.get(contentType).get(objectName);

            if(object != null) {
                return object;
            }
        }

        return (T) Vars.NULL_BLOCK;
    }
}