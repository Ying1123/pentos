package pentos.g0;

import pentos.sim.Cell;
import pentos.sim.Building;
import pentos.sim.Land;
import pentos.sim.Move;

import java.util.*;

public class Player implements pentos.sim.Player {

    private Random gen = new Random();
    private Set<Cell> road_cells = new HashSet<Cell>();

    public Move play(Building request, Land land) {
	// find all valid building locations and orientations
	ArrayList <Move> moves = new ArrayList <Move> ();
	for (int i = 0 ; i < land.side ; i++)
	    for (int j = 0 ; j < land.side ; j++) {
		Cell p = new Cell(i, j);
		Building[] rotations = request.rotations();
		for (int ri = 0 ; ri < rotations.length ; ri++) {
		    Building b = rotations[ri];
		    if (land.buildable(b, p)) 
			moves.add(new Move(true, request, p, ri, new HashSet<Cell>(), new HashSet<Cell>(), new HashSet<Cell>()));
		}
	    }
	// choose a building placement at random
	if (moves.isEmpty()) // reject if no valid placements
	    return new Move(false);
	else {
	    Move chosen = moves.get(gen.nextInt(moves.size()));
	    // get coordinates of building placement (position plus local building cell coordinates)
	    Set<Cell> shiftedCells = new HashSet<Cell>();
	    for (Cell x : chosen.request.rotations()[chosen.rotation])
		shiftedCells.add(new Cell(x.i+chosen.location.i,x.j+chosen.location.j));
	    // builda road to connect this building to perimeter
	    Set<Cell> roadCells = findShortestRoad(shiftedCells, land);
	    if (roadCells != null) {
		chosen.road = roadCells;
		road_cells.addAll(roadCells);
		if (request.type == Building.Type.RESIDENCE) {
		    Set<Cell> markedForConstruction = new HashSet<Cell>();
		    markedForConstruction.addAll(roadCells);
		    chosen.water = randomWalk(shiftedCells, markedForConstruction, land, 4);
		    markedForConstruction.addAll(chosen.water);
		    chosen.park = randomWalk(shiftedCells, markedForConstruction, land, 4);
		}
		return chosen;
	    }
	    else // reject placement if building cannot be connected by road
		return new Move(false);
	}
    }
    
    // build shortest sequence of road cells to connect to a set of cells b
    private Set<Cell> findShortestRoad(Set<Cell> b, Land land) {
	Set<Cell> output = new HashSet<Cell>();
	boolean[][] checked = new boolean[land.side][land.side];
	Queue<Cell> queue = new LinkedList<Cell>();
	// add cells adjacent to current road cells
	for (Cell p : road_cells) {
	    for (Cell q : p.neighbors()) {
		if (!road_cells.contains(q) && land.unoccupied(q) && !b.contains(q)) 
		    queue.add(new Cell(q.i,q.j,p)); // use tail field of cell to keep track of previous road cell during the search
	    }
	}      
	// add border cells that don't have a road currently
	Cell source = new Cell(Integer.MAX_VALUE,Integer.MAX_VALUE);
	for (int z=0; z<land.side; z++) {
	    if (land.unoccupied(0,z) && !b.contains(new Cell(0,z)))
		queue.add(new Cell(0,z,source));
	    if (land.unoccupied(z,0) && !b.contains(new Cell(z,0)))
		queue.add(new Cell(z,0,source));
	    if (land.unoccupied(z,land.side-1) && !b.contains(new Cell(z,land.side-1)))
		queue.add(new Cell(z,land.side-1,source));
	    if (land.unoccupied(land.side-1,z) && !b.contains(new Cell(land.side-1,z)))
		queue.add(new Cell(land.side-1,z,source));
	}
	while (!queue.isEmpty()) {
	    Cell p = queue.remove();
	    checked[p.i][p.j] = true;
	    for (Cell x : p.neighbors()) {		
		if (b.contains(x)) { // trace back through search tree to find path
		    Cell tail = p;
		    while (!b.contains(tail) && !road_cells.contains(tail) && !tail.equals(source)) {
			output.add(new Cell(tail.i,tail.j));
			tail = tail.previous;
		    }
		    if (!output.isEmpty())
			return output;
		}
		else if (!checked[x.i][x.j] && land.unoccupied(x.i,x.j)) {
		    x.previous = p;
		    queue.add(x);	      
		} 

	    }
	}
	if (output.isEmpty() && queue.isEmpty())
	    return null;
	else
	    return output;
    }

    // walk n consecutive cells starting from a building. Used to build a random field or pond. 
    private Set<Cell> randomWalk(Set<Cell> b, Set<Cell> marked, Land land, int n) {
	ArrayList<Cell> adjCells = new ArrayList<Cell>();
	Set<Cell> output = new HashSet<Cell>();
	for (Cell p : b) {
	    for (Cell q : p.neighbors()) {
		if (land.isField(q) || land.isPond(q))
		    return new HashSet<Cell>();
		if (!b.contains(q) && !marked.contains(q) && land.unoccupied(q))
		    adjCells.add(q); 
	    }
	}
	if (adjCells.isEmpty())
	    return new HashSet<Cell>();
	Cell tail = adjCells.get(gen.nextInt(adjCells.size()));
	output.add(tail);
	for (int i=0; i<n; i++) {
	    ArrayList<Cell> walk_cells = new ArrayList<Cell>();
	    for (Cell p : tail.neighbors()) {
		if (!b.contains(p) && !marked.contains(p) && land.unoccupied(p))
		    walk_cells.add(p);		
	    }
	    if (walk_cells.isEmpty())
		return new HashSet<Cell>();
	    tail = walk_cells.get(gen.nextInt(walk_cells.size()));
	    output.add(tail);
	}
	return output;
    }

}
