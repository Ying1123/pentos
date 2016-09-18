package pentos.sim;
import java.util.*;

public class Land {

    // the array
    protected Cell[][] land;
    protected Set<Cell> road_network; // used internally to check road validity
    public final int side;

    // create new Land
    public Land(int side) {
	if (side <= 0)
	    throw new IllegalArgumentException();
	this.side = side;
	land = new Cell[side][side];
	for (int i=0; i<side; i++) {
	    for (int j=0; j<side; j++) {
		land[i][j] = new Cell(i,j);
	    }
	}
	road_network = new HashSet<Cell>();
	for (int z=1; z<side+2; z++) { // turn 50x50 board into 52x52 board with borders included
	    road_network.add(new Cell(z,0));
	    road_network.add(new Cell(0,z));
	    road_network.add(new Cell(z,side+1));
	    road_network.add(new Cell(side+1,z));
	}
    }

    // check if a cell belongs to a pond or field respectively
    public boolean isPond(Cell q) {return isPond(q.i,q.j);}
    public boolean isField(Cell q) {return isField(q.i,q.j);}
    public boolean isPond(int i, int j) {return isGroup( i,j, Cell.Type.WATER);}
    public boolean isField(int i, int j) {return isGroup( i,j, Cell.Type.PARK);}

    private boolean isGroup(int i, int j, Cell.Type t) {
	if (!land[i][j].isType(t)) {return false;}
	boolean[][] checked = new boolean[9][9];
	int count = 0;
	Stack<Cell> stack = new Stack<Cell>();
	stack.push(land[i][j]);
	while (!stack.isEmpty()) {
	    Cell x = stack.pop();
	    checked[x.i - i + 5][x.j - j + 5] = true;
	    if (x.isType(t) ) {
		if (++count == 4) 
		    return true;
 		for (Cell p : x.neighbors()) {
		    if (!checked[p.i - i + 5][p.j - j + 5]) {
			stack.push( land[p.i][p.j] );
		    }
		}
	    }
	}
	return false;
    }

    // check if specific position is empty
    public boolean unoccupied(Cell q) {return unoccupied(q.i,q.j);}
    public boolean unoccupied(int i, int j) {
	return i >= 0 && i < land.length &&
	    j >= 0 && j < land[i].length &&
	    land[i][j].isEmpty();
    }

    // check if building can be built
    public boolean buildable(Building building, Cell q) {
	for (Cell p : building) {
	    if (!unoccupied(p.i + q.i, p.j + q.j))
		return false;

	    Cell[] adj = land[p.i+q.i][p.j+q.j].neighbors();
	    for (Cell x : adj) {
		if ( (land[x.i][x.j].type == Cell.Type.FACTORY && building.type == Building.Type.RESIDENCE) || (land[x.i][x.j].type == Cell.Type.RESIDENCE && building.type == Building.Type.FACTORY)) 
		    return false;
	    }
	}
	return true;
    }
    
    // functions for simulator to build stuff
    protected void buildWater(Cell q) {
	land[q.i][q.j].buildWater();
    }
    protected void buildRoad(Cell q) {
	road_network.add(new Cell(q.i+1,q.j+1)); // re-index to allow borders
	land[q.i][q.j].buildRoad();
    }
    protected void buildPark(Cell q) {
	land[q.i][q.j].buildPark();
    }
    protected boolean validateRoads() {
	return Cell.isConnected(road_network,side+2);
    }
    // return -1 if building cannot be built. Otherwise return the score accrued from constructing the building
    protected int build(Building building, Cell q) {
	if (!buildable(building, q))
	    throw new RuntimeException("Building not buildable.");
	int score = 0;
	for (Cell p : building) {
	    if (building.type == Building.Type.FACTORY)
		land[p.i + q.i][p.j + q.j].buildFactory();
	    else if (building.type == Building.Type.RESIDENCE)
		land[p.i + q.i][p.j + q.j].buildResidence();
	    else
		throw new IllegalArgumentException("Building type not specified.");
	    score += 1;	    
	}
	boolean existsRoad = false;
	
	// gather all adjacent cells to this building
	Set<Cell> adjacent_points = new HashSet<Cell>();	
	for (Cell p : building) {
	    Cell[] adj = land[p.i+q.i][p.j+q.j].neighbors();
	    for (Cell a : adj) {
		adjacent_points.add(a); // also includes building cells but doesn't really matter for the following checks
		if (a.i == 0 || a.i == side-1 || a.j == 0 || a.j == side-1)
		    existsRoad = true;
	    }
	}

	// verify building is next to road
	for (Cell p : adjacent_points) {
	    if (land[p.i][p.j].isRoad()){
		existsRoad = true;
		break;
	    }
	}
	if (!existsRoad){
	    System.err.println("Building not next to road.");
	    return -1;
	}

	// check for pond bonus
	for (Cell p : adjacent_points) {
	    if (isPond(land[p.i][p.j])) {
		score += 2;
		break;
	    }
	}

	// check for field bonus
	for (Cell p : adjacent_points) {
	    if (isField(land[p.i][p.j])) {
		score += 2;
		break;
	    }
	}

	return score;
    }

    

}
