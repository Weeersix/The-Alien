package io.github.alien.world.objects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Null;
import io.github.alien.Constants;
import io.github.alien.graphics.g3d.models.Model;
import io.github.alien.graphics.g3d.models.ModelLoader;
import io.github.alien.graphics.g3d.models.Part;
import io.github.alien.utils.FileUtils;

import java.util.List;
import java.util.Random;

public class EditableShape extends WorldObject {

    public EditableShape(String name, float modelSize, int textureSize, @Null Integer variationsCount){
        super(name, modelSize, textureSize, variationsCount);

        adjust(EditableShape.class);
    }

    public EditableShape(String name, Vector3 position, float modelSize, int textureSize, @Null Integer variationsCount, List<Model> parts){
        super(name, position, modelSize, textureSize, variationsCount, parts);
    }

    @Override
    protected void load(String name, Vector3 position, float modelSize, int textureSize, @Null Integer variationsCount){
        this.position = position;
        this.modelSize = modelSize;
        this.textureSize = textureSize;
        this.variationsCount = variationsCount;
        haveVariants = variationsCount != -1;

        canMove = false;
        this.name = name;

        model = new EditableShapeModel(
                haveVariants ? this.name + "-" + new Random().nextInt(1, variationsCount + 1) : this.name,
                position != null ? position : this.position,
                modelSize,
                textureSize
        );
    }

    private static class EditableShapeModel extends Model{
        private String variantName;

        public EditableShapeModel(String name, Vector3 position, float modelSize, int textureSize){
            super(name, position, modelSize, textureSize);
        }

        @Override
        public void load(String blockName, Vector3 blockPosition) {
            loader = new ModelLoader();
            builder.begin();

            boolean haveVariants = blockName.matches(".*\\d$");

            if (haveVariants) {
                this.name = blockName.replaceAll("-\\d", "");
                this.variantName = blockName;
            } else this.name = blockName;
            this.position = blockPosition;

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
                String id = modelAtlas + "-" + i;

                float[] rect = loader.typedPositions.isEmpty() ?
                        loader.customPositions.get(i) :
                        Part.Shape.getRect(
                                loader.typedPositions.get(i),
                                loader.partPositionType.get(i),
                                modelSize
                        );

                this.parts.add(new Part<>(
                        id,
                        this,
                        builder,
                        loader.model.get(i),
                        rect
                ));
            }

            model = new ModelInstance(builder.end());

            if (position != null) {
                model.transform.translate(this.position);
            }
        }
    }
}
