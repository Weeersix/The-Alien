package io.github.alien.world.generation;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import io.github.alien.loads.Loads;
import io.github.alien.world.objects.SolidShape;
import io.github.alien.world.objects.WorldObject;

import java.util.*;

public class WorldGenerator {
    private final int sizeX, sizeY, sizeZ;

    public static Map<Vector3, WorldObject> world = new HashMap<>();

    public List<WorldObject> blocks = new ArrayList<>();

    public WorldGenerator(int sizeX, int sizeY, int sizeZ) throws Exception {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        create();
    }

    public void create() throws Exception {
        for(int x = 0; x < sizeX; x++) {
            for(int y = 0; y < sizeY; y++) {
                for(int z = 0; z < sizeZ; z++) {
                    WorldObject object = Loads.get(SolidShape.class, "ruby-block").createNew(new Vector3(x, y, z));

                    blocks.add(object);
                    world.put(new Vector3(x, y, z), object);
                }
            }
        }
    }

    public void reload(){
        blocks.forEach(block -> {
            if(block.deleted) block.dispose();
        });
    }

    public void render(ModelBatch modelBatch){
        blocks.forEach(block -> {
            block.render(modelBatch);
        });
    }
}
