package pentos.sim;

import java.util*;

public class Factory extends Building {

    public Factory(Set<Cell> cells) {
        building_type = buildingType.RESIDENCE;
	this.cells = cells;
	if (!valid())
	    throw new RunTimeException("Invalid factory");
    }

    private boolean valid() {
	int mini = 0;
	int maxi = Integer.MAX_VALUE;
	int minj = 0;
	int maxj = Integer.MAX_VALUE;
	for (Cell p : cells) {
	    if (p.i < mini)
		mini = p.i;
	    if (p.i > maxi)
		maxi = p.i;
	    if (p.j < minj)
		minj = p.j;
	    if (p.j > maxj)
		maxj = p.j;
	}
	return Cell.isBuilding(cells) && (maxi - mini <= 5) && (maxj - minj <= 5);
    }
}
