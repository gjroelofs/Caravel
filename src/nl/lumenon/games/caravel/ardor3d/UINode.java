/**
 * 
 */
package nl.lumenon.games.caravel.ardor3d;

import android.os.Handler;

import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.renderer.state.ZBufferState.TestFunction;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Quad;

/**
 * @author Gijs-Jan Roelofs
 *
 */
public class UINode extends Node{

	/* Static Variables */
	/* Instance Variables */

	public UINode(Handler handler, int width, int height){
		
		System.out.println("Creating UI:"+width+"-"+height);
		
		Quad quad = new Quad("", width, height*0.1);
		quad.setTranslation(width/2.0, (height*0.1)/2, 0);
		quad.getSceneHints().setRenderBucketType(RenderBucketType.Ortho);
		quad.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		this.attachChild(quad);
		
		ZBufferState zs = new ZBufferState();
		zs.setEnabled(true);
		zs.setWritable(false);
		zs.setFunction(TestFunction.Always);
		
		this.setRenderState(zs);
		
		this.updateWorldRenderStates(true);
		this.updateGeometricState(0);
		this.updateWorldTransform(true);
		
	}

}
