package pentos.random;

import java.util.*;
import pentos.sim.Building;
import pentos.sim.Cell;

public class Sequencer implements pentos.sim.Sequencer {

    private Random gen;
    private final double ratio = 0.7; // ratio of residences to total number of buildings

    public void init() {
	gen = new Random();
    }
    
    public Building next() {
	if (gen.nextDouble() > 0.7)
	    return randomFactory();
	else
	    return randomResidence();
    }

    private Building randomResidence() { // random walk of length 5
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

    private Building randomFactory() { // random rectangle with side length between 2 and 5 inclusive
	Set<Cell> factory = new HashSet<Cell>();
	int width = gen.nextInt(3);
	int height = gen.nextInt(3);
	for (int i=0; i<width+2; i++) {
	    for (int j=0; j<height+2; j++) {
		factory.add(new Cell(i,j));
	    }
	}
	return new Building(factory.toArray(new Cell[factory.size()]), Building.Type.FACTORY);
    }    
    
}
