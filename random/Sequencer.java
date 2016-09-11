package pentos.random;

import java.util.*;
import pentos.sim.Building;
import pentos.sim.Cell;

public class Sequencer implements pentos.sim.Sequencer {

    private Random gen = new Random();
    
    public Building next() {
	if (gen.nextInt(10) < 1)
	    return randomFactory();
	else
	    return randomResidence();
    }

    private Building randomResidence() { 
	Set<Cell> residence = new HashSet<Cell>();
	Cell tail = new Cell(0,0);
	residence.add(tail);
	for (int i=0; i<4; i++) {
	    ArrayList<Cell> walk_cells = new ArrayList<Cell>();
	    for (Cell p : tail.neighbors()) {
		if (!residence.contains(p))
		    walk_cells.add(p);
	    }
	    tail = walk_cells.get(gen.nextInt(walk_cells.size()));
	    residence.add(tail);
	}
	return new Building(residence.toArray(new Cell[residence.size()]), Building.Type.RESIDENCE);
    }    

    private Building randomFactory() {

	Set<Cell> residence = new HashSet<Cell>();
	Cell tail = new Cell(0,0);
	residence.add(tail);
	int length = gen.nextInt(20) + 5;
	for (int i=0; i<length; i++) {
	    ArrayList<Cell> walk_cells = new ArrayList<Cell>();
	    for (Cell p : tail.neighbors()) {
		if (!residence.contains(p) && p.i < 5 && p.j < 5)
		    walk_cells.add(p);
	    }
	    if (walk_cells.isEmpty())
		break;
	    tail = walk_cells.get(gen.nextInt(walk_cells.size()));
	    residence.add(tail);
	}
	return new Building(residence.toArray(new Cell[residence.size()]), Building.Type.FACTORY);
    }    
    
}
