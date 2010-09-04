/**
 * 
 */
package nl.lumenon.games.caravel.ardor3d;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import com.ardor3d.extension.android.AndroidImage;
import com.ardor3d.extension.android.AndroidImageLoader;
import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.ImageDataType;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.visitor.Visitor;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.geom.GeometryTool;
import com.ardor3d.util.geom.MeshCombiner;
import com.ardor3d.util.geom.GeometryTool.MatchCondition;

import nl.lumenon.games.caravel.R;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;

/**
 * @author Gijs-Jan Roelofs
 * 
 */
public class ModelLibrary {
	
	/** Enums */
	public static enum ModelType {
		Area, Placable, Rest
	};
	
	/** Variables */
	/* Static Variables */

	/* Instance Variables */
	AssetManager assets_;
	Resources resources_;
	AndroidImageLoader loader_;
	String modelLoc_, texLoc_, textureSuffix_, defaultTheme_;
	String[] themes_, modelTypeInfix_;
	HashMap<String, SoftReference<Image>> textureCache_ = new HashMap<String, SoftReference<Image>>();
	HashMap<String, SoftReference<Node>> nodeCache_ = new HashMap<String, SoftReference<Node>>();
	
	/** Constructors */
	
	public ModelLibrary(AssetManager assets, Resources resources) {
		assets_ = assets;
		resources_ = resources;

		setupLibrary();
	}

	/** Methods */
	
	/* Public Methods */

	public Node loadModel(String theme, String name, ModelType type) {
		String place = modelTypeInfix_[type.ordinal()];

		String fileName = modelLoc_ + "/" + theme + "_" + place + "_" + name
				+ ".bin";
		Node outputNode = null;
		if (nodeCache_.get(fileName) != null) {
			if (nodeCache_.get(fileName).get() != null) {
				outputNode = nodeCache_.get(fileName).get();
			}
		}
		if (outputNode == null) {
			try {
				outputNode = (Node) BinaryImporter.getInstance().load(
						assets_.open(fileName));
			} catch (final NotFoundException e) {
				e.printStackTrace();
				// Load default model if you can't find the other one;
				try {
					outputNode = (Node) BinaryImporter.getInstance().load(
							assets_.open(modelLoc_ + "/" + defaultTheme_ + "_"
									+ place + "_" + name + ".bin"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}

			outputNode.acceptVisitor(new CleanUpVisitor(), false);
			nodeCache_.put(fileName, new SoftReference<Node>(outputNode));
		}

		TextureState ts = new TextureState();
		ts.setTexture(TextureManager.loadFromImage(
				loadImage(theme, name, type),
				MinificationFilter.BilinearNearestMipMap));

		outputNode.setRenderState(ts);

		return outputNode;
	}

	public Image loadImage(String theme, String name, ModelType type) {
		String place = modelTypeInfix_[type.ordinal()];

		Image outputImage = null;
		if (textureCache_.get(theme + "-" + name) != null) {
			if (textureCache_.get(theme + "-" + name).get() != null) {
				outputImage = textureCache_.get(theme + "-" + name).get();
			}
		}
		if (outputImage == null) {
			outputImage = getImageFromAsset(texLoc_ + "/" + theme + "_" + place
					+ "_" + name + "_" + textureSuffix_ + ".png", true);
			if (outputImage == null) {
				outputImage = getImageFromAsset(texLoc_ + "/" + defaultTheme_
						+ "_" + place + "_" + name + "_" + textureSuffix_
						+ ".png", true);
			}

			textureCache_.put(theme + "-" + name, new SoftReference<Image>(
					outputImage));
		}

		return outputImage;
	}

	/* Private Methods */

	private void setupLibrary() {
		themes_ = resources_.getStringArray(R.array.theme_names);
		modelTypeInfix_ = resources_.getStringArray(R.array.model_types);
		modelLoc_ = resources_.getString(R.string.model_loc);
		texLoc_ = resources_.getString(R.string.texture_loc);
		textureSuffix_ = resources_.getString(R.string.texture_suffix);
		defaultTheme_ = resources_.getString(R.string.default_theme);
	}

	/* Utility Methods */

	public void destroy() {
		resources_ = null;
		assets_=null;
		loader_=null;
		textureCache_.clear();
		textureCache_=null;
		nodeCache_.clear();
		nodeCache_=null;
	}

	public Image getImageFromResources(final int drawableId, final Resources resources, final boolean flipped) {
		// final Drawable draw = resources.getDrawable(drawableId);
		// if (draw instanceof BitmapDrawable) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		Bitmap bitmap = BitmapFactory.decodeResource(resources, drawableId,
				options);
		if (loader_ == null) {
			loader_ = new AndroidImageLoader();
		}
		Options op = new Options();

		return loader_.loadFromBitMap(bitmap, flipped, op);
		// }
	}

	public Image getImageFromAsset(String asset, final boolean flipped) {
		Image output = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(assets_.open(asset), null,
					options);
			if (loader_ == null) {
				loader_ = new AndroidImageLoader();
			}
			Options op = new Options();
			output = loader_.loadFromBitMap(bitmap, flipped, op);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	/* Static Methods */

	public Mesh compileHeterogenousNodes(HashMap<String, Node> nodes,
			String theme, ModelType type, Handler handler) {
		int elements = 0;

		String[] keySet = new String[nodes.keySet().size()];
		keySet = nodes.keySet().toArray(keySet);

		Node compileNode = new Node();
		for (int i = 0; i < keySet.length; i++) {

			nodes.get(keySet[i]).updateGeometricState(0);
			compileNode.attachChild(MeshCombiner.combine(nodes.get(keySet[i])));

			handler.sendMessage(handler.obtainMessage(0, elements++, 0,
					"Compiling Tiles:" + keySet[i]));
		}

		handler.sendMessage(handler.obtainMessage(0, elements++, 0,
				"Mesh Combine"));
		Mesh finalCompiled = TextureCombiner.combine(compileNode);
		handler.sendMessage(handler.obtainMessage(0, elements++, 0,
				"Texture Combine"));
		TextureState ts = new TextureState();
		ts.setTexture(TextureManager.loadFromImage(compileAndroidImages(keySet,theme,type),
				MinificationFilter.BilinearNearestMipMap));
		finalCompiled.setRenderState(ts);

		return finalCompiled;
	}

	public Image compileAndroidImages(String[] images, String theme, ModelType type) {

		AndroidImage img = (AndroidImage) loadImage(theme, images[0],type);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;

		Bitmap holder = Bitmap.createBitmap(img.getWidth()
				* images.length, img.getHeight(), img.getBitmap(0).getConfig());
		
		Canvas canvas = new Canvas(holder);
		for (int k = 0; k < images.length; k++) {
			img = (AndroidImage) loadImage(theme, images[(images.length-1)-k],type);
			canvas.drawBitmap(img.getBitmap(0),img.getWidth()*k, 0, null);
		}
		
		Options op = new Options();
		AndroidImageLoader _loader = new AndroidImageLoader();
		return _loader.loadFromBitMap(holder, false, op);

	}

	/* Inner Classes */

	public class CleanUpVisitor implements Visitor {

		@Override
		public void visit(Spatial s) {
			if (s instanceof Mesh) {
				GeometryTool.minimizeVerts((Mesh) s, EnumSet
						.allOf(MatchCondition.class));
			}

			s.clearRenderState(StateType.Material);
			s.clearRenderState(StateType.Light);
			s.clearRenderState(StateType.Texture);
			s.clearRenderState(StateType.Shading);
			s.clearRenderState(StateType.Blend);
			s.clearRenderState(StateType.Clip);
			s.clearRenderState(StateType.ColorMask);
			s.clearRenderState(StateType.Cull);
			s.clearRenderState(StateType.Fog);
			s.clearRenderState(StateType.FragmentProgram);
			s.clearRenderState(StateType.GLSLShader);
			s.clearRenderState(StateType.Offset);
			s.clearRenderState(StateType.Stencil);
			s.clearRenderState(StateType.VertexProgram);
			s.clearRenderState(StateType.Wireframe);
			s.clearRenderState(StateType.ZBuffer);
		}

	}

}
