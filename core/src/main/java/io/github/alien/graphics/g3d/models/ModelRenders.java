package io.github.alien.graphics.g3d.models;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Null;
import io.github.alien.Constants;
import io.github.alien.utils.FileUtils;

import java.util.*;

public class ModelRenders {
    private static final ModelBuilder builder = new ModelBuilder();

    private static float modelSizeHalf;
    private static Vector3 pos = new Vector3(0, 0, 0), alternatePos = new Vector3(0, 0, 0);

    public static ModelInstance modelRender(String atlas, @Null Map<Integer, TextureRegion> sides, Map<String, List<TextureRegion>> parts, List<Vector3> typedPositions, List<float[]> customPositions, List<String> partPositionTypes, boolean drawParts, @Null Float modelSize) {
        ModelRenders.modelSizeHalf = modelSize / 2;

        builder.begin();

        int attributes =
                VertexAttributes.Usage.Position
                        | VertexAttributes.Usage.Normal
                        | VertexAttributes.Usage.TextureCoordinates;

        Material material = new Material(
                TextureAttribute.createDiffuse(FileUtils.textureCache(atlas)),
                new BlendingAttribute(GL20.GL_ONE_MINUS_SRC_ALPHA)
        );

        material.set(new FloatAttribute(FloatAttribute.AlphaTest, 0.1f));
        material.set(new IntAttribute(IntAttribute.CullFace, GL20.GL_NONE));

        MeshPartBuilder mpb = builder.part(
                atlas,
                GL20.GL_TRIANGLES,
                attributes,
                material
        );

        if(sides != null) {
            /*        X      Y      Z      */

            mpb.setUVRange(sides.get(0));
            mpb.rect(
                    -modelSizeHalf, modelSizeHalf,  modelSizeHalf,
                     modelSizeHalf, modelSizeHalf,  modelSizeHalf,
                     modelSizeHalf, modelSizeHalf, -modelSizeHalf,
                    -modelSizeHalf, modelSizeHalf, -modelSizeHalf,
                    0, 1, 0
            );

            mpb.setUVRange(sides.get(1));
            mpb.rect(
                    -modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                     modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                     modelSizeHalf, -modelSizeHalf,  modelSizeHalf,
                    -modelSizeHalf, -modelSizeHalf,  modelSizeHalf,
                    0, -1, 0
            );

            mpb.setUVRange(sides.get(2));
            mpb.rect(
                    modelSizeHalf, -modelSizeHalf,  modelSizeHalf,
                    modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                    modelSizeHalf,  modelSizeHalf, -modelSizeHalf,
                    modelSizeHalf,  modelSizeHalf,  modelSizeHalf,
                    1, 0, 0
            );

            mpb.setUVRange(sides.get(3));
            mpb.rect(
                    -modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                    -modelSizeHalf, -modelSizeHalf,  modelSizeHalf,
                    -modelSizeHalf,  modelSizeHalf,  modelSizeHalf,
                    -modelSizeHalf,  modelSizeHalf, -modelSizeHalf,
                    -1, 0, 0
            );

            mpb.setUVRange(sides.get(4));
            mpb.rect(
                    -modelSizeHalf, -modelSizeHalf, modelSizeHalf,
                     modelSizeHalf, -modelSizeHalf, modelSizeHalf,
                     modelSizeHalf,  modelSizeHalf, modelSizeHalf,
                    -modelSizeHalf,  modelSizeHalf, modelSizeHalf,
                    0, 0, 1
            );

            mpb.setUVRange(sides.get(5));
            mpb.rect(
                     modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                    -modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                    -modelSizeHalf,  modelSizeHalf, -modelSizeHalf,
                     modelSizeHalf,  modelSizeHalf, -modelSizeHalf,
                    0, 0, -1
            );

            if (parts != null && drawParts) {
                partsRender(mpb, parts, typedPositions, customPositions, partPositionTypes, modelSize);
            }
        } else {
            partsRender(mpb, parts, typedPositions, customPositions, partPositionTypes, modelSize);
        }

        return new ModelInstance(builder.end());
    }

    private static float getPositionMultiply(float num){
        return (-modelSizeHalf - num) * 2;
    }

    private static void partsRender(MeshPartBuilder mpb, Map<String, List<TextureRegion>> parts, List<Vector3> typedPositions, List<float[]> customPositions, List<String> partPositionTypes, float size) {
        Vector3[] typedCoords = new Vector3[parts.get("TYPED").size()], altTypedCoords = new Vector3[parts.get("TYPED").size()];

        float[] coords = new float[12];

        if(!typedPositions.isEmpty()) {
            for (int i = 0; i < typedPositions.size(); i++) {
                TextureRegion part = parts.get("TYPED").get(i);

                Vector3 position = typedPositions.get(i);

                typedCoords[i] = new Vector3(
                        -(modelSizeHalf - position.x * Constants.PIXEL_SIZE * size),
                        -(modelSizeHalf - position.y * Constants.PIXEL_SIZE * size),
                        -(modelSizeHalf - position.z * Constants.PIXEL_SIZE * size)
                );

                altTypedCoords[i] = new Vector3(
                        typedCoords[i].x + getPositionMultiply(typedCoords[i].x),
                        typedCoords[i].y + getPositionMultiply(typedCoords[i].y),
                        typedCoords[i].z + getPositionMultiply(typedCoords[i].z)
                );

                for (PartParameters param : PartParameters.values()) {
                    if (param.name().equals(partPositionTypes.get(i))) {
                        pos = typedCoords[i];
                        alternatePos = altTypedCoords[i];

                        float[] rect = param.getRect();

                        mpb.setUVRange(part);
                        mpb.rect(
                                rect[0], rect[1], rect[2],
                                rect[3], rect[4], rect[5],
                                rect[6], rect[7], rect[8],
                                rect[9], rect[10], rect[11],
                                0, 0, 0
                        );
                    }
                }
            }
        }

        if(!customPositions.isEmpty()) {
            for (int i = 0; i < customPositions.size(); i++) {
                TextureRegion part = parts.get("CUSTOM").get(i);

                for (int j = 0; j < 12; j++) {
                    coords[j] = customPositions.get(i)[j] * Constants.PIXEL_SIZE;
                }

                mpb.setUVRange(part);
                mpb.rect(
                        coords[0], coords[1 ], coords[2 ],
                        coords[3], coords[4 ], coords[5 ],
                        coords[6], coords[7 ], coords[8 ],
                        coords[9], coords[10], coords[11],
                        0, 0, 0
                );
            }
        }
    }

    public enum PartParameters {
        X_PLUS {
            @Override
            public float[] getRect() {
                return new float[]{
                       -pos.x,           pos.y,           pos.z,
                       -pos.x,           pos.y, -alternatePos.z,
                       -pos.x, -alternatePos.y, -alternatePos.z,
                       -pos.x, -alternatePos.y,           pos.z,
                };
            }
        },
        X_MINUS {
            @Override
            public float[] getRect() {
                return new float[]{
                        pos.x,           pos.y,           pos.z,
                        pos.x,           pos.y, -alternatePos.z,
                        pos.x, -alternatePos.y, -alternatePos.z,
                        pos.x, -alternatePos.y,           pos.z,
                };
            }
        },

        Y_PLUS {
            @Override
            public float[] getRect() {
                return new float[]{
                                  pos.x, -pos.y, -alternatePos.z,
                        -alternatePos.x, -pos.y, -alternatePos.z,
                        -alternatePos.x, -pos.y,           pos.z,
                                  pos.x, -pos.y,           pos.z,
                };
            }
        },
        Y_MINUS {
            @Override
            public float[] getRect() {
                return new float[]{
                                   pos.x, pos.y,           pos.z,
                         -alternatePos.x, pos.y,           pos.z,
                         -alternatePos.x, pos.y, -alternatePos.z,
                                   pos.x, pos.y, -alternatePos.z,
                };
            }
        },
        // -0.625
        Z_PLUS {
            @Override
            public float[] getRect() {
                return new float[]{
                                pos.x,           pos.y, -pos.z,
                      -alternatePos.x,           pos.y, -pos.z,
                      -alternatePos.x, -alternatePos.y, -pos.z,
                                pos.x, -alternatePos.y, -pos.z,
                };
            }
        },

        Z_MINUS {
            @Override
            public float[] getRect() {
                return new float[]{
                                  pos.x,           pos.y,  pos.z,
                        -alternatePos.x,           pos.y,  pos.z,
                        -alternatePos.x, -alternatePos.y,  pos.z,
                                  pos.x, -alternatePos.y,  pos.z,
                };
            }
        };

        public abstract float[] getRect();
    }
}
