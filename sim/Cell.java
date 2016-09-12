package pentos.sim;

import java.util.*;

public class Cell implements Comparable <Cell> {

    public enum Type {EMPTY, RESIDENCE, FACTORY, PARK, WATER, ROAD};
    private static final int default_land_side = 50;
    public final int i;  // row
    public final int j;  // column
    public Cell previous; // pointer to another cell, use for graph searches
    protected Type type;

    public Cell(int i, int j) {
	this(i,j,Cell.Type.EMPTY);
    }

    public Cell(int i, int j, Cell previous) {
	this(i,j,Cell.Type.EMPTY);
	this.previous = previous;
    }

    public Cell(int i, int j, Type type) {
	    if (i < 0)
		throw new IllegalArgumentException("Negative row");
	    if (j < 0)
		throw new IllegalArgumentException("Negative column");
	    this.i = i;
	    this.j = j;
	    this.type = type;
	}    

    public boolean isEmpty() { return (type == Type.EMPTY); }
    public boolean isRoad() { return (type == Type.ROAD); }
    public boolean isWater() {return (type == Type.WATER); }
    public boolean isPark() { return (type == Type.PARK); }
    public boolean isFactory() { return (type == Type.FACTORY); }

    public Cell[] neighbors() {
	return neighbors(default_land_side);
    }

    public Cell[] neighbors(int m)
    {
 	int n = (i > 0 ? 1 : 0) + (i < m-1 ? 1 : 0)
	    + (j > 0 ? 1 : 0) + (j < m-1 ? 1 : 0);
	Cell[] points = new Cell [n];
	if (i > 0) points[--n] = new Cell(i - 1, j);
	if (i < m-1) points[--n] = new Cell(i + 1, j);
	if (j > 0) points[--n] = new Cell(i, j - 1);
	if (j < m-1) points[--n] = new Cell(i, j + 1);
	return points;
    }    

    // check if a set of points are connected. Can pass in as set or array
    public static boolean isConnected(Set<Cell> points) {return isConnected(points, default_land_side);}
    public static boolean isConnected(Cell[] points) {return isConnected(points, default_land_side);}
    public static boolean isConnected(Set<Cell> points, int side) {return isConnected(points.toArray(new Cell[points.size()]), side);}
    
    public static boolean isConnected(Cell[] points, int side) {
	if (points == null || points.length == 0)
	    return false;
	// add items in set and check uniqueness
	Set <Cell> open = new HashSet <Cell> ();
	for (Cell p : points)
	    if (p == null || !open.add(p))
		return false;
	// check if single connected component
	Stack <Cell> fringe = new Stack <Cell> ();
	fringe.push(points[0]);
	open.remove(points[0]);
	do {
	    for (Cell p : fringe.pop().neighbors(side))
		if (open.remove(p))
		    fringe.push(p);
	} while (!fringe.empty());
	return open.isEmpty();
    }

    //for simulator to build stuff
    protected void buildRoad() {buildType(Type.ROAD);    }
    protected void buildWater() {buildType(Type.WATER);    }
    protected void buildPark() {buildType(Type.PARK);    }
    protected void buildResidence() {buildType(Type.RESIDENCE);    }
    protected void buildFactory() {buildType(Type.FACTORY);    }

    private void buildType(Type t) {
	if (type != Type.EMPTY) {
	    throw new RuntimeException("Land not empty. Contains " + type);
	}
	this.type = t;
    }

    public boolean isType(Type t) {
	return (this.type == t);
    }

    public boolean equals(Cell p)
    {
	return i == p.i && j == p.j;
    }

    public boolean equals(Object o)
    {
	if (o instanceof Cell)
	    return equals((Cell) o);
	return false;
    }

    public int compareTo(Cell p)
    {
	return i != p.i ? i - p.i : j - p.j;
    }

    public String toString()
    {
	return "(" + i + ", " + j + ")";
    }


    public static String toString(Set<Cell> points) {
	StringBuffer buf = new StringBuffer();
	for (Cell p : points) {
	    buf.append(";");
	    buf.append(p.i + "," + p.j);
	}
	return buf.toString();
    }



    public int hashCode() {
	return i*100+j;
    }

}

