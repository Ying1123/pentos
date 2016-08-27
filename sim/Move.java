package pentos.sim;

public class Move {
    
    public final boolean accept;
    public final Cell location;
    public final int rotation;
    public final Cell[] water;
    public final Cell[] park;

    public Move(boolean accept) 
    {
	if (accept == false) {
	    this.accept = false;
	}
	else {
	    throw new RunTimeException("Must specify location and orientation of building.");
	}
    }

    public Move(boolean accept, Cell location, int rotation, Cell[] water, Cell[] park)
    {
	this.accept = accept;
	this.location = location;
	this.rotation = rotation;
	this.water = water;
	this.park = park;
    }
}
