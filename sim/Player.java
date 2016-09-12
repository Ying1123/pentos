package pentos.sim;

public interface Player {

    // return cutter shape (length given by simulator)
    // the positions can be given in any order but the
    // point coordinates must be positive the cutters
    // chosen before are shown are shown (normalized)

    public void init();
    public Move play(Building request, Land land);

}
