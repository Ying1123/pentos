package pentos.sim;

public class Move {
    
    public final boolean accept;
    public final Cell[] water;
    public final Cell[] park;

    public Move(boolean accept, Cell[] water, Cell[] park)
    {
	this.accept = accept;
	this.water = water;
	this.park = park;
    }
}
