package io.github.alien.graphics.g3d.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import io.github.alien.Vars;
import io.github.alien.utils.FileUtils;

import java.util.*;

public class ModelLoader {
    protected String name;
    public String postfix;
    protected Integer textureSize;
    protected TextureRegion[][] modelMap;
    public boolean hasModelSprite;

    public List<int[]> texturePositions = new ArrayList<>();
    public List<String> partPositionType = new ArrayList<>();
    public List<Vector3> typedPositions = new ArrayList<>();
    public List<float[]> customPositions = new ArrayList<>();

    public Map<String, List<TextureRegion>> parts = new HashMap<>();

    public List<TextureRegion> model = new ArrayList<>();

    public ModelLoader(){}

    public void load(){
        hasModelSprite = !Objects.equals(getModelAtlas(), Vars.NULL);

        if(hasModelSprite) {
            splitModelTexture();
        }
        getModelData();
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
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

                    model.add(modelMap[texturePositions.get(i)[0]][texturePositions.get(i)[1]]);
                    parts.get("TYPED").add(modelMap[texturePositions.get(i)[0]][texturePositions.get(i)[1]]);
                } else  {
                    float[] rect = new float[12];

                    for (int j = 0; j < 2; j++) texturePoses[j] = part.getInt(j);
                    for (int j = 0; j < 12; j++) rect[j] = part.getFloat(j + 2);

                    customPositions.add(rect);
                    texturePositions.add(texturePoses);

                    model.add(modelMap[texturePositions.get(i)[0]][texturePositions.get(i)[1]]);
                    parts.get("CUSTOM").add(modelMap[texturePositions.get(i)[0]][texturePositions.get(i)[1]]);
                }
            }
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
        texturePositions.clear();
        partPositionType.clear();
        typedPositions.clear();
        customPositions.clear();
    }
}
