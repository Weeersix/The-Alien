package io.github.alien.world.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.SerializationException;
import io.github.alien.graphics.g3d.models.Model;
import io.github.alien.graphics.g3d.models.ModelLoader;
import io.github.alien.graphics.g3d.models.Part;
import io.github.alien.utils.FileUtils;

import java.util.*;

public class SolidShape extends WorldObject{
    public int[] sidesCondition;

    public SolidShape(String name, float modelSize, int textureSize, @Null Integer variationsCount, int[] sidesCondition){
        super(name, modelSize, textureSize, variationsCount);
        this.sidesCondition = sidesCondition;
        adjust(SolidShape.class, (Object) sidesCondition);
    }

    public SolidShape(String name, Vector3 position, float modelSize, int textureSize, @Null Integer variationsCount, List<Model> parts, int[] sidesCondition){
        super(name, position, modelSize, textureSize, variationsCount, parts);
        this.sidesCondition = sidesCondition;
    }

    @Override
    protected void load(String name, @Null Vector3 position, float modelSize, int textureSize, @Null Integer variationsCount) {
        this.position = position;
        this.modelSize = modelSize;
        this.textureSize = textureSize;
        this.variationsCount = variationsCount;
        haveVariants = variationsCount != -1;

        canMove = false;
        this.name = name;
        model = new SolidShapeModel(
                haveVariants ? this.name + "-" + new Random().nextInt(1, variationsCount + 1) : this.name,
                position != null ? position : this.position,
                modelSize,
                textureSize
        );
    }


    public void hideSides(long[] sideProperties){
        for (int i = 0; i < sideProperties.length; i++) {
            model.parts.get(i).setVisibility(sideProperties[i] != 1);
        }
    }

    protected Vector3[] objectsNearby;

    @Override
    public void update(){
        for (int i = 0; i < allObjects.size(); i++) {
            Vector3 pos = objectPositions.get(i);
            long[] objNearbyCount = new long[6];

            objectsNearby = new Vector3[]{
                    new Vector3(pos.x, pos.y + 1, pos.z),
                    new Vector3(pos.x, pos.y - 1, pos.z),
                    new Vector3(pos.x + 1, pos.y, pos.z),
                    new Vector3(pos.x - 1, pos.y, pos.z),
                    new Vector3(pos.x, pos.y, pos.z + 1),
                    new Vector3(pos.x, pos.y, pos.z - 1)
            };

            if(allObjects.get(pos) != null){
                for (int j = 0; j < objectsNearby.length; j++) {
                    if (allObjects.get(objectsNearby[j]) != null) {
                        objNearbyCount[j] = 1;
                    } else objNearbyCount[j] = 0;
                }
            }

            long count = Arrays.stream(objNearbyCount).filter(num -> num == 1).count();
            SolidShape obj = ((SolidShape) allObjects.get(pos));
            if(count == 6){
                obj.hide();
            } else {
                if(obj != null){
                    for (int j = 0; j < objNearbyCount.length; j++) {
                        if(obj.hided) obj.show();
                    }
                    ((SolidShape) allObjects.get(pos)).hideSides(objNearbyCount);
                }
            }
        }
    }

    public static class SolidShapeModel extends Model {
        protected String variantName;
        protected boolean haveVariants = false;

        public SolidShapeModel(String blockName, Vector3 blockPosition, float modelSize, int textureSize){
            super(blockName, blockPosition, modelSize, textureSize);
        }

        @Override
        protected void load(String name, Vector3 position){
            loader = new SolidShapeModelLoader();
            builder.begin();

            haveVariants = name.matches(".*\\d$");

            if (haveVariants) {
                this.name = name.replaceAll("-\\d", "");
                this.variantName = name;
            } else this.name = name;
            this.position = position;

            if (haveVariants) {
                loader.setName(variantName);
            } else loader.setName(name);
            loader.setPostfix("-model");
            loader.setTextureSize(textureSize);
            loader.load();

            modelAtlas = loader.hasModelSprite ?
                    FileUtils.find(name + loader.postfix, ".png") :
                    FileUtils.find(name, ".png");

            for (int i = 0; i < loader.model.size(); i++) {
                String id;
                float[] rect;


                if(i >= 6) {
                    rect = loader.typedPositions.isEmpty() ?
                            loader.customPositions.get(i) :
                            Part.Shape.getRect(
                                    loader.typedPositions.get(i - 6),
                                    loader.partPositionType.get(i - 6),
                                    modelSize
                            );

                    id = modelAtlas + "-" + i;
                } else{
                    rect = getRects()[i];
                    id = modelAtlas + "-side-" + i;
                }

                this.parts.add(new Part<>(
                        id,
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
        }

        private float[][] getRects() {
            return new float[][]{
                    {
                            -modelSize, modelSize,  modelSize,
                             modelSize, modelSize,  modelSize,
                             modelSize, modelSize, -modelSize,
                            -modelSize, modelSize, -modelSize
                    },
                    {
                            -modelSize, -modelSize, -modelSize,
                             modelSize, -modelSize, -modelSize,
                             modelSize, -modelSize,  modelSize,
                            -modelSize, -modelSize,  modelSize
                    },
                    {
                            modelSize, -modelSize,  modelSize,
                            modelSize, -modelSize, -modelSize,
                            modelSize,  modelSize, -modelSize,
                            modelSize,  modelSize,  modelSize
                    },
                    {
                            -modelSize, -modelSize, -modelSize,
                            -modelSize, -modelSize,  modelSize,
                            -modelSize,  modelSize,  modelSize,
                            -modelSize,  modelSize, -modelSize
                    },
                    {
                            -modelSize, -modelSize, modelSize,
                             modelSize, -modelSize, modelSize,
                             modelSize,  modelSize, modelSize,
                            -modelSize,  modelSize, modelSize
                    },
                    {
                             modelSize, -modelSize, -modelSize,
                            -modelSize, -modelSize, -modelSize,
                            -modelSize,  modelSize, -modelSize,
                             modelSize,  modelSize, -modelSize
                    }
            };
        }



        private static class SolidShapeModelLoader extends ModelLoader {
            public SolidShapeModelLoader(){}

            @Override
            public void getModelData(){
                String newName = "";
                try {
                    String fileName = FileUtils.find(name, ".json");

                    if(!parts.containsKey("TYPED")) parts.put("TYPED", new ArrayList<>());
                    if(!parts.containsKey("CUSTOM")) parts.put("CUSTOM", new ArrayList<>());

                    if(name.matches(".*\\d$")){
                        if(fileName != null && fileName.replaceAll(".json", "").matches(".*\\d$")) {
                            newName = name;
                        } else newName = name.replaceAll("-\\d", "");
                    } else newName = name;

                    JsonValue partsData = FileUtils.readValue(newName, "parts");
                    String[] coord = new String[partsData.size];

//                    System.out.println(name + ", " + newName + ", " + fileName);
                    for (int i = 0; i < 6; i++) {
                        model.add(new TextureRegion[]{modelMap[0][i]});
                        zeroRotation();
                    }

                    for (int i = 0; i < partsData.size; i++) {
                        JsonValue part = partsData.get(i);

                        if (part.size < 13) {
                            int x = 0, y = 0, z = 0;

                            zeroRotation();

                            for (int j = 3; j < part.size; j++) {
                                coord[i] = part.getString(j);

                                if(!coord[i].contains("ROT_")) {
                                    switch (coord[i].replaceAll("-\\d.*", "")) {
                                        case "X": x = Integer.parseInt(coord[i].replaceAll("X-", "")); break;
                                        case "Y": y = Integer.parseInt(coord[i].replaceAll("Y-", "")); break;
                                        case "Z": z = Integer.parseInt(coord[i].replaceAll("Z-", "")); break;
                                    }
                                } else {
                                    String AXISnANGLE = coord[i].replaceAll("ROT_", "");

                                    switch (String.valueOf(AXISnANGLE.toCharArray()[0])) {
                                        case "X":{
                                            axes.set(i + 6, new Vector3(1, 0, 0));
                                            partRotations.set(i + 6, Float.parseFloat(AXISnANGLE.replaceAll("X-", "")));
                                            break;
                                        }
                                        case "Y": {
                                            axes.set(i + 6, new Vector3(0, 1, 0));
                                            partRotations.set(i + 6, Float.parseFloat(AXISnANGLE.replaceAll("Y-", "")));
                                            break;
                                        }
                                        case "Z": {
                                            axes.set(i + 6, new Vector3(0, 0, 1));
                                            partRotations.set(i + 6, Float.parseFloat(AXISnANGLE.replaceAll("Z-", "")));
                                            break;
                                        }
                                    }
                                }
                            }

                            if (part.get(2) != null) {
                                partPositionType.add(part.get(0).isArray() ? part.getString(1) : part.getString(2));
                            } else System.out.println("Set the position type in the model part: " + this.name);
                            typedPositions.add(new Vector3(x, y, z));

                            getPartData(part, "TYPED");
                        } else {
                            float[] rect = new float[12];

                            for (int j = 0; j < 12; j++) rect[j] = part.getFloat(j + 2);
                            customPositions.add(rect);

                            getPartData(part, "CUSTOM");
                            zeroRotation();
                        }
                    }
                } catch (SerializationException e) {
                    if(name.matches(".*\\d$")) newName = name;

                    TextureRegion texture = new TextureRegion(new Texture(FileUtils.find(newName, ".png")));

                    if (!hasModelSprite) {
                        for (int i = 0; i < 6; i++) {
                            model.add(i, new TextureRegion[]{texture});
                            zeroRotation();
                        }
                    } else {
                        for (int i = 0; i < 6; i++) {
                            model.add(new TextureRegion[]{modelMap[0][i]});
                            zeroRotation();
                        }
                    }
                }
            }
        }
    }
}