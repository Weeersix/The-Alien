package io.github.alien.graphics.g3d.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import io.github.alien.Constants;
import io.github.alien.utils.FileUtils;

import java.util.*;

public class ModelLoader {
    protected String name;
    public String postfix;
    protected Integer textureSize;
    protected TextureRegion[][] modelMap;
    public boolean hasModelSprite;

    public List<String> partPositionType = new ArrayList<>();
    public List<Vector3> typedPositions = new ArrayList<>();
    public List<float[]> customPositions = new ArrayList<>();

    public Map<String, List<TextureRegion[]>> parts = new HashMap<>();
    public List<float[]> animatedPartsCoord = new ArrayList<>();

    public List<TextureRegion[]> model = new ArrayList<>();

    public ModelLoader(){}

    public void load(){
        hasModelSprite = !Objects.equals(getModelAtlas(), Constants.NULL);

        if(hasModelSprite) {
            splitModelTexture();
        }
        getModelData();
    }

    public void setName(String name){
        this.name = name;
    }

    public void setPostfix(String postfix){
        this.postfix = postfix;
        if (postfix == null) this.postfix = "";
    }

    public void setTextureSize(int textureSize){
        this.textureSize = textureSize;
    }

    public void getModelData(){
        try {
            JsonValue partsData = FileUtils.readValue(name, "parts");

            String[] coord = new String[partsData.size];

            if(!parts.containsKey("TYPED")) parts.put("TYPED", new ArrayList<>());
            if(!parts.containsKey("CUSTOM")) parts.put("CUSTOM", new ArrayList<>());

            for (int i = 0; i < partsData.size; i++) {
                JsonValue part = partsData.get(i);

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

                    if (part.get(2) != null) {
                        partPositionType.add(part.getString(2));
                    } else System.out.println("Set the position type in the model part: " + this.name);
                    typedPositions.add(new Vector3(x, y, z));

                    getPartData(part, "TYPED");
                } else  {
                    float[] rect = new float[12];

                    for (int j = 0; j < 12; j++) rect[j] = part.getFloat(j + (!part.get(0).isArray() ? 2 : 1)) * Constants.PIXEL_SIZE * 2;
                    customPositions.add(rect);

                    getPartData(part, "CUSTOM");
                }
            }
        } catch (SerializationException e) {
            throw new RuntimeException("The model is empty: " + name + " | Please create a file named: «" + name + ".json" +  "» and define the model parts in it.");
        }
    }

    public void getPartData(JsonValue part, String partType){
        int texturesCount = part.get(0).size;
        TextureRegion[] partData = new TextureRegion[part.get(0).isLong() ? 1 : texturesCount / 2];

        if(part.get(0).isLong()){
            partData[0] = modelMap[part.getInt(0)][part.getInt(1)];
        } else {
            for (int i = 0; i < texturesCount / 2; i++) {
                partData[i] = modelMap[part.get(0).getInt(i * 2)][part.get(0).getInt(i * 2 + 1)];

                animatedPartsCoord.add(new float[]{part.get(0).getInt(i * 2), part.get(0).getInt(i * 2 + 1)});
            }
        }

        model.add(partData);
        parts.get(partType).add(partData);
    }

    public String getModelAtlas(){
        return FileUtils.find(name + postfix, ".png");
    }

    protected void splitModelTexture(){
        modelMap = FileUtils.separateTexture(
                textureSize,
                textureSize,
                getModelAtlas()
        );
    }

    public void unload(){
        parts.clear();
        model.clear();
        partPositionType.clear();
        typedPositions.clear();
        customPositions.clear();
    }
}
