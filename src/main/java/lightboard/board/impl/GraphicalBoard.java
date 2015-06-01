package lightboard.board.impl;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lightboard.board.HasColourSwitcher;
import lightboard.board.LightBoard;
import lightboard.board.MonoLightBoard;
import lightboard.board.PolyLightBoard;
import lightboard.updater.WebService;
import lightboard.util.Sync;

public class GraphicalBoard implements PolyLightBoard, HasColourSwitcher {

    private final static String BLACK_BACKGROUND = "-fx-background-color: black;";

    private double redMin = 0.05;
    private double redMax = 1.0;

    private double greenMin = 0.05;
    private double greenMax = 1.0;

    private double blueMin = 0.05;
    private double blueMax = 1.0;

    private Color off = Color.color(redMin, greenMin, blueMin);

    private final static Long LED_REFRESH_TIME = 50L;

    private final int rows;
    private final int cols;

    private Circle[][] leds;

    private int d;

    private Stage stage;

    private String title;
    private int ledRadius;
    private int spacer;

    public GraphicalBoard(int rows, int cols, Stage stage) {
        this(rows, cols, stage, "LightBoard", 2, 0);

    }

    public GraphicalBoard(int rows, int cols, Stage stage, String title, int ledRadius, int spacer) {
        this.rows = rows;
        this.cols = cols;
        this.stage = stage;
        this.title = title;
        this.ledRadius = ledRadius;
        this.spacer = spacer;
        debugTo(new TextBoard(rows, cols));
    }

    @Override
    public void init() {

        System.out.println("Starting UI Simulation LightBoard....");

        leds = new Circle[rows][cols];
        d = ledRadius*2;

        // Build UI components
        final Pane pane = new Pane();
        pane.setStyle(BLACK_BACKGROUND);
        addMouseHandlers(pane);
        Group board = new Group();
        pane.getChildren().add(board);

        // Create LED Board
        for ( int row=0; row<rows; row++ ) {
            for ( int col=0; col<cols; col++ ) {
                Circle led = new Circle(ledRadius - spacer, off);
                led.setCenterX(d + col * d);
                led.setCenterY(d + row * d);
                board.getChildren().add(led);
                leds[row][col] = led;
            }
        }

        // Start UI
        stage.setScene(new Scene(pane, getWidthPixels(), getHeightPixels()));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
        stage.setTitle(title);
        stage.show();

        stage.setOnCloseRequest(event -> {
            WebService.stopWebService();
            Sync.stopSyncThread();
        });

        System.out.println("Board Ready");

    }

    private void addMouseHandlers(Pane pane) {

        pane.setOnMouseDragged((e)->{
            if ( dragOffsetX==null || dragOffsetY==null ) {
                dragOffsetX = e.getX();
                dragOffsetY = e.getY();
            }
            stage.setX(stage.getX()+e.getX()-dragOffsetX);
            stage.setY(stage.getY()+e.getY()-dragOffsetY);
        });

        pane.setOnMouseReleased((e) -> {
            dragOffsetX = null;
            dragOffsetY = null;
        });

        pane.setOnMouseClicked((e) -> {
           if ( e.isControlDown() && e.isShiftDown() ) {
               dumpToDebug = !dumpToDebug;
           }
        });

        pane.setOnMouseClicked((e) -> {
            if ( e.isControlDown() && e.isAltDown() ) {
                if ( e.isShiftDown() ) {
                    stage.hide();
                    stage = new Stage();
                    init();
                } else {
                    dumpToDebug = true;
                }
            }
        });

    }

    private Double dragOffsetX = null;
    private Double dragOffsetY = null;


    ///////////////////////////
    // Polychrome LightBoard //
    ///////////////////////////

    @Override
    public void dump(double[][][] data) {
        if ( allowPoly && dumpToDebug && debugBoard!=null) {
            PolyLightBoard pBoard = (PolyLightBoard)debugBoard;
            pBoard.dump(data);
            dumpToDebug = false;
        }
        for ( int r=0; r<data[0].length; r++ ) {
            for ( int c=0; c<data[0][0].length; c++ ) {
                double red = data[0][r][c];
                double green = data[1][r][c];
                double blue = data[2][r][c];
                if ( colourOverride ) {
                    if ( red>=0.5 || green>=0.5 || blue>=0.5 ) {
                        red = redMax;
                        green = greenMax;
                        blue = blueMax;
                    } else {
                        red = redMin;
                        green = greenMin;
                        blue = blueMin;
                    }
                } else if ( simulateRedGreenOnly ) {
                    red = red>=0.5 ? redMax : redMin;
                    green = green>=0.5 ? greenMax : greenMin;
                    blue = blueMin;
                }
                if ( leds[r][c]!=null ) {
                    leds[r][c].setFill(Color.color(red, green, blue));
                }
            }
        }
    }

    private boolean colourOverride = false;
    private boolean simulateRedGreenOnly = true;


    ///////////////////////////
    // Monochrome LightBoard //
    ///////////////////////////

    @Override
    public void dump(double[][] data) {
        if ( allowMono && dumpToDebug && debugBoard!=null) {
            MonoLightBoard mBoard = (MonoLightBoard)debugBoard;
            mBoard.dump(data);
            dumpToDebug = false;
        }
        for ( int r=0; r<data.length; r++ ) {
            dumpMonoRow(r, data[r]);
        }
    }

    private void dumpMonoRow(int rowNumber, double... data) {
        Circle[] rowLights = leds[rowNumber];
        for ( int c=0; c<rowLights.length; c++ ) {
            double red = redMin + (data[c]*(redMax - redMin));
            double green = greenMin + (data[c]*(greenMax - greenMin));
            double blue = blueMin + (data[c]*(blueMax - blueMin));
            if ( rowLights[c]!=null ) {
                rowLights[c].setFill(Color.color(red, green, blue));
            }
        }
    }


    ///////////////////////
    // Binary LightBoard //
    ///////////////////////

    @Override
    public synchronized void dump(boolean[][] data) {
        if ( dumpToDebug && debugBoard!=null) {
            debugBoard.dump(data);
            dumpToDebug = false;
        }
        for ( int r=0; r<data.length; r++ ) {
            dumpBinaryRow(r, data[r]);
        }
    }

    private void dumpBinaryRow(int rowNumber, boolean... rowData) {
        Circle[] rowLights = leds[rowNumber];
        for ( int c=0; c<rowLights.length; c++ ) {
            double red = rowData[c] ? redMax : redMin;
            double green = rowData[c] ? greenMax : greenMin;
            double blue = rowData[c] ? blueMax : blueMin;
            if ( rowLights[c]!=null ) {
                Color color = Color.color(red, green, blue);
                if ( !rowLights[c].getFill().equals(color)) {
                    rowLights[c].setFill(color);
                }
            }
        }
    }


    /////////////////////////
    // Board Specification //
    /////////////////////////

    @Override
    public Long getRefreshInterval() {
        return LED_REFRESH_TIME;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getCols() {
        return cols;
    }

    public int getHeightPixels() {
        return  (rows+1) * d;
    }

    public int getWidthPixels() {
        return (cols+1) * d;
    }


    ///////////////
    // Debugging //
    ///////////////

    private boolean dumpToDebug = false;
    private LightBoard debugBoard;
    private boolean allowMono = false;
    private boolean allowPoly = false;
    public GraphicalBoard debugTo(LightBoard debugBoard) {
        this.debugBoard = debugBoard;
        if ( debugBoard instanceof MonoLightBoard) {
            allowMono = true;
        }
        if ( debugBoard instanceof PolyLightBoard) {
            allowMono = true;
            allowPoly = true;
        }
        return this;
    }

    @Override
    public void red() {
        colourOverride = true;
        redMin = 0.05;
        redMax = 1.0;
        greenMin = 0;
        greenMax = 0;
        blueMin = 0;
        blueMax = 0;
    }

    @Override
    public void green() {
        colourOverride = true;
        redMin = 0;
        redMax = 0;
        greenMin = 0.05;
        greenMax = 1.0;
        blueMin = 0;
        blueMax = 0;
    }

    @Override
    public void yellow() {
        colourOverride = true;
        redMin = 0.05;
        redMax = 1.0;
        greenMin = 0.05;
        greenMax = 1.0;
        blueMin = 0;
        blueMax = 0;
    }

    @Override
    public void blue() {
        colourOverride = true;
        redMin = 0;
        redMax = 0;
        greenMin = 0;
        greenMax = 0;
        blueMin = 0.05;
        blueMax = 1.0;
    }

    @Override
    public void multi() {
        colourOverride = false;
        redMin = 0.05;
        redMax = 1.0;
        greenMin = 0.05;
        greenMax = 1.0;
        blueMin = 0.05;
        blueMax = 1.0;
    }

    @Override
    public void cycle(int ms) {
        redMin = 0.05;
        redMax = 1.0;
        greenMin = 0.05;
        greenMax = 1.0;
        blueMin = 0.05;
        blueMax = 1.0;
        System.err.println("Graphical Board Does Not Support Colour Cycling");
    }
}
