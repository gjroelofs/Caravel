package nl.lumenon.util.geometrybatch;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.Quaternion;


/**
 * <code>GeometryBatchInstanceAttributes</code> specifies the attributes for a
 * <code>GeometryBatchInstance</code>
 *
 * @author Patrik Lindegrén
 */
public class GeometryBatchInstanceAttributes
        extends GeometryInstanceAttributes {
    protected ColorRGBA color;

    public GeometryBatchInstanceAttributes(Vector3 translation, Vector3 scale,
            Quaternion rotation, ColorRGBA color) {
        super(translation, scale, rotation);
        this.color = color;
    }

    /** <code>buildMatrices</code> updates the world and rotation matrix */
    public ColorRGBA getColor() {
        return color;
    }

    public void setColor(ColorRGBA color) {
        this.color = color;
    }
}

