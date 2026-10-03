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
import com.badlogic.gdx.graphics.g3d.model.Node;
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

    public static ModelInstance modelRender(
            String atlas,
            @Null Map<Integer, TextureRegion> sides,
            Map<String, List<TextureRegion>> parts,
            List<Vector3> typedPositions,
            List<float[]> customPositions,
            List<String> partPositionTypes,
            boolean drawParts,
            @Null Float modelSize
    ){
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

        if(sides != null) {
            float[][] rs = new float[][]{
                    {
                            -modelSizeHalf, modelSizeHalf, modelSizeHalf,
                             modelSizeHalf, modelSizeHalf, modelSizeHalf,
                             modelSizeHalf, modelSizeHalf, -modelSizeHalf,
                            -modelSizeHalf, modelSizeHalf, -modelSizeHalf,
                             0, 1, 0
                    },
                    {
                            -modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                             modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                             modelSizeHalf, -modelSizeHalf,  modelSizeHalf,
                            -modelSizeHalf, -modelSizeHalf,  modelSizeHalf,
                             0, -1, 0
                    },
                    {
                            modelSizeHalf, -modelSizeHalf,  modelSizeHalf,
                            modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                            modelSizeHalf,  modelSizeHalf, -modelSizeHalf,
                            modelSizeHalf,  modelSizeHalf,  modelSizeHalf,
                            1, 0, 0
                    },
                    {
                            -modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                            -modelSizeHalf, -modelSizeHalf,  modelSizeHalf,
                            -modelSizeHalf,  modelSizeHalf,  modelSizeHalf,
                            -modelSizeHalf,  modelSizeHalf, -modelSizeHalf,
                            -1, 0, 0
                    },
                    {
                            -modelSizeHalf, -modelSizeHalf, modelSizeHalf,
                             modelSizeHalf, -modelSizeHalf, modelSizeHalf,
                             modelSizeHalf,  modelSizeHalf, modelSizeHalf,
                            -modelSizeHalf,  modelSizeHalf, modelSizeHalf,
                             0, 0, 1
                    },
                    {
                             modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                            -modelSizeHalf, -modelSizeHalf, -modelSizeHalf,
                            -modelSizeHalf,  modelSizeHalf, -modelSizeHalf,
                             modelSizeHalf,  modelSizeHalf, -modelSizeHalf,
                             0, 0, -1
                    }
            };

            for (int i = 0; i < sides.size(); i++) {
                String id = "side-" + i;
                float[] rect = rs[i];

                Node node = builder.node(); node.id = id;
                MeshPartBuilder mpb = builder.part(
                        id,
                        GL20.GL_TRIANGLES,
                        attributes,
                        material
                );
                mpb.setUVRange(sides.get(i));
                mpb.rect(
                        rect[0 ], rect[1 ],  rect[2 ],
                        rect[3 ], rect[4 ],  rect[5 ],
                        rect[6 ], rect[7 ],  rect[8 ],
                        rect[9 ], rect[10],  rect[11],
                        rect[12], rect[13],  rect[14]
                );
            }
        }

        if(drawParts){
            partsRender(
                    attributes, material,
                    parts,
                    typedPositions,
                    customPositions,
                    partPositionTypes,
                    modelSize
            );
        }

        return new ModelInstance(builder.end());
    }

    private static float getPositionMultiply(float num){
        return -(modelSizeHalf + num) * 2;
    }

    private static void partsRender(
            long attributes, Material material,
            Map<String, List<TextureRegion>> parts,
            List<Vector3> typedPositions,
            List<float[]> customPositions,
            List<String> partPositionTypes,
            float size
    ){
        Vector3[] typedCoords = new Vector3[parts.get("TYPED").size()], altTypedCoords = new Vector3[parts.get("TYPED").size()];
        float[] coords = new float[12];

        if(!typedPositions.isEmpty()) {
            for (int i = 0; i < typedPositions.size(); i++) {
                TextureRegion part = parts.get("TYPED").get(i);
                String id = "part-" + i;

                Node node = builder.node(); node.id = id;
                MeshPartBuilder mpb = builder.part(
                        id,
                        GL20.GL_TRIANGLES,
                        attributes,
                        material
                );

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
                                rect[0], rect[ 1], rect[ 2],
                                rect[3], rect[ 4], rect[ 5],
                                rect[6], rect[ 7], rect[ 8],
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
                String id = "part-" + i;

                for (int j = 0; j < 12; j++) {
                    coords[j] = customPositions.get(i)[j] * Constants.PIXEL_SIZE;
                }

                Node node = builder.node(); node.id = id;
                MeshPartBuilder mpb = builder.part(
                        id,
                        GL20.GL_TRIANGLES,
                        attributes,
                        material
                );

                mpb.setUVRange(part);
                mpb.rect(
                        coords[0], coords[ 1], coords[ 2],
                        coords[3], coords[ 4], coords[ 5],
                        coords[6], coords[ 7], coords[ 8],
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
