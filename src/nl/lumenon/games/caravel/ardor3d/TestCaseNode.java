/**
 * 
 */
package nl.lumenon.games.caravel.ardor3d;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.DataMode;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.geom.MeshCombiner;

/**
 * @author Gijs-Jan Roelofs
 *
 */
public class TestCaseNode extends Node{

	/* Static Variables */
	/* Instance Variables */
    private final int _xSize = 200;
    private final int _ySize = 200;
    private final int _totalSize = _xSize * _ySize;
	private Mesh _triangles, _trianglestrip,_degentrianglestrip;
	private String[] _renderTypes = new String[]{"Array","VBO","VBO Interleaved"};
	private String[] _meshTypes = new String[]{"Triangles","Triangle Strip","Triangle Strip, Degenerate"};
	private int _show=0, _render=0;
    private int _frames=0;    
    private boolean first=true;
    private long _startTime=System.currentTimeMillis();
	
	public TestCaseNode(){
        
		_trianglestrip = createMultiStripMesh();
		_degentrianglestrip =createDegenerateStripMesh();
		_triangles = createTriangleMesh();

        final double maxSize = Math.max(_xSize, _ySize);
        final double requiredDistance = (maxSize / 2)
        / Math.tan(Ardor3DBase._canvas.getCanvasRenderer().getCamera().getFovY() * MathUtils.DEG_TO_RAD * 0.5);
        
        _triangles.updateModelBound();
        _triangles.setTranslation(-_xSize * 0.5, -_ySize * 0.5, requiredDistance);
        
        _trianglestrip.updateModelBound();
        _trianglestrip.setTranslation(-_xSize * 0.5, -_ySize * 0.5, requiredDistance);

		_degentrianglestrip.updateModelBound();
		_degentrianglestrip.setTranslation(-_xSize * 0.5, -_ySize * 0.5, requiredDistance);
		
		this.attachChild(_triangles);
		this.attachChild(_trianglestrip);
		this.attachChild(_degentrianglestrip);
        this.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        
        this.updateGeometricState(0);
        this.updateWorldBound(true);
        this.updateWorldTransform(true);

		_triangles.getSceneHints().setCullHint(CullHint.Inherit);
		_trianglestrip.getSceneHints().setCullHint(CullHint.Always);
		_degentrianglestrip.getSceneHints().setCullHint(CullHint.Always);
	}
	
	/* Private Methods */
    private Mesh createTriangleMesh(){
    	
        final Mesh mesh = new Mesh();
        final MeshData meshData = mesh.getMeshData();

        final FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(_totalSize);
        final FloatBuffer normalBuffer = BufferUtils.createVector3Buffer(_totalSize);
        final FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(_totalSize);

        final ShortBuffer indexBuffer = BufferUtils.createShortBuffer(_totalSize*6);
        
        for (int y = 0; y < _ySize; y++) {
            for (int x = 0; x < _xSize; x++) {
                vertexBuffer.put(x).put(y).put(0);
                normalBuffer.put(0).put(0).put(1);
                textureBuffer.put(x).put(y);
            }
        }
        
        for (int y = 0; y < _ySize - 1; y++) {
            for (int x = 0; x < _xSize; x++) {
                final int index = y * _xSize + x;
                indexBuffer.put((short) index);
                indexBuffer.put((short) (index + _xSize));
                indexBuffer.put((short) (index+1));
                indexBuffer.put((short) (index + _xSize-1)); 	
                indexBuffer.put((short) (index + _xSize));  
                indexBuffer.put((short) index);     
            }
        }

        meshData.setVertexBuffer(vertexBuffer);
        meshData.setNormalBuffer(normalBuffer);
        meshData.setTextureBuffer(textureBuffer, 0);

        meshData.setIndexBuffer(indexBuffer);
        meshData.setIndexMode(IndexMode.Triangles);        

        return mesh;    	
    }
    
    private Mesh createDegenerateStripMesh() {
        final Mesh mesh = new Mesh();
        final MeshData meshData = mesh.getMeshData();

        final FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(_totalSize);
        final FloatBuffer normalBuffer = BufferUtils.createVector3Buffer(_totalSize);
        final FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(_totalSize);
        
        final ShortBuffer indexBuffer = BufferUtils.createShortBuffer((_ySize - 1) * _xSize * 2 + (_ySize - 1) * 2);
        
        for (int y = 0; y < _ySize; y++) {
            for (int x = 0; x < _xSize; x++) {
                vertexBuffer.put(x).put(y).put(0);
                normalBuffer.put(0).put(0).put(1);
                textureBuffer.put(x).put(y);
            }
        }

        for (int y = 0; y < _ySize - 1; y++) {
            for (int x = 0; x < _xSize; x++) {
                final int index = y * _xSize + x;
                indexBuffer.put((short) index);
                indexBuffer.put((short) (index + _xSize));
            }

            final int index = (y + 1) * _xSize;
            indexBuffer.put((short) (index + _xSize - 1));
            indexBuffer.put((short) index);
        }

        meshData.setVertexBuffer(vertexBuffer);
        meshData.setNormalBuffer(normalBuffer);
        meshData.setTextureBuffer(textureBuffer, 0);

        meshData.setIndexBuffer(indexBuffer);
        meshData.setIndexMode(IndexMode.TriangleStrip);

        return mesh;
    }
    
    private Mesh createMultiStripMesh() {
        final Mesh mesh = new Mesh();
        final MeshData meshData = mesh.getMeshData();

        final FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(_totalSize);
        final FloatBuffer normalBuffer = BufferUtils.createVector3Buffer(_totalSize);
        final FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(_totalSize);

        final ShortBuffer indexBuffer = BufferUtils.createShortBuffer((_ySize - 1) * _xSize * 2);
        final int[] indexLengths = new int[_ySize - 1];

        for (int y = 0; y < _ySize; y++) {
            for (int x = 0; x < _xSize; x++) {
                vertexBuffer.put(x).put(y).put(0);
                normalBuffer.put(0).put(0).put(1);
                textureBuffer.put(x).put(y);
            }
        }

        for (int y = 0; y < _ySize - 1; y++) {
            for (int x = 0; x < _xSize; x++) {
                final int index = y * _xSize + x;
                indexBuffer.put((short) index);
                indexBuffer.put((short) (index + _xSize));
            }
            indexLengths[y] = _xSize * 2;
        }

        meshData.setVertexBuffer(vertexBuffer);
        meshData.setNormalBuffer(normalBuffer);
        meshData.setTextureBuffer(textureBuffer, 0);

        meshData.setIndexBuffer(indexBuffer);
        meshData.setIndexLengths(indexLengths);
        meshData.setIndexMode(IndexMode.TriangleStrip);

        return mesh;
    }

    /* Overridden Methods */
    
    public void draw(Renderer renderer){
    	super.draw(renderer);
    	if(first){
    		first=!first;
    		_startTime=System.currentTimeMillis();
    	}
    	
		final long now = System.currentTimeMillis();
		final long dt = now - _startTime;
		if (dt > 10000) {
			final long fps = Math.round(1e3 * _frames / dt);
			System.out.println(fps + " fps "+_meshTypes[_show] + " - "+_renderTypes[_render]);
			
			_startTime = now;
			_frames = 0;
			_show++;
			
			if(_show > 2){
				_show=0;
				_render++;
				
				if(_render>2){
					_render=0;
				}
				
				if(_render==0){
					this.getSceneHints().setDataMode(DataMode.Arrays);
				} else if( _render == 1){
					this.getSceneHints().setDataMode(DataMode.VBO);					
				} else if (_render == 2){
					this.getSceneHints().setDataMode(DataMode.VBOInterleaved);					
				}
			}
			
	    	if(_show==2){	    		
	    		_degentrianglestrip.getSceneHints().setCullHint(CullHint.Inherit);
	    		_trianglestrip.getSceneHints().setCullHint(CullHint.Always);
	    		_triangles.getSceneHints().setCullHint(CullHint.Always);
	    	} else if (_show == 1) {
	    		_trianglestrip.getSceneHints().setCullHint(CullHint.Inherit);
	    		_degentrianglestrip.getSceneHints().setCullHint(CullHint.Always);
	    		_triangles.getSceneHints().setCullHint(CullHint.Always);
	    	} else if (_show == 0){
	    		_triangles.getSceneHints().setCullHint(CullHint.Inherit);
	    		_trianglestrip.getSceneHints().setCullHint(CullHint.Always);
	    		_degentrianglestrip.getSceneHints().setCullHint(CullHint.Always);
	    	} 
		} 
		_frames++;
    }

}
