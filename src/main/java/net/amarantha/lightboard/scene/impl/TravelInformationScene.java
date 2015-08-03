package net.amarantha.lightboard.scene.impl;

import net.amarantha.lightboard.font.SmallFont;
import net.amarantha.lightboard.scene.Scene;
import net.amarantha.lightboard.surface.LightBoardSurface;
import net.amarantha.lightboard.updater.Updater;
import net.amarantha.lightboard.updater.UpdaterBundle;
import net.amarantha.lightboard.updater.transport.BusTimesUpdater;
import net.amarantha.lightboard.updater.transport.TubeStatusUpdater;
import net.amarantha.lightboard.updater.weather.WeatherUpdater;
import net.amarantha.lightboard.zone.impl.ClockZone;
import net.amarantha.lightboard.zone.impl.CompositeZone;
import net.amarantha.lightboard.zone.impl.TextZone;

public class TravelInformationScene extends Scene {

    private static final int BUS_NUMBER_WIDTH = 20;
    private static final int CLOCK_WIDTH = 23;


    private static final int BUSES_HEIGHT = 18;
    private static final int TUBE_HEIGHT = 8;
    private static final int STATUS_HEIGHT = 5;

    public TravelInformationScene(LightBoardSurface surface) {
        super(surface, "Travel Information");
    }


    @Override
    public void build() {

        int busFrameWidth = (getCols()-CLOCK_WIDTH-BUS_NUMBER_WIDTH)/2;

        // Bus Arrivals
        TextZone busNumber = TextZone.scrollUp(getSurface());
        busNumber.setScrollTick(60).setRestDuration(3500)
                .setRegion(
                        0, BUSES_HEIGHT/4,
                        BUS_NUMBER_WIDTH, BUSES_HEIGHT/2);

        TextZone busDestinationLeft = TextZone.scrollUp(getSurface());
        busDestinationLeft.setScrollTick(60).setRestDuration(3500)
                .setRegion(
                        BUS_NUMBER_WIDTH, 0,
                        busFrameWidth, BUSES_HEIGHT/2);

        TextZone busTimesLeft = TextZone.scrollDown(getSurface());
        busTimesLeft.setScrollTick(60).setRestDuration(3500)
                .setRegion(
                        BUS_NUMBER_WIDTH, BUSES_HEIGHT/2,
                        busFrameWidth, BUSES_HEIGHT/2);

        TextZone busDestinationRight = TextZone.scrollUp(getSurface());
        busDestinationRight.setScrollTick(60).setRestDuration(3500)
                .setRegion(
                        BUS_NUMBER_WIDTH + busFrameWidth, 0,
                        busFrameWidth, BUSES_HEIGHT/2);

        TextZone busTimesRight = TextZone.scrollDown(getSurface());
        busTimesRight.setScrollTick(60).setRestDuration(3500)
                .setRegion(
                        BUS_NUMBER_WIDTH + busFrameWidth, BUSES_HEIGHT/2,
                        busFrameWidth, BUSES_HEIGHT/2);

        // Tube Status Summary
        TextZone tubeSummaryZone = TextZone.fixed(getSurface());
        tubeSummaryZone
                .setFont(new SmallFont())
                .setScrollTick(1000)
                .setRestDuration(1000)
                .setRegion(0, getRows() - STATUS_HEIGHT, getCols(), STATUS_HEIGHT);

        // Tube Status Detail
        TextZone tubeDetailZone = TextZone.scrollLeft(getSurface());
        tubeDetailZone
                .setScrollTick(25)
                .setRestDuration(5000)
                .setRegion(0, BUSES_HEIGHT, getCols(), TUBE_HEIGHT);

        // Bundle Travel Updater
        TubeStatusUpdater tubeStatus =  new TubeStatusUpdater(tubeDetailZone, tubeSummaryZone);
        BusTimesUpdater busTimes = new BusTimesUpdater(busNumber, busDestinationLeft, busTimesLeft, busDestinationRight, busTimesRight);
        tubeStatus.setDataRefresh(45000);
        busTimes.setDataRefresh(60000);

        CompositeZone cZone = new CompositeZone(getSurface(), busNumber, busDestinationLeft, busTimesLeft, busDestinationRight, busTimesRight);
        cZone.setScrollTick(100);//.setRestDuration(4000);

        // Weather
        TextZone weatherZone = TextZone.fixed(getSurface());
        WeatherUpdater weatherUpdater = new WeatherUpdater(weatherZone);


        // Clock
        TextZone clockZone = new ClockZone(getSurface());
        clockZone.setRegion(getCols() - CLOCK_WIDTH, 0, CLOCK_WIDTH, BUSES_HEIGHT);

        // Setup Scene
        registerZones(clockZone, tubeDetailZone, tubeSummaryZone, cZone);
        registerUpdaters(busTimes, tubeStatus);//, weatherUpdater);
        cZone.start();

    }

}
