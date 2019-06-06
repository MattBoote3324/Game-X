package seng302.Client.Controllers;

import javafx.animation.AnimationTimer;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import seng302.Client.ClientMain;
import seng302.Common.*;

import java.net.URL;
import java.util.*;

/**
 * ONLY DRAW TO CANVASES
 */
public class RaceViewController implements Initializable, Observer {
    private Canvas canvas;
    private GraphicsContext graphicsContext;

    private double scaleGeoToCartesian;

    private HashMap<Boat, ArrayList<Double>> boatsLastPos = new HashMap<>();

    private boolean zoomed;
    private List<Double> centreCoords = new ArrayList<>();
    private double zoomFactor = 1;

    private CourseController myCourseController;

    private Course course;

    private Pane backgroundPane;
    private ImageView bgImageView;

    private AnimationTimer animationTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientMain.raceViewController = this;

        canvas = new Canvas(Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());

        backgroundPane = new Pane();
        backgroundPane.setMinWidth(Screen.getPrimary().getBounds().getWidth());
        backgroundPane.setMinHeight(Screen.getPrimary().getBounds().getHeight());

        Image bgImage = new Image(getClass().getClassLoader().getResourceAsStream("images/ripple.gif"));
        bgImageView = new ImageView(bgImage);
        bgImageView.setOpacity(0.35);
        bgImageView.setViewport(new Rectangle2D(0, 0, backgroundPane.getWidth(), backgroundPane.getHeight()));
        bgImageView.minWidth(backgroundPane.getWidth());
        bgImageView.minHeight(backgroundPane.getHeight());
        backgroundPane.getChildren().add(bgImageView);
    }

    /**
     * Start an AnimationTimer that constantly re-draws everything.
     */
    public synchronized void startCanvasReDrawer() {
        FrameRateMeasure frameRateMeasure = new FrameRateMeasure(50);

        setCanvasDimensions();
        graphicsContext = canvas.getGraphicsContext2D();

        try {
            ClientMain.mainWindowController.getRaceViewAnchor().getChildren().remove(backgroundPane);
            ClientMain.mainWindowController.getRaceViewAnchor().getChildren().add(backgroundPane);
            ClientMain.mainWindowController.getRaceViewAnchor().getChildren().remove(canvas);
            ClientMain.mainWindowController.getRaceViewAnchor().getChildren().add(canvas);
        } catch (IllegalArgumentException e) {
            System.err.println("Caught an error in the JavaFX thread on restarting race " + e);
            e.printStackTrace();
        }
        ClientMain.mainWindowController.getLeaderboardController().startRacePlacings();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                clearCanvas();
                Fleet fleet = ClientMain.getRace().getFleet();
                Annotations.assignFleet(fleet);

                Annotations.clearTracks();

                if (course != null) {
                    CourseDrawer.drawCourse(course, graphicsContext, zoomed, zoomFactor);

                    if (fleet.getUserAssignBoat() != null && ClientMain.getRace().getRaceState() == RaceStatus.STARTED) {
                        // Draw the mark rounding arrows only after the race has started
                        CourseDrawer.drawDirectionArrow(fleet.getUserAssignBoat(), graphicsContext, zoomFactor);
                    }


                }
                Annotations.draw();
                fleet.drawBoats(graphicsContext, zoomFactor);

                Annotations.fpsUpdate(frameRateMeasure.measure(now));

                // Move background pane relative to boat if zoomed
                if (zoomed) {
                    double boatX = getXPos(fleet.getUserAssignBoat().getLongitude(), false);
                    double boatY = getYPos(fleet.getUserAssignBoat().getLatitude(), false);
                    bgImageView.setViewport(new Rectangle2D(boatX, boatY, backgroundPane.getWidth(), backgroundPane.getHeight()));
                }
            }
        };
        animationTimer.start();
    }

    /**
     * Clear the canvas.
     */
    private synchronized void clearCanvas() {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Set the dimensions of a given canvas.
     */
    private void setCanvasDimensions() {
        //The minus 4 is to fix the mysterious growing of the anchorpane. It would automatically grow by 4 pixels.

        ClientMain.mainWindowController.getRaceViewAnchor().setMinHeight(ClientMain.getStage().getHeight());
        ClientMain.mainWindowController.getRaceViewAnchor().setMinWidth(ClientMain.getStage().getWidth());


//        canvas.setWidth(ClientMain.mainWindowController.getRaceViewAnchor().getWidth() - 4);
//        canvas.setHeight(ClientMain.mainWindowController.getRaceViewAnchor().getHeight() - 4);

//1680 width
//        984.0 h
//        aresCanvas.setWidth(ClientMain.mainWindowController.getRaceViewAnchor().getWidth() - 4);
//        aresCanvas.setHeight(ClientMain.mainWindowController.getRaceViewAnchor().getHeight() - 4);
    }

    //1010 1050 - out by 40 pixels

    /**
     * Set the course to draw.
     *
     * Called only when client receives a Race.xml.
     *
     * @param course The course to draw.
     */
    public void setupCourse(Course course) {
        setCanvasDimensions();

        this.course = course;
        myCourseController = new CourseController(course, canvas);
        myCourseController.setZoomed(zoomed);
        myCourseController.setZoomFactor(zoomFactor);
        if (zoomed) {
            updateCentreCoords();
            myCourseController.setCentreCoords(centreCoords);
        }
        myCourseController.addCartesianToCourseFeatures();

        scaleGeoToCartesian = myCourseController.getScaleGeoToCartesian();
    }


    private void updateCentreCoords() {
        Fleet fleet = ClientMain.getRace().getFleet();
        double centreLon = fleet.getUserAssignBoat().getLongitude();
        double centreLat = fleet.getUserAssignBoat().getLatitude();
        double canvasCentreX = canvas.getWidth() / 2;
        double canvasCentreY = canvas.getHeight() / 2;
        centreCoords = new ArrayList<>(Arrays.asList(centreLon, centreLat, canvasCentreX, canvasCentreY));
    }

    /**
     * Updates the Speed over ground of a given boat. It is a vector pointing in the direction of the boats motion and is
     * calculated using the difference between a boats current and previous location.
     * @param boat the boat to have its SOG updated
     */
    //TODO: Move the calculations in this server side?
    public void updateSOG(Boat boat) {
        double scale = 20;
        double max = 30;

        double x = getXPos(boat.getLongitude(), ClientMain.raceViewController.isZoomed());
        double y = getYPos(boat.getLatitude(), ClientMain.raceViewController.isZoomed());
        double diffX;
        double diffY;

        if (boatsLastPos.containsKey(boat)) {

            // boatsLastPos key of boat has a value list
            // The list indices are as follows [previous x val, previous y val, diff x, diff y]

            // Diff = CurrentX - PreviousX = delta coords
            diffX = x - boatsLastPos.get(boat).get(0);
            diffY = y - boatsLastPos.get(boat).get(1);

            if(diffX == 0.0 || diffY == 0.0){
                double x1 = boatsLastPos.get(boat).get(2);
                double y1 = boatsLastPos.get(boat).get(3);
                double x2 = boatsLastPos.get(boat).get(4);
                double y2 = boatsLastPos.get(boat).get(5);
                graphicsContext.setStroke(Color.WHITE);
                graphicsContext.setLineWidth(1);
                //Use last vec
                graphicsContext.strokeLine(x1, y1, x2, y2);
            } else {
                //Scale the length of the SOG
                diffX = diffX * scale;
                diffY = diffY * scale;
                //Limits line length to scale + 10
                if (diffX > max){
                    diffX = max;
                }
                if (diffY > max){
                    diffY = max;
                }
                if (diffX < -max){
                    diffX = -max;
                }
                if (diffY < -max){
                    diffY = -max;
                }

                //Finds end point of the SOG vector
                double x2 = x + diffX;
                double y2 = y + diffY;

                //Update the pos list
                boatsLastPos.get(boat).add(0, x);
                boatsLastPos.get(boat).add(1, y);
                boatsLastPos.get(boat).add(2, x2);
                boatsLastPos.get(boat).add(3, y2);
            }
        } else {
            ArrayList arrayList = new ArrayList(Arrays.asList(x, y, x, y, x, y));
            boatsLastPos.put(boat, arrayList);
        }
    }

    /**
     * Draws the SOG vector of a given boat
     * @param boat boat to draw SOG onto
     */
    ///TODO: Move the calculations in this server side?s
    public void drawBoatSOG(Boat boat) {
        graphicsContext.setStroke(Color.WHITE);
        graphicsContext.setLineWidth(1);
        //Use last vec
        ArrayList<Double> arrayList = boatsLastPos.get(boat);
        graphicsContext.strokeLine(arrayList.get(0), arrayList.get(1), arrayList.get(2), arrayList.get(3));
    }

    /**
     * Calculates and draws the VMG vector for a given boat.
     * VMG is the component of SOG directly towards the next objective on the course
     * Warning: there is some hacky code to ensure the next course objective is correctly determined
     * @param boat boat to do VMG of
     */
    //TODO: Move the calculations in this server side?
    public void drawBoatVMG(Boat boat) {

        if(boatsLastPos.containsKey(boat) && getXPos(boat.getLongitude(), ClientMain.raceViewController.isZoomed()) > 0) {

            double boatX = getXPos(boat.getLongitude(), ClientMain.raceViewController.isZoomed());
            double boatY = getYPos(boat.getLatitude(), ClientMain.raceViewController.isZoomed());

            double sogX = boatsLastPos.get(boat).get(2);
            double sogY = boatsLastPos.get(boat).get(3);

            double markX = -1;
            double markY = -1;


//            TODO this is really hacky. Sorry. This is to fix the issue with the new live race having an entry mark that throws out the index of the course features.
            //Because we are running out of time, and have a lack of knowledge about when entry marks will appear, we are just putting in this patch to fix for
            //this sprint.
//            if(course.getCourseFeatures().get(0).getMedia().toLowerCase().contains("entry")){
//                markX = course.getCourseFeatureById(boat.getLegNumber() + 2).getMidPoint().getX();
//                markY = course.getCourseFeatureById(boat.getLegNumber() + 2).getMidPoint().getY();
//            }else{
            //TODO: this is not how to get the next mark, use course order.
            if(boat.getCourseProgress()+1 < course.getCourseFeatures().size()){
                markX = course.getCourseFeatureById(boat.getCourseProgress() + 1).getMidPoint().getX();
                markY = course.getCourseFeatureById(boat.getCourseProgress() + 1).getMidPoint().getY();
            }

            //from boat to mark
            double boatMarkX = markX - boatX;
            double boatMarkY = markY - boatY;

            //from boat to sog
            double boatSOGX = (sogX - boatX);
            double boatSOGY = (sogY - boatY);

            double dotProductSogMark = (boatMarkX * boatSOGX) + (boatMarkY * boatSOGY); // finds the dot product to work out angle of boat to mark

            if(dotProductSogMark > 0) { //means boat is heading towards the mark.

                double sogDotMark = (boatMarkX * boatSOGX) + (boatMarkY * boatSOGY);

                double denom = (boatMarkX * boatMarkX) + (boatMarkY * boatMarkY);

                double finalPX = boatX + boatMarkX * (sogDotMark / denom);
                double finalPY = boatY + boatMarkY * (sogDotMark / denom);

                //Draws the SOG
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setLineWidth(1);
                //annotationGc.strokeLine(boatX, boatY, boatX+finalPX, boatY+finalPY);
                graphicsContext.strokeLine(boatX, boatY, finalPX, finalPY);

                double[] xPoints = {boatX, sogX, finalPX};
                double[] yPoints = {boatY, sogY, finalPY};

                //add a shaded polygon between the VMG and SOG in an opaque version of the boat colour
                graphicsContext.setFill(boat.getFillColor().deriveColor(1, 1, 1, 0.3));
                graphicsContext.fillPolygon(xPoints, yPoints, 3);
            }
        }
    }

    /**
     * Get the Y position of a geo lat
     *
     * @param lat getting y position for this lat
     * @return Y position of the lat
     */
    public double getYPos(double lat, boolean zoomed) {
        double y;
        if (myCourseController == null) {
            return 0;
        }

        double centreLat;
        double canvasCentreY;
        if (zoomed) {   // if zoomed, we need to centre the view on the boat's position
            updateCentreCoords();
            centreLat = centreCoords.get(1);
            canvasCentreY = centreCoords.get(3);
            y = (centreLat - lat) * scaleGeoToCartesian * zoomFactor + canvasCentreY;
        } else {
            centreLat = myCourseController.getCenterLat();
            canvasCentreY = canvas.getHeight() / 2;
            y = (centreLat - lat) * scaleGeoToCartesian + canvasCentreY;
        }
        return y;
    }

    /**
     * Get the X position of a geo lon
     *
     * @param lon getting y position for this lon
     * @return X position of the lon
     */
    public double getXPos(double lon, boolean zoomed) {
        double x;
        if (myCourseController == null) {
            return 0;
        }

        double centreLon;
        double canvasCentreX;
        if (zoomed) {   // if zoomed, we need to centre the view on the boat's position
            updateCentreCoords();
            centreLon = centreCoords.get(0);
            canvasCentreX = centreCoords.get(2);
            x = (lon - centreLon) * scaleGeoToCartesian * zoomFactor + canvasCentreX;
        } else {
            centreLon = myCourseController.getCenterLon();
            canvasCentreX = canvas.getWidth() / 2;
            x = (lon - centreLon) * scaleGeoToCartesian + canvasCentreX;
        }
        return x;
    }

    /**
     * Draws the boats estimated time to the next mark at a position on the canvas
     *
     * @param boat    Boat to put measurement next to
     * @param offsetX offset x from boat
     * @param offsetY offset y from boat
     */
    private void drawBoatTimeNextMark(Boat boat, int offsetX, int offsetY) {
        String label = String.format("%.0f mins", boat.getSpeed());
        graphicsContext.setFill(boat.getStrokeColor());
        graphicsContext.fillText(label, boat.getAnnotationX() - offsetX, boat.getAnnotationY() - offsetY);
    }

    /**
     * Set zoomed to true, set the centre object to the player controlled boat, and
     * increment the zoom factor, by 0.25, up to maximum of 3.5.
     */
    private void incrementZoom() {
        backgroundPane.setScaleX(zoomFactor);
        if (zoomFactor + 0.25 >= 4) {
            zoomFactor = 4;
        } else {
            zoomFactor += 0.25;
            zoomed = true;
        }
        backgroundPane.setScaleX(zoomFactor);
        backgroundPane.setScaleY(zoomFactor);
    }

    /**
     * Decrement the zoom, by 0.25, down to a minimum of 1.
     * If zoom factor 1 is reached, set zoomed to false and set
     * the centre object to null.
     */
    private void decrementZoom() {
        if (zoomFactor - 0.25 <= 1) {
            zoomFactor = 1;
            zoomed = false;

            // Reset the course after un-zooming.
            myCourseController = new CourseController(course, canvas);
            myCourseController.setZoomed(zoomed);
            myCourseController.setZoomFactor(zoomFactor);
            myCourseController.addCartesianToCourseFeatures();

            // Reset background.
            bgImageView.setViewport(new Rectangle2D(0, 0, backgroundPane.getWidth(), backgroundPane.getHeight()));
        } else {
            zoomFactor -= 0.25;
        }
        backgroundPane.setScaleX(zoomFactor);
        backgroundPane.setScaleY(zoomFactor);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Integer) {
            int action = ((int) arg);
            if (action == 100) {
                incrementZoom();
            } else if (action == 101) {
                decrementZoom();
            }
        } else if (arg instanceof Course) {
            setupCourse(((Course) arg));
        }
    }

    ////////////////////////////////////////////
    // Only getters and setters from here on. //
    ////////////////////////////////////////////

    public boolean isZoomed() {
        return zoomed;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public GraphicsContext getGraphicsContext() {
        return graphicsContext;
    }


    public double getScaleGeoToCartesian() {
        return scaleGeoToCartesian;
    }

    public AnimationTimer getAnimationTimer() {
        return animationTimer;
    }
}