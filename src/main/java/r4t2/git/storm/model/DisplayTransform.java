package r4t2.git.storm.model;

/**
 * A single display transform slot from a Java Edition model's "display" block.
 * Each slot (thirdperson_righthand, firstperson_righthand, gui, head, etc.)
 * can have rotation, translation, and scale.
 *
 * All values are in Java model space (units of 1/16 of a block for translation).
 */
public class DisplayTransform {

    private float[] rotation = {0, 0, 0};

    private float[] translation = {0, 0, 0};
    private float[] scale = {1, 1, 1};

    public float[] getRotation() { return rotation; }
    public void setRotation(float[] rotation) { this.rotation = rotation; }

    public float[] getTranslation() { return translation; }
    public void setTranslation(float[] translation) { this.translation = translation; }

    public float[] getScale() { return scale; }
    public void setScale(float[] scale) { this.scale = scale; }
}
