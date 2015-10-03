package net.amarantha.lightboard.updater.transport;

import com.google.inject.Inject;
import net.amarantha.lightboard.updater.Updater;
import net.amarantha.lightboard.utility.Sync;
import net.amarantha.lightboard.zone.impl.TextZone;
import org.javalite.http.Http;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class BusUpdater extends Updater {

    private final static String TFL_BUS_URL = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1";

    protected Map<String, BusDeparture> buses = new HashMap<>();

    private TextZone busNumberZone;
    private TextZone leftDestinationZone;
    private TextZone leftTimesZone;
    private TextZone rightDestinationZone;
    private TextZone rightTimesZone;

    @Inject
    public BusUpdater(Sync sync) {
        super(sync);
    }

    public BusUpdater setZones(TextZone busNumberZone, TextZone leftDestinationZone, TextZone leftTimesZone, TextZone rightDestinationZone, TextZone rightTimesZone) {
        this.busNumberZone = busNumberZone;
        this.leftDestinationZone = leftDestinationZone;
        this.leftTimesZone = leftTimesZone;
        this.rightDestinationZone = rightDestinationZone;
        this.rightTimesZone = rightTimesZone;
        return this;
    }

    public void loadBusConfig() {

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader("busconfig.json"));
            JSONObject jsonObject = (JSONObject) obj;

            JSONArray busJa = (JSONArray)jsonObject.get("buses");
            Iterator<JSONObject> iter = busJa.iterator();
            while ( iter.hasNext() ) {
                JSONObject bus = iter.next();
                buses.put(
                        (String)bus.get("id"),
                        new BusDeparture(
                                (long)bus.get("stop"),
                                (String)bus.get("bus"),
                                (String)bus.get("destination"),
                                (boolean)bus.get("active"),
                                (long)bus.get("offset")
                        )
                );
            }
        } catch (ParseException | IOException e) {
            saveBusConfig();
            e.printStackTrace();
        }
    }

    public void saveBusConfig() {
        try {
            JSONObject json = getBusStopJson();
            FileWriter file = new FileWriter("busconfig.json");
            file.write(json.toString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getBusStopJson() {
        JSONObject json = new JSONObject();
        JSONArray ja = new JSONArray();

        for ( Map.Entry<String, BusDeparture> entry : buses.entrySet() ) {
            JSONObject jsonBus = new JSONObject();
            jsonBus.put("id", entry.getKey());
            BusDeparture bus = entry.getValue();
            jsonBus.put("stop", bus.getStopCode());
            jsonBus.put("bus", bus.getBusNo());
            jsonBus.put("destination", bus.getDestination());
            jsonBus.put("offset", bus.getOffset());
            jsonBus.put("active", bus.isActive());
            ja.add(jsonBus);
        }

        json.put("buses", ja);
        return json;
    }
    public Map<String, BusDeparture> getBusDepartures() {
        return buses;
    }

    private int numberOfActiveBuses(Map<BusDeparture, List<Long>> map) {
        int result = 0;
        for ( Entry<BusDeparture, List<Long>> timeEntry : map.entrySet() ) {
            if ( !timeEntry.getValue().isEmpty() ) {
                result ++;
            }
        }
        return result;
    }

    @Override
    public void refresh() {

        loadBusConfig();

        Thread thread = new Thread(() -> {

            busNumberZone.clearAllMessages();
            leftDestinationZone.clearAllMessages();
            leftTimesZone.clearAllMessages();
            rightDestinationZone.clearAllMessages();
            rightTimesZone.clearAllMessages();

            BusData data = new BusData();
            try {
                for (BusDeparture bus : buses.values()) {
                    if ( bus.isActive() ) {
                        data.addResult(bus, getDepartureTimesFor(bus));
                    }
                }
            } catch ( Exception e ) {
                data.getDataByBusNumber().clear();
            }

            int sourceId = 0;

            if ( data.getDataByBusNumber().entrySet().isEmpty() ) {
                busNumberZone.addMessage(sourceId, " ");
                leftDestinationZone.addMessage(sourceId, "{red}No Data");
                leftTimesZone.addMessage(sourceId, "{red}* * *");
                rightDestinationZone.addMessage(sourceId, "{red}No Data");
                rightTimesZone.addMessage(sourceId, "{red}* * *");
            } else {
                for (Entry<String, Map<BusDeparture, List<Long>>> eBus : data.getDataByBusNumber().entrySet()) {

                    busNumberZone.clearMessages(sourceId);
                    leftDestinationZone.clearMessages(sourceId);
                    leftTimesZone.clearMessages(sourceId);
                    rightDestinationZone.clearMessages(sourceId);
                    rightTimesZone.clearMessages(sourceId);

                    int activeBuses = numberOfActiveBuses(eBus.getValue());

                    if (activeBuses > 0) {

                        String busNumber = "{yellow}" + eBus.getKey();
                        busNumberZone.addMessage(sourceId, busNumber);

                        if (activeBuses > 2) {
                            leftDestinationZone.addMessage(sourceId, "{red}Too");
                            leftTimesZone.addMessage(sourceId, "{red}Many");
                            rightDestinationZone.addMessage(sourceId, "{red}Buses");
                            rightTimesZone.addMessage(sourceId, "{red}Returned!");
                        } else {


                            int direction = 1;

                            for (Entry<BusDeparture, List<Long>> timeEntry : eBus.getValue().entrySet()) {

                                String destination = "{yellow}" + timeEntry.getKey().getDestination();

                                List<Long> busTimes = timeEntry.getValue();
                                Collections.sort(busTimes);
                                StringBuilder sb = new StringBuilder();
                                int count = 0;
                                for (Long bt : busTimes) {
                                    Long busTime = bt + timeEntry.getKey().getOffset();
                                    if (count < 3 && busTime >= 0) {
                                        if (busTime == 0) {
                                            sb.append("{red}due ");
                                        } else {
                                            if (busTime < 5) {
                                                sb.append("{red}");
                                            } else {
                                                sb.append("{green}");
                                            }
                                            sb.append(busTime).append("m ");
                                        }
                                    }
                                    count++;
                                }
                                String timesMessage = sb.toString();

                                if (direction == 1) {
                                    leftDestinationZone.addMessage(sourceId, destination);
                                    leftTimesZone.addMessage(sourceId, timesMessage);
                                } else if (direction == 2) {
                                    rightDestinationZone.addMessage(sourceId, destination);
                                    rightTimesZone.addMessage(sourceId, timesMessage);
                                }
                                direction++;

                                if (activeBuses == 1) {
                                    rightDestinationZone.addMessage(sourceId, "{yellow}-");
                                    rightTimesZone.addMessage(sourceId, "{yellow}-");
                                }

                            }

                        }
                    }
                    sourceId++;
                }
            }

        });

        thread.start();


    }

    protected List<Long> getDepartureTimesFor(BusDeparture departure) throws Exception {

        List<Long> result = new ArrayList<>();

        String httpResult = Http.get(TFL_BUS_URL + "?StopCode1=" + departure.getStopCode()).text();

        List<String[]> lineArrays = breakIntoLineArrays(httpResult);

        if ( lineArrays.isEmpty() ) {
            throw new Exception();
        }

        for ( String[] line : lineArrays ) {
            if ( line[BUS_NUMBER].equals(departure.getBusNo()) ) {
                result.add(minutesIntoFuture(line[BUS_TIME]));
            }
        }

        return result;
    }

    private Long minutesIntoFuture(String timecodeStr) {
        Long time = 0L;
        try {
            Long timecode = Long.parseLong(timecodeStr);
            Date due = new Date(timecode - System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("m");
            time = Long.parseLong(sdf.format(due));
        } catch ( NumberFormatException e ) {
            System.out.println(timecodeStr + " fucked!");
        }
        return time;
    }

    private List<String[]> breakIntoLineArrays(String httpResult) {
        List<String[]> result = new LinkedList<>();
        String[] lines = httpResult.split("\n");
        for (int l = 1; l < lines.length; l++) {
            String line = lines[l];
            String trimmed = line.replaceAll("\\[","").replaceAll("]", "").replace("\"","").replaceAll("\r", "");
            String[] cols = trimmed.split(",");
            result.add(cols);
        }
        return result;
    }

    private Map<String, List<Long>> parseResult(String result) {
        Map<String, List<Long>> messages = new HashMap<>();
        String[] lines = result.split("\n");
        for (int l = 1; l < lines.length; l++) {
            String[] cols = lines[l].split(",");
            String bus = cols[2].substring(1, cols[2].length() - 1);
            List<Long> dueTimes = messages.get(bus);
            if (dueTimes == null) {
                dueTimes = new ArrayList<>();
                messages.put(bus, dueTimes);
            }
            String timecodeStr = cols[3].split("]")[0];
            Long timecode = null;
            try {
                timecode = Long.parseLong(timecodeStr);
            } catch (NumberFormatException e) {
                System.err.println("Bad time " + timecodeStr);
            }
            if (timecode != null && dueTimes.size() < 3) {

                Date due = new Date(timecode - System.currentTimeMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("m");
                Long time = Long.parseLong(sdf.format(due)) + 0;
                if (time >= 0) {
                    dueTimes.add(time);
                }
            }
        }
        return messages;
    }

    private static final int BUS_NUMBER = 2;
    private static final int BUS_TIME = 3;

}