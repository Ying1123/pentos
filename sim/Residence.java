package pentos.sim;

import java.util*;

public class Residence extends Building {

    public Residence(Set<Cell> cells){
	building_type = buildingType.RESIDENCE;
	this.cells = cells;
	if (!valid()) 
	    throw new RunTimeException("Invalid residence");
    }   

    private boolean valid() {
	return Cell.isBuilding(cells) && cells.size() == 5;
    }

}
