package io.github.alien.loads;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.SerializationException;
import io.github.alien.Constants;
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
                JsonValue object = content.get(i);
                List<Object> anotherProperties = new ArrayList<>();
                List<Object> args = new ArrayList<>();

                getProperties(object, properties, anotherProperties);

                args.add(object.name);
                args.add(properties.get("model-size"));
                args.add(properties.get("texture-size"));
                args.add(properties.get("variations-count"));
                args.addAll(anotherProperties);

                T obj = constructor.newInstance(args.toArray());
                objects.put(object.name, obj);

                boolean hasParts = false;
                for (JsonValue v : object) {
                    if (v.name.contains("ADD_")) {
                        hasParts = true;
                        break;
                    }
                }

                if(WorldObject.class.isAssignableFrom(objectClass)) {
                    if (hasParts) {
                        Method attachPart = objectClass.getMethod("attachParts", List.class);
                        attachPart.invoke(obj, getParts(object, (Float) properties.get("model-size"), (Integer) properties.get("texture-size")));
                    }
                }
            }
        } catch (SerializationException ignored){}
    }

    private static <T> Constructor<?> getConstructor(Class<T> objectClass) throws Exception {
        Constructor<?> constructor;

        for (int i = 0; i < objectClass.getConstructors().length; i++) {
            constructor = objectClass.getConstructors()[i];

            if(constructor.getParameters()[1].getType() != Vector3.class) {
                return constructor;
            }
        }

        return objectClass.getConstructor(String.class, float.class, int.class, Integer.class);
    }

    private static void getProperties(JsonValue object, HashMap<String, Object> properties, List<Object> anotherProperties){
        if(object.size != 0) {
            for (int i = 0; i < object.size; i++) {
                JsonValue param = object.get(i);

                properties.put("model-size", object.has("model-size") ? object.getFloat("model-size") : 1f);
                properties.put("texture-size", object.has("texture-size") ? object.getInt("texture-size") : 16);
                properties.put("variations-count", object.has("variations-count") ? object.getInt("variations-count") : -1);

                if (!param.name.equals("model-size") &&
                        !param.name.equals("texture-size") &&
                        !param.name.equals("variations-count") &&
                        !param.name.contains("ADD_"))
                {
                    if (param.type() == JsonValue.ValueType.array) {
                        switch (param.get(0).type()) {
                            case longValue: {
                                anotherProperties.add(param.asIntArray());
                                break;
                            }
                            case doubleValue: {
                                anotherProperties.add(param.asFloatArray());
                                break;
                            }
                            case booleanValue: {
                                anotherProperties.add(param.asBooleanArray());
                                break;
                            }
                            case stringValue: {
                                anotherProperties.add(param.asStringArray());
                                break;
                            }
                            default:
                                System.out.println("Unknown type of values in the array");
                        }
                    } else {
                        switch (param.type()) {
                            case longValue: {
                                anotherProperties.add(param.asInt());
                                break;
                            }
                            case doubleValue: {
                                anotherProperties.add(param.asFloat());
                                break;
                            }
                            case booleanValue: {
                                anotherProperties.add(param.asBoolean());
                                break;
                            }
                            case stringValue: {
                                anotherProperties.add(param.asString());
                                break;
                            }
                            default:
                                System.out.println("Unknown value type");
                        }
                    }
                }
            }
        } else {
            properties.put("model-size", 1f);
            properties.put("texture-size", 16);
            properties.put("variations-count", -1);
        }
    }

    private static List<Model> getParts(JsonValue object, @Null float modelSize, @Null int textureSize) {
        List<Model> parts = new ArrayList<>();
        Vector3 position = new Vector3(0, 0, 0);
        Vector3 axis = new Vector3(0, 0, 0);
        float angle = 0, startAngle = 0, finalAngle = 0;
        float rotationSpeed = 0;
        boolean loopedRotation = false;

        for (int i = 0; i < object.size; i++) {
            if(object.get(i).name.contains("ADD_")){
                for (int j = 0; j < object.get(i).size; j++) {
                    JsonValue obj = object.get(i).get(j);

                    switch (obj.name){
                        case "position":{
                            position = new Vector3(obj.getFloat(0), obj.getFloat(1), obj.getFloat(2));
                            break;
                        }
                        case "rotation": {
                            if(obj.get("axis") != null) axis = new Vector3(obj.get("axis").getFloat(0), obj.get("axis").getFloat(1), obj.get("axis").getFloat(2));
                            if(obj.get("minAngle") != null) startAngle = obj.getFloat("minAngle");
                            if(obj.get("maxAngle") != null) finalAngle = obj.getFloat("maxAngle");
                            if(obj.get("angle") != null) angle = obj.getFloat("angle");
                            if(obj.get("rotationSpeed") != null) rotationSpeed = obj.getFloat("rotationSpeed");
                            if(obj.get("looped") != null) loopedRotation = obj.getBoolean("looped");
                            break;
                        }
                    }
                }

                parts.add(
                        new Model(
                                object.get(i).name.replaceAll("ADD_", ""),
                                new Vector3(0, 0, 0),
                                modelSize,
                                textureSize
                        ).asPart()
                                .setPosition(position)
                                .setRotation(axis, angle, loopedRotation)
                                .setRotation(axis, startAngle, finalAngle, rotationSpeed)
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

        return (T) Constants.NULL_BLOCK;
    }
}