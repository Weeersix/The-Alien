package io.github.alien;

import io.github.alien.world.objects.SolidShape;

import java.nio.file.Path;

public class Constants {
    public static final Path ASSETS_FILE_PATH = Path.of("assets/assets.txt");

    public static final int TILE_SIZE = 1;
    public static final int BASE_SPRITE_SIZE = 16;

    public static final float PIXEL_SIZE = 0.0625F;

    public static final String NULL = "other/null.png";
    public static final SolidShape NULL_BLOCK = new SolidShape("other/null.png", 1, 16, 1, null);
}
