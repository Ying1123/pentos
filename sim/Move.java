package pentos.sim;

import java.util.*;

public class Move {
    
    public boolean accept;
    public Cell location;
    public int rotation;
    public Building request;
    public Set<Cell> road;
    public Set<Cell> water;
    public Set<Cell> park;

    public Move(boolean accept) { // as a convention, if you reject a request you cannot build roads/water/parks. There is no need to anyway until you accept the next request
	if (accept == false) {
	    this.accept = false;
	}
	else {
	    throw new RuntimeException("Must specify location and orientation of building.");
	}
    }

    public Move(boolean accept, Building request, Cell location, int rotation, Set<Cell> road, Set<Cell> water, Set<Cell> park) {
	this.accept = accept;
	this.request = request;
	this.location = location;
	this.rotation = rotation;
	this.road = road;
	this.water = water;
	this.park = park;
    }
}
