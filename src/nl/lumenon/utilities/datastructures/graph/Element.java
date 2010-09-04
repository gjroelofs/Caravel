package nl.lumenon.utilities.datastructures.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import nl.lumenon.utilities.datastructures.graph.ElementInterface.ElementType;

/**
 * 
 * @param <E>
 * @author GJ Roelofs
 */
public class Element<E> implements ElementInterface<E>, Cloneable {

	protected double[] xy=new double[2];
	protected ElementType type;
	protected String name;
	protected ArrayList<E> objects = new ArrayList<E>();
	protected ArrayList<Element> vertices = new ArrayList<Element>();
	protected ArrayList<Element> edges = new ArrayList<Element>();
	protected ArrayList<Element> areas = new ArrayList<Element>();

	public Element(String name, ElementType type, List<Element> elements){
		this.type=type;
		this.name=name;
		
		switch(type){
		case Vertex:{
			/* Vertex Creation */			
			System.out.println("A vertex should only be instantiated with it's Type/Location Constructor");
			break;}
		case Edge:{
			/* Edge Creation */					
			Element a = elements.get(0), b = elements.get(1);	
			
			Assert.assertTrue("Both elements should be Vertices",(a.getType().equals(ElementType.Vertex) && b.getType().equals(ElementType.Vertex)));
							
			this.addAdjacent(a);
			this.addAdjacent(b);

			for (Element e : (ArrayList<Element>) a.getAdjacent(ElementType.Edge)) {
				if (!e.equals(this)) {
					e.addAdjacent(this);
					this.addAdjacent(e);
				}
			}
			for (Element e : (ArrayList<Element>) b.getAdjacent(ElementType.Edge)) {
				if (!e.equals(this)) {
					e.addAdjacent(this);
					this.addAdjacent(e);
				}
			}

			a.addAdjacent(b);
			a.addAdjacent(this);
			b.addAdjacent(a);
			b.addAdjacent(this);

			updateLocation();
			break;}
		case Area:
			/* Area Creation */						
 			for (int i = 0; i < elements.size(); i++) {
 				Assert.assertTrue("An area must be constructed from edges", (elements.get(i).getType().equals(ElementType.Edge)));
 				
				this.addAdjacent(elements.get(i));
			}
			for (int i = 0; i < edges.size(); i++) {
				// Add this area to all edges
				edges.get(i).addAdjacent(this);
				ArrayList<Element> verts = edges.get(i).getAdjacent(ElementType.Vertex);
				for (int j = 0; j < verts.size(); j++) {
					Element v = verts.get(j);
					// Connect to Vertices in Edges
					this.addAdjacent(v);
					v.addAdjacent(this);
				}
	
				ArrayList<Element> areas = edges.get(i).getAdjacent(ElementType.Area);
				for (int j = 0; j < areas.size(); j++) {
					Element v = areas.get(j);
					if (!v.equals(this)) {
						// Connect to Areas in Edges
						this.addAdjacent(v);
						v.addAdjacent(this);
					}
				}
			}
			updateLocation();
			break;
		}
	}

	/**
	 * Convenience constructor for Vertices
	 * @param xy The XY array used to construct the Vertex
	 */
	public Element(String name, double[] xy){			
		this.type=ElementType.Vertex;
		this.name=name;
		
		this.xy[0]=round(xy[0], 4);
		this.xy[1]=round(xy[1], 4);
	}
	
	/**
	 * Convenience constructor for Edges
	 * @param start Starting Vertex of the Edge
	 * @param end Ending Vertex of the Edge
	 */
	public Element(String name, Element start, Element end){
		this(name, ElementType.Edge,Arrays.asList(start,end));		
	}

	/**
	 * Convenience constructor for Areas
	 * @param List<Element> a List containing Edges
	 */
	public Element(String name, List<Element> elements){
		this(name, ElementType.Area,elements);		
	}
	
	/** Convenience constructor for clone 
	 * 
	 */
	private Element(){}
	
	/**
	 * Adds an Object of class E to this Element.
	 * 
	 * @param e
	 *            Object of class E.
	 */
	public void addObject(E e) {
		this.objects.add(e);
	}

	/**
	 * Adds an element to this element, making the param element adjacent to
	 * this one. If the element is already adjacent, it is not added.
	 * 
	 * @param e
	 *            Element to be added to the adjacency list.
	 */
	public void addAdjacent(ElementInterface e) {
		switch (e.getType()) {
		case Vertex:
			if (!this.vertices.contains(e)) {
				this.vertices.add((Element) e);
				Assert.assertTrue("Edge contains too many Vertices", !(this.getType()==ElementType.Edge && this.vertices.size()>2));
			}
			break;
		case Edge:
			if (!this.edges.contains(e)) {
				this.edges.add((Element) e);
			}
			break;
		case Area:
			if (!this.areas.contains(e)) {
				this.areas.add((Element) e);
			}
			break;
		}
	}

	/**
	 * Removes an adjacent Element from this Element.
	 * 
	 * @param e
	 *            Element to remove
	 */
	public void removeAdjacent(ElementInterface e) {
		switch (e.getType()) {
		case Vertex:
			this.vertices.remove(e);
			break;
		case Edge:
			this.edges.remove(e);
			break;
		case Area:
			this.areas.remove(e);
			break;
		}
	}

	/**
	 * Removes the specificied object from this Element.
	 * 
	 * @param e
	 *            Object of class E to remove.
	 */
	public void removeObject(E e) {
		this.objects.remove(e);
	}

	/**
	 * Returns the location of this Element.
	 * 
	 * @return Double array, size 2. Containing X and then Y.
	 */
	public double[] getLocation() {
		return xy;
	}

	/**
	 * Returns a list of Objects associated with this Element<E>, all of type E
	 * 
	 * @return ArrayList containing all objects associated with this Element, of
	 *         class E
	 */
	public ArrayList<E> getObjects() {
		return objects;
	}

	/**
	 * Returns adjacent types to this Element within the Graph.</br></br> The
	 * term adjacent is different from the Graph Theory definition, and should
	 * be explained in each implementation/extension.</br> If the type is equal
	 * to the class type, the objects returned are adjacent to this
	 * element.</br> If the type is not equal, the objects are <b>connected</b>
	 * to this element. Meaning this element is a part of the returned
	 * element.</br>
	 * 
	 * @param type
	 *            Specifies the type of adjacent elements to return.</br>
	 * @return ArrayList containing the type specified, adjacent to this
	 *         Element.</br>
	 */
	public ArrayList getAdjacent(ElementType type) {
		ArrayList output = null;
		switch (type) {
		case Vertex:
			output = vertices;
			break;
		case Edge:
			output = edges;
			break;
		case Area:
			output = areas;
			break;
		}
		return output;
	}
		
	/**
	 * @param o
	 * @return
	 * @see java.util.ArrayList#contains(java.lang.Object)
	 */
	public boolean containsObject(Object o) {
		return objects.contains(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAllObjectsl(Collection<?> c) {
		return objects.containsAll(c);
	}

	public void destroy() {
		for (Element v : vertices) {
			v.removeAdjacent(this);
		}
		for (Element e : edges) {
			e.removeAdjacent(this);
		}
		for (Element ar : areas) {
			ar.removeAdjacent(this);
		}
		
		vertices.clear();
		edges.clear();
		areas.clear();
		
		objects.clear();
	}
	
	/**
	 * Called when a location altering element is added. 
	 * (Edge -> Vertex, Area -> Edge, Vertex)
	 * Updates the location of this element.
	 */
	public void updateLocation() {
		switch(this.type){
			case Edge:{
				xy = new double[2];
				ArrayList<Element> list = getAdjacent(ElementType.Vertex);
				for (int i = 0; i < list.size(); i++) {
					double[] xyb = list.get(i).getLocation();
					xy[0] += xyb[0];
					xy[1] += xyb[1];
				}
				xy[0] = (double) (xy[0] / list.size());
				xy[1] = (double) (xy[1] / list.size());
			}
			;break;
			case Area:{
				xy = new double[2];
				ArrayList<Element> list = getAdjacent(ElementType.Edge);
				for (int i = 0; i < list.size(); i++) {
					List<Element> tempList = ((Element) list.get(i)).getAdjacent(ElementType.Vertex);
					double[][] xyb = {tempList.get(0).getLocation(),tempList.get(1).getLocation()};
					xy[0] += xyb[0][0] + xyb[1][0];
					xy[1] += xyb[0][1] + xyb[1][1];
				}
				xy[0] = (double) (xy[0] / (list.size() * 2));
				xy[1] = (double) (xy[1] / (list.size() * 2));
			}
			;break;
		}

		this.xy[0]=round(xy[0], 4);
		this.xy[1]=round(xy[1], 4);
	}

	public ElementType getType(){
		return this.type;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean equals(Object o){
		switch(type){
			case Vertex:{
				if(o instanceof Element){
					Element b = (Element) o;
					if(b.type.equals(type)){
						if (Arrays.equals(this.xy, b.xy)) {
							return true;
						} 
					} 
				}
				return false;
			}
			case Edge:{
				if (o instanceof Element) {
					Element b = (Element) o;		
					if(b.type.equals(type)){
						if (vertices.contains(b.vertices.get(0))
								&& vertices.contains(b.vertices.get(1))) {
							return true;
						}
					}
				} else if (o instanceof Element[]) {
					// Dealing with a set of Vertices
					Element[] b = (Element[]) o;
					if(b[0].type.equals(ElementType.Vertex) && b[1].type.equals(ElementType.Vertex)){
						if (vertices.contains(b[0]) && vertices.contains(b[1])) {
							return true;
						}
					}
				}
				return false;
			}
			case Area:{
				if (o instanceof Element) {
					Element a = (Element) o;
					if(a.type.equals(type)){
						for (Element ed : (ArrayList<Element>) a.edges) {
							if (!edges.contains(ed)) {
								return false;
							}
						}
						return true;
					}
					return false;
				} else if (o instanceof List) {
					// Dealing with a list of Edges
					List<Element> e = (List<Element>) o;
					for (Element ed : e) {
						if(ed.type.equals(ElementType.Edge)){
							if (!edges.contains(ed)) {
								return false;
							}
						} else{
							return false;
						}
					}
					return true;
				}
			}
		}
		return super.equals(o);
	}	
	
	@Override
	/**
	 * Returns a clone of the Element, but adjacent Elements are not added!
	 * (Rather useless)
	 */
	public Object clone(){
		Element<E> output = new Element<E>();
		output.type=this.type;
		output.xy=this.xy;

		return output;		
	}
	
	 public static double round(double value, int decimalPlace)
	    {
	      double power_of_ten = 1;
	      // floating point arithmetic can be very tricky.
	      // that's why I introduce a "fudge factor"
	      double fudge_factor = 0.05;
	      while (decimalPlace-- > 0) {
	         power_of_ten *= 10.0d;
	         fudge_factor /= 10.0d;
	      }
	      return Math.round((value + fudge_factor)* power_of_ten)  / power_of_ten;
	    }

}
