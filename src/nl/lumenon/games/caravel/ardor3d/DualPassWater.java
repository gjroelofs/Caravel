package nl.lumenon.games.caravel.ardor3d;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.ApplyMode;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix4;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.Vector4;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.TextureManager;

public final class DualPassWater extends Node
{
	/**
	 * All variables regarding the Water animation, texture, place, animation speed and alpha.
	 * wt = Water Top, wb = Water Bottom
	 */
	private Quad wtQuad_, wbQuad_; 
	private Texture wtTexture_, wbTexture_; 
	private Matrix4 wtMatrix_ = new Matrix4(), wbMatrix_ = new Matrix4();
	private TextureState wtTextureState_,wbTextureState_;
	private float wtXSpeed_ = 0.0217f, wtYSpeed_ = 0f,	wbXSpeed_ = 0f, wbYSpeed_ = 0.04f;
	private float wtAlpha_ = 0.85f, wbAlpha_ = 1f;
	
	public DualPassWater() {}
	
	public DualPassWater(Image img, Image img2, String name,float sizeX,float sizeY)
	{	
		this(TextureManager.loadFromImage(img,	MinificationFilter.BilinearNearestMipMap),TextureManager.loadFromImage(img2,MinificationFilter.BilinearNearestMipMap), name, sizeX, sizeY);
	}
	
	public DualPassWater(Texture t, Texture t2, String name, float sizeX, float sizeY){
		super(name);
		/* Setup Quads */
		wtQuad_ = new Quad("Water Quad Top", sizeX, sizeY);  
		wbQuad_ = new Quad("Water Quad Bottom", sizeX, sizeY); 
		
		// Initialize TextureStates, and flip Quads.
		wtTextureState_ = new TextureState(); 
		flipQuad(wtQuad_); 
		
		wbTextureState_ = new TextureState();
		flipQuad(wbQuad_);

		
		setTextures(t,t2);		
		
		CullState cs = new CullState();
		ZBufferState zs = new ZBufferState();
		zs.setEnabled(true);
		zs.setWritable(false);
		cs.setCullFace(CullState.Face.Front);
		
		// Make our quad transparent and assign the TextureState
		wtQuad_.setRenderState(makeAlphaForTransparency());
		wtQuad_.setRenderState(wtTextureState_);
		
		// We need to make sure the quad is rendered after the land. QUEUE
		// TRANSPARENT!
		//waterQuad.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
		wtQuad_.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		wtQuad_.setRenderState(cs);
		wtQuad_.setRenderState(zs);
		
		
		// Make our quad transparent and assign the TextureState
		wbQuad_.setRenderState(makeAlphaForTransparency());
		wbQuad_.setRenderState(wbTextureState_);
		
		// We need to make sure the quad is rendered after the land. QUEUE
		// TRANSPARENT!
		//waterQuad2.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
		wbQuad_.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		wbQuad_.setRenderState(cs);
		wbQuad_.setRenderState(zs);

		// make sure we have a bounding box!
		wtQuad_.setModelBound(new BoundingBox());
		wbQuad_.setModelBound(new BoundingBox());
		
		// set the transparency factors for the quads
		setAlphaFactors(wtAlpha_,wbAlpha_);		
		
		// The usual
		wtQuad_.updateModelBound();
		wbQuad_.updateModelBound();

		// attach the quads as children of this node
		attachChild(wtQuad_);
		attachChild(wbQuad_);
		
		wtQuad_.updateWorldRenderStates(true);
		wtQuad_.updateWorldTransform(true);
		wbQuad_.updateWorldRenderStates(true);
		wbQuad_.updateWorldTransform(true);
	}
	
	private void setTextureScale(Vector3 scaleVec)
	{

		// Scale our textures to make them look better, along with wrapping so they
		// don't "fall off" the quad.
		double scale = (scaleVec.getX() * wtQuad_.getWidth() + scaleVec.getZ() * wtQuad_.getHeight()) / 200;
		
		if(scale < 1.0f) scale = 1.0f;
		scale=10f;
		wtMatrix_ = new Matrix4(wtTexture_.getTextureMatrix());
		wtMatrix_.scaleLocal(new Vector4(scale,scale,1,1));
		wtMatrix_.set(wtMatrix_);
		wtTexture_.setTextureMatrix(wtMatrix_);
		
		wbMatrix_ = new Matrix4(wbTexture_.getTextureMatrix());
		if(wbMatrix_ == null){
			wbMatrix_ = new Matrix4();					
        }
		
		wbMatrix_.scaleLocal(new Vector4(scale,scale,1,1));
		wbTexture_.setTextureMatrix(wbMatrix_);
		
		wtQuad_.setTranslation(0,0.1f/scaleVec.getY(),0);
	}
	
	private BlendState makeAlphaForTransparency() 
	{
		BlendState bs = new BlendState();
		bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
		bs.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
		bs.setTestEnabled(true);
		bs.setTestFunction(BlendState.TestFunction.Always);
		bs.setEnabled(true);
		bs.setBlendEnabled(true);
		return bs;
	}

	private void flipQuad(Spatial s) 
	{
		Quaternion rotQuad = new Quaternion();
		rotQuad = rotQuad.fromAngleNormalAxis(Math.PI * .5f, Vector3.UNIT_X);
		s.setRotation(rotQuad);
	}
	
	public void setTopSpeedFactor(float xSpeed, float ySpeed){
		this.wtXSpeed_ = Math.abs(xSpeed);
		this.wtYSpeed_ = Math.abs(ySpeed);
	}
	public void setBottomSpeedFactor(float xSpeed, float ySpeed){
		this.wbXSpeed_ = Math.abs(xSpeed);
		this.wbYSpeed_ = Math.abs(ySpeed);
	}

	public void setTextures(Texture texTop,Texture texBottom)
	{
		if(texTop != null)
		{
			this.wtTexture_ = texTop.createSimpleClone();
			// Assign our Textures to our TextureState
			this.wtTextureState_.setTexture(this.wtTexture_);
			wtTexture_.setWrap(WrapMode.Repeat);
			wtTexture_.setApply(ApplyMode.Modulate);
			// Avoids NPE's when we do our first update!
			wtTexture_.setTextureMatrix(new Matrix4());			
		}
		if(texBottom != null)
		{
			this.wbTexture_ = texBottom.createSimpleClone();
			this.wbTextureState_.setTexture(this.wbTexture_);
			wbTexture_.setWrap(WrapMode.Repeat);
			wbTexture_.setApply(ApplyMode.Modulate);
			wbTexture_.setTextureMatrix(new Matrix4());
		}
		
		setTextureScale((Vector3) getWorldScale());
	}
	
	public void setAlphaFactors(float alphaTop,float alphaBottom)
	{
		wtQuad_.setSolidColor(new ColorRGBA(1, 1, 1, alphaTop));
		wbQuad_.setSolidColor(new ColorRGBA(1, 1, 1, alphaBottom));
	}
	
	@Override
	public void updateGeometricState(double time, boolean initiator) 
	{
		updateWater(time);
		super.updateGeometricState(time, initiator);		
	}

	private void updateWater(double tpf) 
	{		
		// These are to keep the float from getting too big. They offer the
		// possibility of a jump, but nobody should notice.
		if (wtMatrix_.getValue(3, 0) > 1000){
			wtMatrix_.setValue(3, 0, 0);
		}
		if (wtMatrix_.getValue(3, 1) > 1000){
			wtMatrix_.setValue(3, 1, 0);
		}
		if (wbMatrix_.getValue(3, 0) > 1000){
			wbMatrix_.setValue(3, 0, 0);
		}
		if (wbMatrix_.getValue(3, 1) > 1000){
			wbMatrix_.setValue(3, 1, 0);
		}
		// Moves our water, nice and slowly.
		wtMatrix_.setValue(3, 0, wtMatrix_.getValue(3, 0)+(wtXSpeed_*tpf));
		wtMatrix_.setValue(3, 1, wtMatrix_.getValue(3, 1)+(wtYSpeed_*tpf));
		wtTexture_.setTextureMatrix(wtMatrix_);

		wbMatrix_.setValue(3, 0, wbMatrix_.getValue(3, 0)+(wbXSpeed_*tpf));
		wbMatrix_.setValue(3, 1, wbMatrix_.getValue(3, 1)+(wbYSpeed_*tpf));
		wbTexture_.setTextureMatrix(wbMatrix_);

		wtQuad_.updateWorldRenderStates(true);
		wbQuad_.updateWorldRenderStates(true);
	}
}
