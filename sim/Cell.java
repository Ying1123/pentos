package pentos.sim;

import java.util.*;

public class Cell implements Comparable <Cell> {

    public enum landType {EMPTY, BUILDING, PARK, WATER, ROAD};

    public final int i;  // row
    public final int j;  // column
    protected landType t;

    public Cell(int i, int j) {
	this(i,j,landType.EMPTY);
    }

    public Cell(int i, int j, landType t)
	{
	    if (i < 0)
		throw new IllegalArgumentException("Negative row");
	    if (j < 0)
		throw new IllegalArgumentException("Negative column");
	    this.i = i;
	    this.j = j;
	    this.t = t;
	}    

    public boolean isEmpty() {
	return (t == landType.EMPTY);
    }

    public boolean isRoad() {
	return (t == landType.ROAD);
    }

    public boolean isWater() {
	return (t == landType.WATER);
    }

    public boolean isPark() {
	return (t == landType.PARK);
    }

    public void buildRoad() {
	buildType(landType.ROAD);
    }

    public void buildWater() {
	buildType(landType.WATER);
    }

    public void buildPark() {
	buildType(landType.PARK);
    }

    public void buildBuilding() {
	buildType(landType.BUILDING);
    }

    private void buildType(landType t) {
	if (this.t != landType.EMPTY) {
	    throw new RunTimeException("Land not empty");
	}
	this.t = t;
    }

    public boolean isType(landType t) {
	return (this.t == t);
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

    public int hashCode()
    {
	int f = 0x9e3779b1;
	return ((i * f) ^ j) * f;
    }

    public int compareTo(Cell p)
    {
	return i != p.i ? i - p.i : j - p.j;
    }

    public String toString()
    {
	return "(" + i + ", " + j + ")";
    }

    public Cell[] neighbors() {
	return neighbors(50);
    }

    public Cell[] neighbors(int m)
    {
 	int n = (i > 0 ? 1 : 0) + (i < m ? 1 : 0)
	    + (j > 0 ? 1 : 0) + (j < m ? 1 : 0);
	Cell[] points = new Cell [n];
	if (i > 0) points[--n] = new Cell(i - 1, j);
	if (i < m) points[--n] = new Cell(i + 1, j);
	if (j > 0) points[--n] = new Cell(i, j - 1);
	if (j < m) points[--n] = new Cell(i, j + 1);
	return points;
    } 

    public static boolean isBuilding(Set<Cell> points) {
	return isBuilding(points.toArray(new Cell[points.size()]));
    }

    public static boolean isBuilding(Cell[] points)
    {
	if (points == null || points.length == 0)
	    return false;
	// add items in set and check uniqueness
	HashSet <Cell> open = new HashSet <Cell> ();
	for (Cell p : points)
	    if (p == null || !open.add(p))
		return false;
	// check if single connected component
	Stack <Cell> fringe = new Stack <Cell> ();
	fringe.push(points[0]);
	open.remove(points[0]);
	do {
	    for (Cell p : fringe.pop().neighbors())
		if (open.remove(p))
		    fringe.push(p);
	} while (!fringe.empty());
	return open.isEmpty();
    }
}
