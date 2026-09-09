package io.github.alien.world.objects;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Null;
import io.github.alien.graphics.g3d.models.Model;
import io.github.alien.graphics.g3d.models.ModelLoader;
import io.github.alien.graphics.g3d.models.ModelRenders;
import io.github.alien.utils.FileUtils;

import java.util.List;

public class EditableShape extends WorldObject {

    public EditableShape(String name, float modelSize, int textureSize, @Null Integer variationsCount){
        super(name, modelSize, textureSize, variationsCount);

        reload(EditableShape.class);
    }

    public EditableShape(String name, Vector3 position, float modelSize, int textureSize, @Null Integer variationsCount, List<Model> parts){
        super(name, position, modelSize, textureSize, variationsCount, parts);
    }

    @Override
    protected void load(String name, Vector3 position, float modelSize, int textureSize, @Null Integer variationsCount){
        this.modelSize = modelSize;
        this.textureSize = textureSize;
        this.variationsCount = variationsCount;
        haveVariants = variationsCount != -1;

        canMove = false;
        this.name = name;

        model = new EditableShapeModel(haveVariants ? getVariant(variationsCount) : this.name, position != null ? position : this.position, modelSize, textureSize);
        if(position != null) model.setPosition(position);
    }

    private static class EditableShapeModel extends Model{
        private String variantName;

        public EditableShapeModel(String name, Vector3 position, float modelSize, int textureSize){
            super(name, position, modelSize, textureSize);
        }

        @Override
        public void load(String blockName, Vector3 blockPosition) {
            loader = new ModelLoader();

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

            model = ModelRenders.modelRender(
                    loader.hasModelSprite ?
                            FileUtils.find((!haveVariants ? name : variantName) + loader.postfix, ".png") :
                            FileUtils.find(!haveVariants ? name : variantName, ".png"),
                    null,
                    loader.parts,
                    loader.typedPositions,
                    loader.customPositions,
                    loader.partPositionType,
                    drawParts,
                    modelSize
            );
        }
    }
}
