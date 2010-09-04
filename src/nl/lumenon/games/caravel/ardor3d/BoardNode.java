/**
 * 
 */
package nl.lumenon.games.caravel.ardor3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import nl.lumenon.games.caravel.Caravel;
import nl.lumenon.games.caravel.ardor3d.BaseScene.SceneSetupVisitor;
import nl.lumenon.games.caravel.ardor3d.ModelLibrary.ModelType;
import nl.lumenon.utilities.datastructures.graph.Element;
import nl.lumenon.utilities.datastructures.graph.Graph;
import nl.lumenon.utilities.datastructures.graph.ElementInterface.ElementType;

import android.os.Handler;
import android.os.Message;

import com.ardor3d.image.Image;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.DataMode;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.geom.ClonedCopyLogic;
import com.ardor3d.util.geom.MeshCombiner;
import com.ardor3d.util.geom.SceneCopier;
import com.ardor3d.util.scenegraph.CompileOptions;
import com.ardor3d.util.scenegraph.RenderDelegate;
import com.ardor3d.util.scenegraph.SceneCompiler;

/**
 * @author Gijs-Jan Roelofs
 *
 */
public class BoardNode extends Node{

	/* Static Variables */
	/* Instance Variables */
    private Node _areasPick=new Node("Area Picking"), _edgesPick=new Node("Edge Picking"), _vertPick=new Node("Vertex Picking");
	    
	public BoardNode(ModelLibrary lib, Handler handler, Graph board){
		        
		constructPickNodes(handler, board);
		constructBoard(lib, handler, board);		
		
		// Center Board because we are EXTREMELY LAZY..
		this.updateWorldBound(true);
		Vector3 center = new Vector3();
		this.getWorldBound().getCenter().multiply(-1, center);
		center.setY(0);
		this.setTranslation(center);
	}
	
	/* Public Methods */

	public PickResults doPick(ElementType type, Ray3 pickRay) {
		Node search=null;
		
		switch(type){
			case Vertex:search=_areasPick;break;
			case Edge:search=_areasPick;break;
			case Area:search=_areasPick;break;
		}
		
		final PrimitivePickResults pickResults = new PrimitivePickResults();
		pickResults.setCheckDistance(true);
		
		try {
			PickingUtil.findPick(search, pickRay, pickResults);
		} catch (Exception e) {
			System.err.println("MainInit.doPick Error: " +e.getMessage());
			e.printStackTrace();
		}
		
		return pickResults;
	}
	
	/* Private Methods */
	
	private void constructPickNodes(Handler handler, Graph board){
		handler.sendMessage(handler.obtainMessage(0, board.getSize(null), Caravel.SET_TOTAL, "Setting up Picking Nodes"));
		
		int elements = 1; 
		ArrayList<Element> elementList = board.getElements(ElementType.Area);
		for(Element element:elementList){
			PickHolder box = new PickHolder("Tile:"+element.getName(), element);
			box.setScale(2);
			box.setDefaultColor(ColorRGBA.ORANGE);
			double[] xy = element.getLocation();
			box.setTranslation(xy[0], 1, xy[1]);
			_areasPick.attachChild(box);
			
			handler.sendMessage(handler.obtainMessage(0, elements, 0, "Creating Tiles:"+elements));
    		elements++;
		}
		
		elementList = board.getElements(ElementType.Edge);
		for(Element element:elementList){
			PickHolder box = new PickHolder("Edge:"+element.getName(), element);
			box.setScale(1);
			box.setDefaultColor(ColorRGBA.RED);
			double[] xy = element.getLocation();
			box.setTranslation(xy[0], 1, xy[1]);
			_edgesPick.attachChild(box);

			handler.sendMessage(handler.obtainMessage(0, elements, 0, "Creating Edges:"+elements));
    		elements++;
		}
		

		elementList = board.getElements(ElementType.Vertex);
		for(Element element:elementList){
			PickHolder box = new PickHolder("Vertex:"+element.getName(), element);
			box.setScale(1);
			double[] xy = element.getLocation();
			box.setTranslation(xy[0], 1, xy[1]);
			_vertPick.attachChild(box);

			handler.sendMessage(handler.obtainMessage(0, elements, 0, "Creating Vertices:"+elements));
    		elements++;
		}

		handler.sendMessage(handler.obtainMessage(0, 0, Caravel.NEXT_TOTAL, "Picking Nodes Complete"));		
	}
	
	private void constructBoard(ModelLibrary lib, Handler handler, Graph board){

		handler.sendMessage(handler.obtainMessage(0, board.getSize(ElementType.Area), Caravel.SET_TOTAL, "Setting up Board"));
		
		int elements=0;
		ArrayList<Element> elementList = board.getElements(ElementType.Area);
		HashMap<String, Node> compileTileList = new HashMap<String, Node>();
		for(Element element:elementList){
			if(!element.getName().toLowerCase().equals("water")){
				Node tile = lib.loadModel("lumenon", element.getName().toLowerCase(), ModelType.Area).makeCopy(true);
				double[] xy = element.getLocation();
				tile.setTranslation(xy[0], 0.2, xy[1]);
				
				//if(element.getName().toLowerCase().equals("grain") || element.getName().toLowerCase().equals("stone")){
					Node compileNode = compileTileList.get(element.getName().toLowerCase());
					if(compileNode == null){
						compileNode = new Node(element.getName().toLowerCase());
						compileTileList.put(element.getName().toLowerCase(), compileNode);
					}
					compileNode.attachChild(tile);
				//} else {
					//this.attachChild(tile);
				//}
				handler.sendMessage(handler.obtainMessage(0, elements, 0, "Creating Tile:"+element.getName()));
			}
    		elements++;
		}
		handler.sendMessage(handler.obtainMessage(0, 0, Caravel.NEXT_TOTAL, "Board Init Complete"));	
		handler.sendMessage(handler.obtainMessage(0, compileTileList.keySet().size()+1, Caravel.SET_TOTAL, "Compiling Board"));
		
		Mesh compiled = lib.compileHeterogenousNodes(compileTileList, "lumenon", ModelType.Area, handler);
		System.out.println("Vertex:"+compiled.getMeshData().getTotalPrimitiveCount()+ " - "+compiled.getMeshData().getVertexCount());
		//compiled.getSceneHints().setDataMode(DataMode.VBOInterleaved);
		//compiled.acceptVisitor(new SceneSetupVisitor(true), false);
		
		System.out.println("Mesh formed from index modes: " +Arrays.toString(compiled.getMeshData().getIndexModes()));
		
		this.attachChild(compiled);
		
		handler.sendMessage(handler.obtainMessage(0, 0, Caravel.NEXT_TOTAL, "Compile Complete"));	
			
	}
	
	/* Overridden Methods */	
	
	public void draw(Renderer renderer){
		super.draw(renderer);	
	}

	public void destroy(){
		this.detachAllChildren();
		
		_areasPick.detachAllChildren();
		_areasPick=null;
		
		_edgesPick.detachAllChildren();
		_edgesPick=null;
		
		_vertPick.detachAllChildren();
		_vertPick=null;		
	}
	
	public class PickHolder extends Box {
		private Element element_;
		
		public PickHolder(String name, Element element){
			super(name);
			element_=element;
		}
	}
}
