package nl.lumenon.util.geometrybatch;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.MeshData;


/**
 * <code>GeometryBatchInstance</code> uses a <code>GeometryBatchInstanceAttributes</code>
 * to define an instance of object in world space. Uses TriMesh as source
 * data for the instance, instead of GeomBatch which does not have an index
 * buffer.
 *
 * @author Patrik Lindegrén
 */
public class GeometryBatchInstance
        extends GeometryInstance<GeometryBatchInstanceAttributes> {
    public MeshData instanceMesh;

    public GeometryBatchInstance(MeshData sourceBatch,
                                 GeometryBatchInstanceAttributes attributes) {
        super(attributes);
        this.instanceMesh = sourceBatch;
    }

    /** Vector used to store and calculate world transformations */
    Vector3 worldVector = new Vector3();

    /**
     * Uses the instanceAttributes to transform the instanceBatch into world
     * coordinates. The transformed instance mesh is added to the mesh.
     *
     * @param mesh
     */
    @Override
	public void commit(MeshData mesh) {
        if (mesh == null || instanceMesh == null || getNumVerts() <= 0) {
            return;
        }

        int nVerts = 0;

        // Texture buffers
        for (int i = 0; i < instanceMesh.getNumberOfUnits(); i++) {
            FloatBuffer texBufSrc = instanceMesh.getTextureCoords(i).getBuffer();
            FloatBuffer texBufDst = mesh.getTextureCoords(i).getBuffer();
            if (texBufSrc != null && texBufDst != null) {
                texBufSrc.rewind();
                texBufDst.put(texBufSrc);
            }
        }

        // Vertex buffer
        FloatBuffer vertBufSrc = instanceMesh.getVertexBuffer();
        FloatBuffer vertBufDst = mesh.getVertexBuffer();
        if (vertBufSrc != null && vertBufDst != null) {
            vertBufSrc.rewind();
            nVerts = vertBufDst.position() / 3;
            for (int i = 0; i < instanceMesh.getVertexCount(); i++) {
                worldVector.set(vertBufSrc.get(), vertBufSrc.get(),
                                vertBufSrc.get());

                attributes.getScale().multiply(worldVector, worldVector);                
                attributes.getTranslation().add(worldVector, worldVector);
                
                //attributes.getNormalMatrix().mult(, worldVector);
                vertBufDst.put((float) worldVector.getX());
                vertBufDst.put((float) worldVector.getY());
                vertBufDst.put((float) worldVector.getZ());
            }
        }

        // Color buffer
        FloatBuffer colorBufSrc = instanceMesh.getColorBuffer();
        FloatBuffer colorBufDst = mesh.getColorBuffer();
        if (colorBufSrc != null && colorBufDst != null) {
            colorBufSrc.rewind();
            for (int i = 0; i < instanceMesh.getVertexCount(); i++) {
                colorBufDst.put(colorBufSrc.get() * attributes.getColor().getRed());
                colorBufDst.put(colorBufSrc.get() * attributes.getColor().getGreen());
                colorBufDst.put(colorBufSrc.get() * attributes.getColor().getBlue());
                colorBufDst.put(colorBufSrc.get() * attributes.getColor().getAlpha());
            }
        } else if (colorBufDst != null) {
            for (int i = 0; i < instanceMesh.getVertexCount(); i++) {
                colorBufDst.put(attributes.getColor().getRed());
                colorBufDst.put(attributes.getColor().getGreen());
                colorBufDst.put(attributes.getColor().getBlue());
                colorBufDst.put(attributes.getColor().getAlpha());
            }
        }

        // Normal buffer
        FloatBuffer normalBufSrc = instanceMesh.getNormalBuffer();
        FloatBuffer normalBufDst = mesh.getNormalBuffer();
        if (normalBufSrc != null && normalBufDst != null) {
            normalBufSrc.rewind();
            for (int i = 0; i < instanceMesh.getVertexCount(); i++) {
                worldVector.set(normalBufSrc.get(), normalBufSrc.get(),
                                normalBufSrc.get());

                //attributes.getTranslation().multiply(worldVector, worldVector);
                //attributes.getNormalMatrix().mult(worldVector, worldVector);
                worldVector.normalizeLocal();
                normalBufDst.put((float) worldVector.getX());
                normalBufDst.put((float) worldVector.getY());
                normalBufDst.put((float) worldVector.getZ());
            }
        }

        // Index buffer        
        ShortBuffer indexBufSrc = (ShortBuffer) instanceMesh.getIndexBuffer();
        ShortBuffer indexBufDst = (ShortBuffer) mesh.getIndexBuffer();
        if (indexBufSrc != null && indexBufDst != null) {
            indexBufSrc.rewind();
            
            while(indexBufSrc.remaining()>=1){
            	indexBufDst.put((short) (nVerts + indexBufSrc.get()));
            }
        }
    }

    @Override
	public int getNumIndices() {
        if (instanceMesh == null) {
            return 0;
        }
        return instanceMesh.getIndices().remaining();
    }

    @Override
	public int getNumVerts() {
        if (instanceMesh == null) {
            return 0;
        }
        return instanceMesh.getVertexCount();
    }
}


