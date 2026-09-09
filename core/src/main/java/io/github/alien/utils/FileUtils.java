package io.github.alien.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import io.github.alien.Vars;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public class FileUtils {
    public static String find(String name, String extension) {
        try(Stream<String> lines = Files.lines(Vars.ASSETS_FILE_PATH)) {
            return lines
                    .filter(line -> line.contains(name + extension))
                    .findFirst()
                    .orElse(Vars.NULL);
        } catch (IOException e) {
            return null;
        }
    }

    /*       Texture Utils       */

    private static final Map<String, Texture> textureCache = new HashMap<>();

    public static TextureRegion[][] separateTexture(int columns, int rows, String name){
        Texture texture = textureCache(name);

        return TextureRegion.split(texture, columns, rows);
    }



    public static Texture textureCache(String texturePath){
        if (textureCache.containsKey(texturePath)) {
            return textureCache.get(texturePath);
        } else {
            Texture newTexture = new Texture(texturePath);
            textureCache.put(texturePath, newTexture);
            return newTexture;
        }
    }

    public static void clearCache() {
        for (Texture texture : textureCache.values()) {
            texture.dispose();
        }
        textureCache.clear();
    }



    private static boolean isPixelEmpty(int pixel) {
        int alpha = (pixel >>> 24) & 0xFF;

        return alpha == 0;
    }



    /*       Json Files Utils       */

    private static final JsonReader reader = new JsonReader();

    public static JsonValue readValue(String fileName, String key){
        FileHandle filePath = Gdx.files.internal(find(fileName, ".json"));

        return reader.parse(filePath).get(key);
    }
}
