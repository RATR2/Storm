package r4t2.git.storm.converter;

import r4t2.git.storm.model.JavaModel;
import r4t2.git.storm.model.ModelElement;
import r4t2.git.storm.output.BedrockGeometry;
import r4t2.git.storm.pack.NamespaceResolver;

import java.util.Map;

/**
 * Converts a resolved Java Edition model with elements[] into a Bedrock geometry.json.
 */
public class GeometryConverter {

    private static final float JAVA_CENTER = 8.0f;
    /**
     * Converts a 3D Java model to a {@link BedrockGeometry}.
     *
     * @param model    The fully-resolved Java model.
     * @param itemId   Used to generate the geometry identifier.
     * @return The converted Bedrock geometry.
     */
    public BedrockGeometry convert(JavaModel model, String itemId) {
        String geoId = "geometry.storm." + new NamespaceResolver(itemId).toBedrockId();

        int texW = 16, texH = 16;

        BedrockGeometry geo = new BedrockGeometry(geoId, texW, texH);

        int boneIndex = 0;
        for (ModelElement element : model.getElements()) {
            String boneName = "bone_" + boneIndex++;
            BedrockGeometry.Bone bone = convertElement(element, boneName, model.getTextures(), texW, texH);
            geo.addBone(bone);
        }

        return geo;
    }

    private BedrockGeometry.Bone convertElement(ModelElement element, String boneName,
                                                 Map<String, String> textures,
                                                 int texW, int texH) {
        float[] from = element.getFrom();
        float[] to = element.getTo();

        float ox = from[0] - JAVA_CENTER;
        float oy = from[1] - JAVA_CENTER;
        float oz = from[2] - JAVA_CENTER;
        float sx = to[0] - from[0];
        float sy = to[1] - from[1];
        float sz = to[2] - from[2];
        float pivotX = ox + sx / 2f;
        float pivotY = oy + sy / 2f;
        float pivotZ = oz + sz / 2f;

        BedrockGeometry.Bone bone = new BedrockGeometry.Bone(boneName, new float[]{pivotX, pivotY, pivotZ});
        if (element.getRotation() != null) {
            ModelElement.ElementRotation javaRot = element.getRotation();
            float[] boneRot = {0, 0, 0};
            switch (javaRot.getAxis()) {
                case "x" -> boneRot[0] = -javaRot.getAngle();
                case "y" -> boneRot[1] = -javaRot.getAngle();
                case "z" -> boneRot[2] = javaRot.getAngle();
            }
            bone.setRotation(boneRot);
            float[] jOrigin = javaRot.getOrigin();
            float rpX = jOrigin[0] - JAVA_CENTER;
            float rpY = jOrigin[1] - JAVA_CENTER;
            float rpZ = jOrigin[2] - JAVA_CENTER;
            bone.getPivot()[0] = rpX;
            bone.getPivot()[1] = rpY;
            bone.getPivot()[2] = rpZ;
        }
        BedrockGeometry.BoneCube cube = new BedrockGeometry.BoneCube(
                new float[]{ox, oy, oz},
                new float[]{sx, sy, sz},
                new float[]{0, 0}
        );

        if (!element.getFaces().isEmpty()) {
            BedrockGeometry.PerFaceUv pfuv = new BedrockGeometry.PerFaceUv();
            for (Map.Entry<String, ModelElement.ElementFace> faceEntry : element.getFaces().entrySet()) {
                ModelElement.ElementFace face = faceEntry.getValue();
                float[] uv = face.getUv();
                if (uv == null || uv.length < 4) continue;
                float u1 = uv[0] / 16f * texW;
                float v1 = uv[1] / 16f * texH;
                float u2 = uv[2] / 16f * texW;
                float v2 = uv[3] / 16f * texH;
                float[] bedrockUv = {u1, v1, u2 - u1, v2 - v1};
                switch (faceEntry.getKey()) {
                    case "north" -> pfuv.north = bedrockUv;
                    case "south" -> pfuv.south = bedrockUv;
                    case "east"  -> pfuv.east  = bedrockUv;
                    case "west"  -> pfuv.west  = bedrockUv;
                    case "up"    -> pfuv.up    = bedrockUv;
                    case "down"  -> pfuv.down  = bedrockUv;
                }
            }
            cube.setPerFaceUv(pfuv);
        }
        bone.addCube(cube);
        return bone;
    }
}
