package pentos.sim;

import java.util.*;

public class Building implements Iterable <Cell> {

    public enum buildingType {FACTORY, RESIDENCE};

    // cells in building (this rotation)
    private Set <Cell> cells;

    // hash code of cells (this rotation)
    private int hash;

    // circular list of rotations
    private Building rotate;

    

    private final building_type;

    // internal constructor
    *private Building(Set <Cell> cells, Building rotate)
    {
	this.cells = cells;
	this.rotate = rotate;
	hash = cells.hashCode();
    }

    // generate building and its rotations
    private void generateBuilding(Cell[] cells)
    {
	if (!Cell.isBuilding(cells))
	    throw new IllegalArgumentException("Not a building");
	int min_i = Integer.MAX_VALUE;
	int min_j = Integer.MAX_VALUE;
	int max_i = Integer.MIN_VALUE;
	int max_j = Integer.MIN_VALUE;
	for (Cell p : cells) {
	    if (min_i > p.i) min_i = p.i;
	    if (max_i < p.i) max_i = p.i;
	    if (min_j > p.j) min_j = p.j;
	    if (max_j < p.j) max_j = p.j;
	}
	Set <Cell> cells_0 = new HashSet <Cell> ();
	Set <Cell> cells_1 = new HashSet <Cell> ();
	Set <Cell> cells_2 = new HashSet <Cell> ();
	Set <Cell> cells_3 = new HashSet <Cell> ();
	for (Cell p : cells) {
	    cells_0.add(new Cell(p.i - min_i, p.j - min_j));
	    cells_1.add(new Cell(p.j - min_j, max_i - p.i));
	    cells_2.add(new Cell(max_i - p.i, max_j - p.j));
	    cells_3.add(new Cell(max_j - p.j, p.i - min_i));
	}
	this.cells = cells_0;
	hash = cells_0.hashCode();
	if (cells_0.equals(cells_1))
	    rotate = this;
	else if (cells_0.equals(cells_2))
	    rotate = new Building(cells_1, this);
	else
	    rotate = new Building(cells_1,
				  new Building(cells_2,
					       new Building(cells_3, this)));
    }

    // size of building
    public int size()
    {
	return cells.size();
    }

    public buildingType getType() {
	return building_type;
    }

    // check if buildings are equal
    public boolean equals(Building building)
    {
	if (cells.size() == building.cells.size()) {
	    Building rot = this;
	    do {
		if (rot.hash == building.hash &&
		    rot.cells.equals(building.cells))
		    return true;
		rot = rot.rotate;
	    } while (rot != this);
	}
	return false;
    }

    // generic equal
    public boolean equals(Object obj)
    {
	if (obj instanceof Building)
	    return equals((Building) obj);
	return false;
    }

    // the only way to access the cells
    public Iterator <Cell> iterator()
    {
	return cells.iterator();
    }

    // generate array of rotated buildings
    public Building[] rotations()
    {
	Building rot = this;
	int r = 0;
	do {
	    r++;
	    rot = rot.rotate;
	} while (rot != this);
	Building[] rots = new Building [r];
	rot = this;
	r = 0;
	do {
	    rots[r++] = rot;
	    rot = rot.rotate;
	} while (rot != this);
	return rots;
    }

    // invariant to rotation order
    public int hashCode()
    {
	Building rot = this;
	int hash = 0;
	do {
	    hash ^= rot.hash;
	    rot = rot.rotate;
	} while (rot != this);
	return hash;
    }

    // convert building to string
    public String toString(Cell q, boolean par)
    {
	StringBuffer buf = new StringBuffer();
	boolean f = true;
	for (Cell p : cells) {
	    if (f) f = false;
	    else buf.append(", ");
	    if (par) buf.append("(");
	    buf.append((p.i + q.i) + ", " + (p.j + q.j));
	    if (par) buf.append(")");
	}
	return buf.toString();
    }

    // default convert to string
    public String toString()
    {
	return toString(new Cell(0, 0), true);
    }
}
