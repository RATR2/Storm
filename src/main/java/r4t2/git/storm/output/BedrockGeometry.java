package r4t2.git.storm.output;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Bedrock Edition geometry.json structure for a custom 3D item model.
 *
 * Bedrock geometry format (1.12.0+):
 * {
 *   "format_version": "1.12.0",
 *   "minecraft:geometry": [{
 *     "description": {
 *       "identifier": "geometry.storm.example_item_sword",
 *       "texture_width": 16,
 *       "texture_height": 16,
 *       "visible_bounds_width": 2,
 *       "visible_bounds_height": 2.5,
 *       "visible_bounds_offset": [0, 0.75, 0]
 *     },
 *     "bones": [ ... ]
 *   }]
 * }
 */
public class BedrockGeometry {

    public static final String FORMAT_VERSION = "1.12.0";
    private final String identifier;
    private final int textureWidth;
    private final int textureHeight;
    private final List<Bone> bones = new ArrayList<>();

    public BedrockGeometry(String identifier, int textureWidth, int textureHeight) {
        this.identifier = identifier;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public String getIdentifier() { return identifier; }
    public int getTextureWidth() { return textureWidth; }
    public int getTextureHeight() { return textureHeight; }
    public List<Bone> getBones() { return bones; }

    public void addBone(Bone bone) { bones.add(bone); }

    public static class Bone {
        private final String name;
        private final float[] pivot;
        private float[] rotation = {0, 0, 0};
        private final List<BoneCube> cubes = new ArrayList<>();
        public Bone(String name, float[] pivot) {
            this.name = name;
            this.pivot = pivot;
        }

        public String getName() { return name; }
        public float[] getPivot() { return pivot; }
        public float[] getRotation() { return rotation; }
        public void setRotation(float[] rotation) { this.rotation = rotation; }
        public List<BoneCube> getCubes() { return cubes; }
        public void addCube(BoneCube cube) { cubes.add(cube); }
    }

    public static class BoneCube {
        private final float[] origin;
        private final float[] size;
        private final float[] uv;
        private PerFaceUv perFaceUv;

        public BoneCube(float[] origin, float[] size, float[] uv) {
            this.origin = origin;
            this.size = size;
            this.uv = uv;
        }

        public float[] getOrigin() { return origin; }
        public float[] getSize() { return size; }
        public float[] getUv() { return uv; }
        public PerFaceUv getPerFaceUv() { return perFaceUv; }
        public void setPerFaceUv(PerFaceUv perFaceUv) { this.perFaceUv = perFaceUv; }
    }

    public static class PerFaceUv {
        public float[] north, south, east, west, up, down;
    }
}
