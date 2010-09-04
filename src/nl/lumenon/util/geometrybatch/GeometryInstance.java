

package nl.lumenon.util.geometrybatch;

import com.ardor3d.scenegraph.MeshData;

/**
 * <code>GeometryInstance</code> uses a <code>GeometryInstanceAttributes</code>
 * to define an instance of object in world space.
 *
 * @author Patrik Lindegrén
 */
public abstract class GeometryInstance<T extends GeometryInstanceAttributes> {
    protected T attributes;

    public abstract void commit(MeshData batch);

    public abstract int getNumIndices();

    public abstract int getNumVerts();

    public GeometryInstance(T attributes) {
        this.attributes = attributes;
    }

    public T getAttributes() {
        return attributes;
    }
}
