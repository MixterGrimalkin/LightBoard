package net.amarantha.lightboard.zone.transition;

import java.util.Map;

public class TypeIn extends AbstractTransition {

    private Map<Integer, Letter> letters;
    private int currentLetter;

    @Override
    public void reset() {
        letters = splitPattern();
        currentLetter = 0;
    }

    @Override
    public int getNumberOfSteps() {
        return letters.size();
    }

    @Override
    public void animate(double progress) {
        zone.clear();
        for ( int i = 0; i <= currentLetter; i++ ) {
            Letter l = letters.get(i);
            zone.drawPattern(zone.getRestX()+l.x, zone.getRestY()+l.y, l.pattern);
        }
        currentLetter++;
    }

}
