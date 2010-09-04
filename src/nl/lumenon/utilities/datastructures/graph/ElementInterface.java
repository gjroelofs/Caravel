package nl.lumenon.utilities.datastructures.graph;

import java.util.ArrayList;

/**
 * 
 * @author GJ Roelofs
 */
public interface ElementInterface<E> {

	public enum ElementType {
		Vertex, Edge, Area
	};

	void addObject(E e);

	void addAdjacent(ElementInterface e);

	void removeAdjacent(ElementInterface e);

	void removeObject(E e);

	void destroy();

	double[] getLocation();

	ArrayList<E> getObjects();

	ArrayList getAdjacent(ElementType type);

	ElementType getType();

}
