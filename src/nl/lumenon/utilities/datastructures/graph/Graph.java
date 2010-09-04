package nl.lumenon.utilities.datastructures.graph;

import java.util.ArrayList;
import java.util.List;

import nl.lumenon.utilities.datastructures.graph.ElementInterface.ElementType;

/**
 * 
 * @author GJ Roelofs
 */
public class Graph<E, F, G> {

	private String name_;
	protected ArrayList<Element<E>> vertices = new ArrayList<Element<E>>();
	protected ArrayList<Element<F>> edges = new ArrayList<Element<F>>();
	protected ArrayList<Element<G>> areas = new ArrayList<Element<G>>();

	public Graph(String name) {
		name_ = name;
	}

	public ArrayList getElements(ElementType type) {
		switch (type) {
		case Vertex:
			return vertices;
		case Edge:
			return edges;
		case Area:
			return areas;
		}
		return null;
	}

	public int getSize(ElementType type){
		if(type == null){
			return vertices.size()+edges.size()+areas.size();
		}
		
		switch (type) {
		case Vertex:
			return vertices.size();
		case Edge:
			return edges.size();
		case Area:
			return areas.size();
		}
		
		return 0;
	}
	
	/**
	 * Returns the original element if the element was not in the Domain of the
	 * Graph, or the equivalent Element, if it was.
	 * 
	 * @param element
	 *            The Element to add to the Graph.
	 * @return Parameter if element does not occur in Graph, Domain Element if
	 *         it is
	 */
	public Element addElement(Element element) {
		switch (element.getType()) {
		case Vertex:
			if (!vertices.contains(element)) {
				vertices.add(element);
			} else {
				return vertices.get(vertices.indexOf(element));
			}
			break;
		case Edge:
			if (!edges.contains(element)) {
				edges.add(element);
			} else {
				return edges.get(edges.indexOf(element));
			}
			break;
		case Area:
			if (!areas.contains(element)) {
				areas.add(element);
			} else {
				return areas.get(areas.indexOf(element));
			}
			break;
		}
		return element;
	}

	public String getName_() {
		return name_;
	}

	public void setName_(String name) {
		name_ = name;
	}
}
