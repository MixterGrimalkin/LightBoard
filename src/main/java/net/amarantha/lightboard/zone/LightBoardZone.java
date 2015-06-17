package net.amarantha.lightboard.zone;

import net.amarantha.lightboard.entity.Edge;
import net.amarantha.lightboard.entity.HPosition;
import net.amarantha.lightboard.entity.Pattern;
import net.amarantha.lightboard.entity.VPosition;
import net.amarantha.lightboard.surface.LightBoardSurface;
import net.amarantha.lightboard.surface.RGBLightBoardSurface;
import net.amarantha.lightboard.surface.Region;
import net.amarantha.lightboard.util.Sync;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public abstract class LightBoardZone {

    private final BoardType boardType;

    protected final LightBoardSurface surface;

    protected LightBoardZone(LightBoardSurface surface) {
        this.surface = surface;
        region = surface.safeRegion(0, 0, surface.getCols(), surface.getRows());
        if ( surface instanceof RGBLightBoardSurface) {
            boardType = BoardType.COLOUR;
        } else {
            boardType = BoardType.BINARY;
        }
    }


    //////////////////////
    // Abstract Methods //
    //////////////////////

    public abstract boolean render();


    //////////
    // Tick //
    //////////

    public LightBoardZone setScrollTick(int scrollTick) {
        return setScrollTick((long)scrollTick);
    }

    public LightBoardZone setScrollTick(Long scrollTick) {
        this.scrollTick = scrollTick;
        return this;
    }

    public LightBoardZone start() {
        if ( !singleRender ) {
            Sync.addTask(new Sync.Task(scrollTick) {
                @Override
                public void runTask() {
                    tick();
                }
            });
        }
        onScrollComplete();
        resetScroll();
        return this;
    }

    protected boolean paused = true;
    protected boolean singleRender = false;
    protected boolean rendered = false;

    public void pause() {
        paused = true;
    }

    public void resume() {
        rendered = false;
        paused = false;
        tick();
    }

    public void tick() {
        if ( !paused ) {
            if (resting) {
                if (currentTimeMillis() - lastTick > restDuration) {
                    resting = false;
                    initScroll(Scrolling.OUT);
                }
            } else {
                updateScroll();
                if (autoRender) {
                    doRender();
                }
                lastTick = currentTimeMillis();
            }
        }
    }

    private Long scrollTick = null;
    private long lastTick;
    protected boolean resting;


    ///////////////
    // Scrolling //
    ///////////////

    private enum Scrolling { IN, OUT }

    protected int contentLeft = 0;
    protected int contentTop = 0;

    private int restX = 0;
    private int restY = 0;

    private final static int DELTA = 1;

    private int deltaX = 0;
    private int deltaY = 0;

    private void initScroll(Scrolling scrolling) {
        switch ( scrolling ) {
            case OUT:
                prepareScrollOut();
                break;
            case IN:
                switch (restPositionH) {
                    case LEFT:
                        restX = 0;
                        break;
                    case CENTRE:
                        restX = (region.width - getContentWidth()) / 2;
                        break;
                    case RIGHT:
                        restX = region.width - getContentWidth();
                        break;
                }
                switch (restPositionV) {
                    case TOP:
                        restY = 0;
                        break;
                    case MIDDLE:
                        restY = (region.height - getContentHeight()) / 2;
                        break;
                    case BOTTOM:
                        restY = region.height - getContentHeight();
                        break;
                }
                prepareScrollIn();
                break;
        }
    }

    private void prepareScrollIn() {
        switch (scrollFrom) {
            case TOP_EDGE:
                contentLeft = restX;
                contentTop = -getContentHeight();
                deltaX = 0;
                deltaY = DELTA;
                break;
            case LEFT_EDGE:
                contentLeft = -getContentWidth();
                contentTop = restY;
                deltaX = DELTA;
                deltaY = 0;
                break;
            case BOTTOM_EDGE:
                contentLeft = restX;
                contentTop = region.height;
                deltaX = 0;
                deltaY = -DELTA;
                break;
            case RIGHT_EDGE:
                contentLeft = region.width;
                contentTop = restY;
                deltaX = -DELTA;
                deltaY = 0;
                break;
            case NO_SCROLL:
                contentLeft = restX;
                contentTop = restY;
                deltaX = 0;
                deltaY = 0;
                break;
        }
    }

    private void prepareScrollOut() {
        switch (scrollTo) {
            case TOP_EDGE:
                deltaX = 0;
                deltaY = -DELTA;
                break;
            case LEFT_EDGE:
                deltaX = -DELTA;
                deltaY = 0;
                break;
            case BOTTOM_EDGE:
                deltaX = 0;
                deltaY = DELTA;
                break;
            case RIGHT_EDGE:
                deltaX = DELTA;
                deltaY = 0;
                break;
            case NO_SCROLL:
                if ( autoReset ) {
                    onScrollComplete();
                    resetScroll();
                }
                break;
        }
    }

    private void updateScroll() {
        contentLeft += deltaX;
        contentTop += deltaY;
        if ( isInRestPosition() ) {
            resting = true;
        }
        if ( !contentVisible() && autoReset ) {
            onScrollComplete();
            resetScroll();
        }

    }

    public void resetScroll() {
        initScroll(Scrolling.IN);
    }

    public void onScrollComplete() {
        scrollCompleteHandlers.forEach(ScrollCompleteHandler::onScrollComplete);
    }

    private boolean contentVisible() {
        return contentLeft < region.width
                && contentLeft+getContentWidth() >= 0
                && contentTop < region.height
                && contentTop+getContentHeight() >= 0;
    }

    private boolean isInRestPosition() {
        return     getContentWidth()<=region.width
                && getContentHeight()<=region.height
                && contentLeft == restX
                && contentTop == restY;
    }

    protected void doRender() {

        if ( clear ) {
            surface.clearRegion(region);
        }

        if ( !render() && autoReset ) {
            onScrollComplete();
            resetScroll();
        }

        if ( outline ) {
            surface.outlineRegion(region);
        }

        if ( invert ) {
            surface.invertRegion(region);
        }

    }

    public int getContentWidth() {
        return region.width;
    }

    public int getContentHeight() {
        return region.height;
    }


    /////////////////////
    // Surface Drawing //
    /////////////////////

    protected boolean drawPoint(int x, int y) {
        return surface.drawPoint(region.left+contentLeft+x, region.top+contentTop+y, region);
    }

    protected boolean clearPoint(int x, int y) {
        return surface.clearPoint(region.left + contentLeft + x, region.top + contentTop + y, region);
    }

    protected boolean drawRect(int x, int y, int width, int height, boolean fill) {
        Region toDraw = surface.safeRegion(region.left+contentLeft+x, region.top+contentTop+y, width, height);
        if ( fill ) {
            return surface.fillRegion(toDraw);
        } else {
            return surface.outlineRegion(toDraw);
        }
    }

    protected boolean clearRect(int x, int y, int width, int height) {
        Region toDraw = surface.safeRegion(region.left+contentLeft+x, region.top+contentTop+y, width, height);
        return surface.clearRegion(toDraw);
    }

    public boolean clear() {
        return surface.clearRegion(region);
    }

    protected boolean drawPattern(int x, int y, Pattern pattern) {
        return surface.drawPattern(region.left+contentLeft+x, region.top+contentTop+y, pattern, region);
    }

    protected boolean drawPattern(int x, int y, Pattern pattern, boolean clearBackground) {
        return surface.drawPattern(region.left+contentLeft+x, region.top+contentTop+y, pattern, clearBackground, region);
    }


    /////////////
    // Options //
    /////////////

    public LightBoardZone region(Region region) {
        this.region = region;
        return this;
    }

    public LightBoardZone setRegion(int regionLeft, int regionTop, int regionWidth, int regionHeight) {
        region = surface.safeRegion(regionLeft, regionTop, regionWidth, regionHeight);
        return this;
    }

    public LightBoardZone autoRender(boolean enableTickRender) {
        this.autoRender = enableTickRender;
        return this;
    }

    public LightBoardZone autoReset(boolean autoReset) {
        this.autoReset = autoReset;
        return this;
    }

    public LightBoardZone clear(boolean clear) {
        this.clear = clear;
        return this;
    }

    public LightBoardZone outline(boolean outline) {
        this.outline = outline;
        return this;
    }

    public LightBoardZone invert(boolean invert) {
        this.invert = invert;
        return this;
    }

    public LightBoardZone scroll(Edge from, Edge to) {
        scrollFrom = from;
        scrollTo = to;
        return this;
    }

    public LightBoardZone setRestPosition(HPosition x, VPosition y) {
        restPositionH = x;
        restPositionV = y;
        return this;
    }

    public LightBoardZone setRestDuration(int pause) {
        restDuration = pause;
        return this;
    }

    public LightBoardZone singleRender(boolean singleRender) {
        this.singleRender = singleRender;
        return this;
    }

    private boolean autoRender = true;
    private boolean autoReset = true;

    protected boolean clear = true;
    private boolean outline = false;
    private boolean invert = false;

    protected Region region;

    private Edge scrollFrom = Edge.NO_SCROLL;
    private Edge scrollTo = Edge.NO_SCROLL;
    private HPosition restPositionH = HPosition.CENTRE;
    private VPosition restPositionV = VPosition.MIDDLE;
    private int restDuration = 3000;

    private List<ScrollCompleteHandler> scrollCompleteHandlers = new ArrayList<>();

    public static interface  ScrollCompleteHandler {
        void onScrollComplete();
    }

    public void addScrollCompleteHandler(ScrollCompleteHandler handler) {
        scrollCompleteHandlers.add(handler);
    }


    /////////////
    // Getters //
    /////////////

    public Edge getScrollFrom() {
        return scrollFrom;
    }

    public Edge getScrollTo() {
        return scrollTo;
    }

    public HPosition getRestPositionH() {
        return restPositionH;
    }

    public VPosition getRestPositionV() {
        return restPositionV;
    }

    public int getRestDuration() {
        return restDuration;
    }

    public Region getRegion() {
        return region;
    }

    public BoardType getBoardType() {
        return boardType;
    }

    protected enum BoardType { BINARY, COLOUR }

}
