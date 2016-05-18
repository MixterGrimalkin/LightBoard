package net.amarantha.lightboard.zone.transition;

import net.amarantha.lightboard.entity.Colour;
import net.amarantha.lightboard.entity.Pattern;

public abstract class Explode extends AbstractTransition {

    protected int maxSpacing = 30;

    protected int spacing = 0;

    @Override
    public void reset() { }

    @Override
    public int getNumberOfSteps() {
        return maxSpacing;
    }

    @Override
    public void animate(double progress) {
        int xSpacing = spacing;
        int ySpacing = spacing;
        Pattern exploded = new Pattern(zone.getPattern().getHeight() * (ySpacing + 1), zone.getPattern().getWidth() * (xSpacing + 1));
        for ( int r=0; r<zone.getPattern().getHeight(); r++ ) {
            for ( int c=0; c<zone.getPattern().getWidth(); c++ ) {
                int xJitter = 0;//(int)Math.round((Math.random()-0.5) * (spacing/3));
                int yJitter = 0;//(int)Math.round((Math.random()-0.5) * (spacing/3));
                int x = r*(ySpacing+1)+xJitter;
                int y = c*(xSpacing+1)+yJitter;
                Colour colour = zone.getPattern().getColourPoint(r, c);
                exploded.drawPoint(x, y, colour);
            }
        }
        int explodedX = zone.getRestX() + (zone.getPattern().getWidth()/2) - (exploded.getWidth()/2);
        int explodedY = zone.getRestY() + (zone.getPattern().getHeight()/2) - (exploded.getHeight()/2);
        zone.clear();
        zone.drawPattern(explodedX, explodedY, exploded);
        updateSpacing();
    }

    protected abstract void updateSpacing();

}