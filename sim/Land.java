package pentos.sim;

public class Land {

    // the array
    protected Cell[][] land;

    // create new Land
    public Land(int side)
    {
	if (side <= 0)
	    throw new IllegalArgumentException();
	land = new Cell[side][side];
	for (int i=0; i<side; i++) {
	    for (int j=0; j<side; j++) {
		land[i][j] = new Cell(i,j);
	    }
	}
	n_buildings = 0;
    }

    // check if a cell belongs to a pond or field respectively
    public boolean isPond(Cell p) {
	return isGroup( land[p.i][p.j], landType.WATER);
    }
    public boolean isField(Cell p) {
	return isGroup( land[p.i][p.j], landType.PARK);
    }

    private boolean isGroup(Cell p, landType t) {
	if (!p.isType(t)) {return false;}
	boolean[][] checked = new boolean[9][9];
	checked[5][5] = true;
	int count = 0;
	Stack<Cell> fringe = new Stack<Cell>();
	fringe.push(p);
	while (!fringe.isEmpty()) {
	    Cell x = fringe.pop();
	    if (x.isType(t) ) {
		count++;
		if (count == 4) {return true;}
 		for (Cell p : x.neighbors()) {
		    if (!checked[p.i - i + 5][p.j - j + 5]) {
			fringe.push( land[p.i][p.j] );
			checked[p.i - i + 5][p.j - j + 5] = true;
		    }
		}
	    }
	}
	return false;
    }

    // return number of rows and columns
    public int side()
    {
	return land.length;
    }

    // check if specific position is empty
    public boolean unoccupied(int i, int j)
    {
	return i >= 0 && i < land.length &&
	    j >= 0 && j < land[i].length &&
	    land[i][j].isEmpty();
    }

    // check if building can be built
    public boolean buildable(Building building, Cell q)
    {
	for (Cell p : building)
	    if (!unoccupied(p.i + q.i, p.j + q.j))
		return false;
	return true;
    }

    // build building (simulator calls this)
    public boolean build(Building building, Cell q)
    {
	if (!buildable(building, q))
	    return false;
	for (Cell p : building) {
	    land[p.i + q.i][p.j + q.j].buildBuilding();
	}
	return true;
    }

    

}
