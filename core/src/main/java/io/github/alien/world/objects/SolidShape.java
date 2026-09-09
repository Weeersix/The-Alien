package io.github.alien.world.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.SerializationException;
import io.github.alien.Constants;
import io.github.alien.graphics.g3d.models.Model;
import io.github.alien.graphics.g3d.models.ModelLoader;
import io.github.alien.graphics.g3d.models.ModelRenders;
import io.github.alien.utils.FileUtils;

import java.util.*;

public class SolidShape extends WorldObject{
    public int[] sidesCondition;

    public SolidShape(String name, float modelSize, int textureSize, @Null Integer variationsCount, int[] sidesCondition){
        super(name, modelSize, textureSize, variationsCount);
        this.sidesCondition = sidesCondition;
        reload(SolidShape.class, (Object) sidesCondition);
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
        model = new SolidShapeModel(haveVariants ? getVariant(variationsCount) : this.name, position != null ? position : this.position, modelSize, textureSize);
    }

//    public void hideSides(int[] sidesCondition){
//        ((SolidShapeModel) model).setSidesCondition(sidesCondition);
//    }

    public int[] getSidesCondition(){
        return this.sidesCondition;
    }



    public static class SolidShapeModel extends Model {
        protected String variantName;
        protected int[] sidesCondition = new int[6];
        private Map<Integer, TextureRegion> sides;
        protected boolean haveVariants = false;

        public SolidShapeModel(String blockName, Vector3 blockPosition, float modelSize, int textureSize){
            super(blockName, blockPosition, modelSize, textureSize);
        }

        @Override
        protected void load(String name, Vector3 position){
            loader = new SolidShapeModelLoader();
            sides = new HashMap<>();

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

            getSides();

            model = ModelRenders.modelRender(
                    loader.hasModelSprite ?
                            FileUtils.find((!haveVariants ? name : variantName) + loader.postfix, ".png") :
                            FileUtils.find(!haveVariants ? name : variantName, ".png"),
                    sides,
                    loader.parts,
                    loader.typedPositions,
                    loader.customPositions,
                    loader.partPositionType,
                    drawParts,
                    modelSize
            );
        }

//        public void setSidesCondition(int[] sidesCondition){
//            this.sidesCondition = sidesCondition;
//        }

        private void getSides() {
            for (int i = 0; i < 6; i++) {
                sides.put(i, loader.model.get(i));
            }
        }



        private static class SolidShapeModelLoader extends ModelLoader {
            public SolidShapeModelLoader(){}

            @Override
            public void load(){
                hasModelSprite = !Objects.equals(getModelAtlas(), Constants.NULL);

                if(hasModelSprite) {
                    splitModelTexture();
                }

                getModelData();
                if(hasModelSprite) {
                    getParts();
                }
            }

            @Override
            public void getModelData(){
                String newName;
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

                    for (int i = 0; i < partsData.size; i++) {
                        JsonValue part = partsData.get(i);

                        int[] texturePoses = new int[2];

                        if (part.size <= 6) {
                            int x = 0, y = 0, z = 0;

                            for (int j = 3; j < part.size; j++) {
                                coord[i] = part.getString(j);

                                switch (coord[i].replaceAll("-\\d.*", "")) {
                                    case "X": x = Integer.parseInt(coord[i].replaceAll("X-", "")); break;
                                    case "Y": y = Integer.parseInt(coord[i].replaceAll("Y-", "")); break;
                                    case "Z": z = Integer.parseInt(coord[i].replaceAll("Z-", "")); break;
                                }
                            }

                            for (int j = 0; j < 2; j++) {
                                texturePoses[j] = part.getInt(j);
                            }
                            texturePositions.add(texturePoses);
                            if (part.get(2) != null) {
                                partPositionType.add(part.getString(2));
                            } else System.out.println("Set the position type in the model part: " + this.name);
                            typedPositions.add(new Vector3(x, y, z));

                            parts.get("TYPED").add(modelMap[texturePositions.get(i)[0]][texturePositions.get(i)[1]]);
                        } else {
                            float[] rect = new float[12];

                            for (int j = 0; j < 2; j++) texturePoses[j] = part.getInt(j);
                            for (int j = 0; j < 12; j++) rect[j] = part.getFloat(j + 2);

                            customPositions.add(rect);
                            texturePositions.add(texturePoses);

                            model.add(modelMap[texturePositions.get(i)[0]][texturePositions.get(i)[1]]);
                            parts.get("CUSTOM").add(modelMap[texturePositions.get(i)[0]][texturePositions.get(i)[1]]);
                        }
                    }

                    model.addAll(Arrays.asList(modelMap[0]).subList(0, 6));
                } catch (SerializationException e) {
                    TextureRegion texture = new TextureRegion(new Texture(FileUtils.find(name, ".png")));

                    if (!hasModelSprite) {
                        for (int i = 0; i < 6; i++) {
                            model.add(i, texture);
                        }
                    } else {
                        model.addAll(Arrays.asList(modelMap[0]).subList(0, 6));
                    }
                }
            }

            private void getParts() {
                for (int[] texturePosition : texturePositions) {
                    model.add(modelMap[texturePosition[0]][texturePosition[1]]);
                }
            }
        }
    }
}