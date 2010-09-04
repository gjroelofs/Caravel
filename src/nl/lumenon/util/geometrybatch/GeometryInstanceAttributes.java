package nl.lumenon.util.geometrybatch;

import com.ardor3d.math.Matrix4;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;

/**
 * <code>GeometryInstanceAttributes</code> specifies the attributes for a
 * <code>GeometryInstance</code>.
 *
 * @author Patrik Lindegrén
 */
public class GeometryInstanceAttributes {
    protected Vector3 translation;    // Translation
    protected Vector3 scale;        // Scale
    protected Quaternion rotation;    // Rotation
    protected Matrix4 mtNormal;    // Normal matrix (scale, rotation)
    protected Matrix4 mtWorld;        // Local to world matrix (scale, rotation, translation)

    public GeometryInstanceAttributes(Vector3 translation, Vector3 scale,
                                      Quaternion rotation) {
        this.scale = scale;
        this.rotation = rotation;
        this.translation = translation;
        mtWorld = new Matrix4();
        mtNormal = new Matrix4();
        buildMatrices();
    }

    /**
     * Vector used to store and calculate rotation in degrees Not needed when
     * radian rotation is implemented in Matrix4f
     */
    private Vector3 rotationDegrees = new Vector3();

    /** <code>buildMatrices</code> updates the world and rotation matrix */
    public void buildMatrices() {
        // Scale (temporarily use mtWorld as storage)
        mtWorld.setIdentity();
        mtWorld.setValue(0, 0, scale.getX());
        mtWorld.setValue(1, 1, scale.getY());
        mtWorld.setValue(2, 2, scale.getZ());

        // Build rotation matrix (temporarily use mtNormal as storage)
//        rotationDegrees.set(rotation).multLocal(FastMath.RAD_TO_DEG);
        mtNormal.setIdentity();
        
        //mtNormal.setRotationQuaternion(rotation); DO SOMETING HERE
        
        
//        mtNormal.angleRotation(rotationDegrees);
        //mtNormal.radianRotation(rotation);            // Add a radian rotation function to Matrix4f (requested feature)

        // Build normal matrix (scale * rotation)
        mtNormal.multiplyLocal(mtWorld);

        // Build world matrix (scale * rotation + translation)
        mtWorld.set(mtNormal);
        
        mtWorld.setRow(3, new double[]{translation.getX(),translation.getY(),translation.getZ(),1});
    }

    public Vector3 getScale() {
        return scale;
    }

    /**
     * After using the <code>setScale</code> function, user needs to call the
     * <code>buildMatrices</code> function
     *
     * @param scale
     */
    public void setScale(Vector3 scale) {
        this.scale = scale;
    }

    public Vector3 getTranslation() {
        return translation;
    }

    /**
     * After using the <code>setTranslation</code> function, user needs to call
     * the <code>buildMatrices</code> function
     *
     * @param translation
     */
    public void setTranslation(Vector3 translation) {
        this.translation = translation;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    /**
     * After using the <code>setRotation</code> function, user needs to call the
     * <code>buildMatrices</code> function
     *
     * @param rotation
     */
    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    public Matrix4 getWorldMatrix() {
        return mtWorld;
    }

    public Matrix4 getNormalMatrix() {
        return mtNormal;
    }
}
