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
                    WorldObject object = Loads.get(SolidShape.class, "stone").createNew(new Vector3(x, y, z));

                    blocks.add(object);
                    world.put(new Vector3(x, y, z), object);
                }
            }
        }
    }

    public void update(){
        int[] condition = new int[6];

        Vector3[] objectsNearby;

        if(WorldObject.getAllObjects().size() == this.blocks.size() && WorldObject.getObjectPositions().size() == this.blocks.size()) {

            for (int i = 0; i < WorldObject.getAllObjects().size(); i++) {
                Vector3 pos = WorldObject.getObjectPositions().get(i);

                WorldObject object = WorldObject.getAllObjects().get(WorldObject.getObjectPositions().get(i));

                objectsNearby = new Vector3[]{
                        new Vector3(pos.x, pos.y + 1, pos.z),
                        new Vector3(pos.x, pos.y - 1, pos.z),
                        new Vector3(pos.x + 1, pos.y, pos.z),
                        new Vector3(pos.x - 1, pos.y, pos.z),
                        new Vector3(pos.x, pos.y, pos.z + 1),
                        new Vector3(pos.x, pos.y, pos.z - 1)
                };

                for (int j = 0; j < objectsNearby.length; j++) {
                    if(WorldObject.getAllObjects().get(objectsNearby[j]) != null){
                        condition[j] = 1;
                    } else condition[j] = 0;
                }

                if(object instanceof SolidShape) {
                    if (Arrays.equals(condition, new int[]{1, 1, 1, 1, 1, 1})) {
                        object.hide();
                    }
//                    else {
//                        for (int j = 0; j < ((SolidShape) object).getSidesCondition().length; j++) {
//                            if(condition[j] == 1){
//                                if(((SolidShape) object).getSidesCondition()[j] == 0) condition[j] = 0;
//                            }
//                        }
//                        ((SolidShape) object).hideSides(condition);
//                    }
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
            block.set(modelBatch);
        });
    }
}
