package io.github.alien.graphics.g3d.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import io.github.alien.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Part<T extends Model> {
    public String id;
    private final T parent;
    public TextureRegion[] texture;
    private float frameTime = 0;

    public float[] rect;
    public Node node;
    public int attribute;
    public Material material;

    private static final Map<TextureRegion[], TextureRegion[]> regionsCache = new HashMap<>();

    public Part(String id, T parent, ModelBuilder builder, TextureRegion[] texture, float[] rect, Vector3 axis, float angle){
        this.id = id;
        this.parent = parent;
        this.rect = rect;

        if (!regionsCache.containsKey(texture)) {
            regionsCache.put(texture, texture);
            this.texture = texture;
        } else {
            this.texture = regionsCache.get(texture);
        }

        parent.setPartSettings(this);

        this.node = builder.node(); node.id = id;
        MeshPartBuilder mpb = builder.part(
                this.id,
                GL20.GL_TRIANGLES,
                this.attribute,
                this.material
        );

        if (texture.length > 1) {
            mpb.setUVRange(0f, 0f, 1f, 1f);
        } else {
            mpb.setUVRange(texture[0]);
        }

        mpb.rect(
                this.rect[0], this.rect[ 1], this.rect[ 2],
                this.rect[3], this.rect[ 4], this.rect[ 5],
                this.rect[6], this.rect[ 7], this.rect[ 8],
                this.rect[9], this.rect[10], this.rect[11],
                0, 0, 0
        );

        node.rotation.set(axis, angle);
    }

    public String getId(){
        return this.id;
    }
    public Class<?> getParentClass(){
        return this.parent.getClass();
    }

    public void setVisibility(boolean visible){
        parent.model.getNode(id).parts.get(0).enabled = visible;
    }
    public void dispose(){
        parent.model.getNode(id).parts.clear();
    }

    private float stateTime = 0;
    private int currentFrameCount = 0;
    private boolean started = false;

    public void update(){
        stateTime += Gdx.graphics.getDeltaTime();

        if (this.texture.length > 1 && stateTime >= frameTime) {
            currentFrameCount = (currentFrameCount + 1) % texture.length;

            ((TextureAttribute) parent.model.getNode(id).parts.get(0).material.get(TextureAttribute.Diffuse)).set(texture[currentFrameCount]);
            stateTime = 0;
        }

        if(!started){
            this.frameTime = texture.length > 1 ? (float) 1 / texture.length : 0;
            started = true;
        }
    }


    public static class Shape {
        private static Vector3 typedCoord;
        private static Vector3 altTypedCoord;
        private static float modelSize, modelSizeHalf;

        public static float[] getRect(Vector3 typedPosition, String partPositionType, float modelSize) {
            float[] rect = new float[15];
            Shape.modelSize = modelSize * 2;
            Shape.modelSizeHalf = Shape.modelSize / 2;

            getCoord(typedPosition);

            for (PartParameters param : PartParameters.values()) {
                if (param.name().equals(partPositionType)) {
                    rect = param.getRect();
                }
            }

            return rect;
        }

        private static void getCoord(Vector3 typedPosition) {
            typedCoord = new Vector3(
                    -(modelSizeHalf - typedPosition.x * Constants.PIXEL_SIZE * modelSize),
                    -(modelSizeHalf - typedPosition.y * Constants.PIXEL_SIZE * modelSize),
                    -(modelSizeHalf - typedPosition.z * Constants.PIXEL_SIZE * modelSize)
            );

            altTypedCoord = new Vector3(
                    typedCoord.x + getPositionMultiply(typedCoord.x),
                    typedCoord.y + getPositionMultiply(typedCoord.y),
                    typedCoord.z + getPositionMultiply(typedCoord.z)
            );
        }

        private static float getPositionMultiply(float num){
            return -(modelSizeHalf + num) * 2;
        }

        public enum PartParameters {
            X_PLUS {
                @Override
                public float[] getRect() {
                    return new float[]{
                            -typedCoord.x,     typedCoord.y,     typedCoord.z,
                            -typedCoord.x,     typedCoord.y, -altTypedCoord.z,
                            -typedCoord.x, -altTypedCoord.y, -altTypedCoord.z,
                            -typedCoord.x, -altTypedCoord.y,     typedCoord.z,
                    };
                }
            },
            X_MINUS {
                @Override
                public float[] getRect() {
                    return new float[]{
                            typedCoord.x,     typedCoord.y,     typedCoord.z,
                            typedCoord.x,     typedCoord.y, -altTypedCoord.z,
                            typedCoord.x, -altTypedCoord.y, -altTypedCoord.z,
                            typedCoord.x, -altTypedCoord.y,     typedCoord.z,
                    };
                }
            },

            Y_PLUS {
                @Override
                public float[] getRect() {
                    return new float[]{
                                typedCoord.x, -typedCoord.y, -altTypedCoord.z,
                            -altTypedCoord.x, -typedCoord.y, -altTypedCoord.z,
                            -altTypedCoord.x, -typedCoord.y,     typedCoord.z,
                                typedCoord.x, -typedCoord.y,     typedCoord.z,
                    };
                }
            },
            Y_MINUS {
                @Override
                public float[] getRect() {
                    return new float[]{
                                typedCoord.x, typedCoord.y,     typedCoord.z,
                            -altTypedCoord.x, typedCoord.y,     typedCoord.z,
                            -altTypedCoord.x, typedCoord.y, -altTypedCoord.z,
                                typedCoord.x, typedCoord.y, -altTypedCoord.z,
                    };
                }
            },
            // -0.625
            Z_PLUS {
                @Override
                public float[] getRect() {
                    return new float[]{
                                typedCoord.x,     typedCoord.y, -typedCoord.z,
                            -altTypedCoord.x,     typedCoord.y, -typedCoord.z,
                            -altTypedCoord.x, -altTypedCoord.y, -typedCoord.z,
                                typedCoord.x, -altTypedCoord.y, -typedCoord.z,
                    };
                }
            },

            Z_MINUS {
                @Override
                public float[] getRect() {
                    return new float[]{
                                typedCoord.x,     typedCoord.y,  typedCoord.z,
                            -altTypedCoord.x,     typedCoord.y,  typedCoord.z,
                            -altTypedCoord.x, -altTypedCoord.y,  typedCoord.z,
                                typedCoord.x, -altTypedCoord.y,  typedCoord.z,
                    };
                }
            };

            public abstract float[] getRect();
        }
    }
}
