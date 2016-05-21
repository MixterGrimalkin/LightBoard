package net.amarantha.lightboard.zone.transition;

public class InterlaceOut extends Interlace {

    @Override
    public void updateShift() {
        shift += shiftDelta++;
    }

    @Override
    public boolean isComplete() {
        return shift > maxShift;
    }

    @Override
    public void reset() {
        shift = 0;
        shiftDelta = 4;
    }

}
