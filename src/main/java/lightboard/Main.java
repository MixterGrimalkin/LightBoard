package lightboard;

import javafx.application.Application;
import javafx.stage.Stage;
import lightboard.board.LightBoard;
import lightboard.board.impl.BlankBoard;
import lightboard.board.impl.GraphicalBoard;
import lightboard.board.impl.TextBoard;
import lightboard.board.surface.LightBoardSurface;
import lightboard.board.zone.impl.TextZone;
import lightboard.updater.schedule.MessageResource;
import lightboard.updater.schedule.MessageUpdater;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

import static lightboard.board.zone.Zones.*;

public class Main extends Application {

    private final static int COLS = 180;
    private final static int ROWS = 16;

    private static int ledRadius = 2;
    private static int ledSpacer = 1;

    private final static int CLOCK_WIDTH = 28;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        HttpServer server = startServer();

        int cols = getCols();
        int rows = getRows();

        LightBoard board;

        String boardType = getParameters().getNamed().get("board");
        if ("graphical".equals(boardType)) {
            board = new GraphicalBoard(rows, cols, primaryStage, "Travel Board", ledRadius, ledSpacer).debugTo(new TextBoard(rows, cols));
            ((GraphicalBoard)board).setServer(server);
        } else if ("text".equals(boardType)) {
            board = new TextBoard(rows, cols);
        } else {
            board = new BlankBoard(rows, cols);
        }
        board.init();

        LightBoardSurface surface = new LightBoardSurface(board);
        surface.init();

        startClock          (surface, COLS - CLOCK_WIDTH, 0,    CLOCK_WIDTH, ROWS);

        TextZone busZone =  startBusStopDisplay     (surface, 0, 0,                     COLS - CLOCK_WIDTH, ROWS/2);
        TextZone tubeZone = startTubeStatusDisplay  (surface, 0, ROWS/2,                COLS - CLOCK_WIDTH, ROWS/2,         "bad");

        MessageUpdater m = new MessageUpdater(busZone, tubeZone);
        MessageResource.bindUpdater(m);

    }

    private int getCols() {
        int cols = COLS;
        String colsStr = getParameters().getNamed().get("cols");
        try {
            cols = Integer.parseInt(colsStr);
        } catch ( NumberFormatException e ) {}
        return cols;
    }

    private int getRows() {
        int rows = ROWS;
        String rowsStr = getParameters().getNamed().get("rows");
        try {
            rows = Integer.parseInt(rowsStr);
        } catch ( NumberFormatException e ) {}
        return rows;
    }

    public static final String BASE_URI = "http://192.168.0.3:8080/lightboard/";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("lightboard");//.updater.schedule");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

}
