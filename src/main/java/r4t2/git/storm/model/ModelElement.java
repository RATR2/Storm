package r4t2.git.storm.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents one entry in a Java Edition model's "elements" array.
 *
 * Java coordinate space: 0-16 units, origin at (0,0,0).
 * "from" and "to" are [x, y, z] in that space.
 */
public class ModelElement {

    private float[] from = {0, 0, 0};
    private float[] to = {16, 16, 16};
    private ElementRotation rotation;
    private boolean shade = true;
    private Map<String, ElementFace> faces = new LinkedHashMap<>();

    public float[] getFrom() { return from; }
    public void setFrom(float[] from) { this.from = from; }

    public float[] getTo() { return to; }
    public void setTo(float[] to) { this.to = to; }

    public ElementRotation getRotation() { return rotation; }
    public void setRotation(ElementRotation rotation) { this.rotation = rotation; }

    public boolean isShade() { return shade; }
    public void setShade(boolean shade) { this.shade = shade; }

    public Map<String, ElementFace> getFaces() { return faces; }
    public void setFaces(Map<String, ElementFace> faces) { this.faces = faces; }

    public static class ElementRotation {
        private float[] origin = {8, 8, 8};
        private String axis = "y";
        private float angle = 0;
        private boolean rescale = false;

        public float[] getOrigin() { return origin; }
        public void setOrigin(float[] origin) { this.origin = origin; }
        public String getAxis() { return axis; }
        public void setAxis(String axis) { this.axis = axis; }
        public float getAngle() { return angle; }
        public void setAngle(float angle) { this.angle = angle; }
        public boolean isRescale() { return rescale; }
        public void setRescale(boolean rescale) { this.rescale = rescale; }
    }

    public static class ElementFace {
        private float[] uv;
        private String texture;
        private String cullface;
        private int rotation = 0;
        private int tintindex = -1;

        public float[] getUv() { return uv; }
        public void setUv(float[] uv) { this.uv = uv; }
        public String getTexture() { return texture; }
        public void setTexture(String texture) { this.texture = texture; }
        public String getCullface() { return cullface; }
        public void setCullface(String cullface) { this.cullface = cullface; }
        public int getRotation() { return rotation; }
        public void setRotation(int rotation) { this.rotation = rotation; }
        public int getTintindex() { return tintindex; }
        public void setTintindex(int tintindex) { this.tintindex = tintindex; }
    }
}
