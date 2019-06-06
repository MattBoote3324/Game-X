package seng302.Common;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import seng302.Client.ClientMain;
import seng302.Client.Controllers.BoatDrawer;
import seng302.Common.Utils.Calculator;
import seng302.Server.ServerDataStream;
import seng302.Server.ServerMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * group of boats that are racing
 */

public class Fleet {
    private List<Boat> aresAbilityOnList = new ArrayList<>();


    private final long TRACK_INTERVAL = 400;
    private long timeStamp = 0;

    private ObservableList<Boat> boats = FXCollections.observableArrayList(new Callback<Boat, Observable[]>() {

        @Override
        public Observable[] call(Boat param) {
            return new Observable[]{
                    param.speedProperty()
            };
        }
    });

    private int userAssignId;

    public Fleet() {

    }

    public Fleet(List<Boat> boats){
        this.boats.addAll(FXCollections.observableArrayList(boats));
    }

    public void addBoat(Boat boat) {
        boats.add(boat);
    }

    public Boat getBoat(int sourceID) {
        for (Boat boat : boats) {
            if (boat.getSourceID() == sourceID) {
                return boat;
            }
        }
        return null;
    }

    public ObservableList<Boat> getBoats() {
        return boats;
    }

    public int getSize() {
        return boats.size();
    }

    /**
     * Draw the boat tracks of the track canvas
     * @param gc canvas which to draw
     * @param currTime the current timestamp when the method was called
     */
    void drawTracks(GraphicsContext gc, long currTime) {
        if(boats.size() > 0) { //competing boats are being initialised with no boats being added to it.
            if (currTime - timeStamp > TRACK_INTERVAL) {
                timeStamp = currTime;

                for (Boat boat : boats) {
                    boat.addTrackPoint(boat.toPoint());
                }
            }
            for (Boat boat : boats) {
                drawBoatTrackDot(gc, boat);
            }
        }
    }


    /**
     * clears each individual track point per boats on the canvas
     * and keeps the track point list updated every 200 millis
     * @param gc graphics context for the race canvas
     * @param currTime the current timestamp when the method was called
     */
    void clearTracks(GraphicsContext gc, long currTime){
        if(boats.size() > 0) {

            if (currTime - timeStamp > TRACK_INTERVAL) {
                timeStamp = currTime;
                for (Boat boat : boats) {
                    boat.addTrackPoint(boat.toPoint());
                }
            }
            for (Boat boat : boats) {
                for (Point trackPoint : boat.getTrackPoints()) {
                    gc.clearRect(ClientMain.raceViewController.getXPos(trackPoint.getLongitude(), ClientMain.raceViewController.isZoomed()), ClientMain.raceViewController.getYPos(trackPoint.getLatitude(), ClientMain.raceViewController.isZoomed()), 1.5 * ClientMain.raceViewController.getZoomFactor(), 1.5* ClientMain.raceViewController.getZoomFactor());
                }

            }
        }
    }

    /**
     * Draws the icons that represent the god that a boat has chosen
     * @param gc the GraphicsContext of the canvas
     * @param annotationOffset an integer to offset the icon from the boat based on how many annotations are on.
     */
    void drawGodIcons(GraphicsContext gc, int annotationOffset){
        if(boats.size() > 0) { //competing boats are being initialised with no boats being added to it.
            for(Boat boat : boats) {
                Image icon = boat.getGreekGod().getGodIcon();
                gc.drawImage(icon, boat.getAnnotationX() - 10, boat.getAnnotationY() - annotationOffset - 25, 25, 25);
            }
        }
    }

    /**
     * Draws a circle at the current x,y position of the given boat,
     * called within the timeline to draw a full line of circles behind each boat
     * to represent the track
     * @param boat boat to be drawn
     */
    private void drawBoatTrackDot(GraphicsContext gc, Boat boat) {
        for(Point point : boat.getTrackPoints()) {
            gc.setFill(boat.getFillColor().deriveColor(1, 1, 1, 0.5));
            gc.fillOval(ClientMain.raceViewController.getXPos(point.getLongitude(), ClientMain.raceViewController.isZoomed()), ClientMain.raceViewController.getYPos(point.getLatitude(), ClientMain.raceViewController.isZoomed()), 3 * ClientMain.raceViewController.getZoomFactor(), 3* ClientMain.raceViewController.getZoomFactor());
        }
    }

    /**
     * If the annotation is set, then Draw the speed of the boat
     *
     * @param gc the graphics context to draw the annotation to
     * @param offsetY desired annotation y offset
     */
    int showSpeed(GraphicsContext gc, int offsetY) {
        for (Boat boat : boats) {
            String label = String.format("%.0f knots", boat.getSpeed());
            gc.setFill(boat.getStrokeColor());
            gc.fillText(label, boat.getAnnotationX() - 10, boat.getAnnotationY() - offsetY);

        }
        // If we used the annotation, then increment the offset for the next one
        offsetY += Annotations.ANNOTATION_OFFSET_SPACE;
        return offsetY;
    }

    /**
     * Draw the annotation displaying the name of the boat
     * @param gc the canvas to draw the annotation to
     * @param offsetY desired annotation Y offset
     */
    int showNames(GraphicsContext gc, int offsetY) {
        for (Boat boat : boats) {
            String label = String.format("%s", boat.getAbbreviatedName());

            gc.setFill(boat.getStrokeColor());
            gc.fillText(label, boat.getAnnotationX() - 10, boat.getAnnotationY() - offsetY);
        }
        // If we used the annotation, then increment the offset for the next one
        offsetY += Annotations.ANNOTATION_OFFSET_SPACE;
        return offsetY;
    }

    /**
     * this will need tidied up externally before it can be in this function
     */
    void showVMG_SOG() {
        if(ClientMain.raceViewController.isZoomed()){
            return;
        }
        for (Boat boat : boats) {
                ClientMain.raceViewController.updateSOG(boat);
                ClientMain.raceViewController.drawBoatSOG(boat);
                ClientMain.raceViewController.drawBoatVMG(boat);
        }
    }

    /**
     * Draws the boats time since it passed the last mark
     * @param gc the context to draw annotation to
     * @param offsetY offset y from boat
     */
    int showTimeSinceMark(GraphicsContext gc, int offsetY) {

        for (Boat boat : boats) {
            long timeSinceMark = boat.getTimeSinceLastMark();
            String label;
            if (timeSinceMark > 59) {
                label = String.format("%d min, %d sec",
                        TimeUnit.SECONDS.toMinutes(timeSinceMark),
                        TimeUnit.SECONDS.toSeconds(timeSinceMark) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.SECONDS.toMinutes(timeSinceMark))
                );
            } else {
                label = String.format("%d sec", TimeUnit.SECONDS.toSeconds(timeSinceMark));
            }

            gc.setFill(boat.getStrokeColor());
            gc.fillText(label, boat.getAnnotationX() - 10, boat.getAnnotationY() - offsetY);
        }
        // If we used the annotation, then increment the offset for the next one
        offsetY += Annotations.ANNOTATION_OFFSET_SPACE;

        return offsetY;
    }

    /**
     * draws all the boats in the fleet
     * @param graphicsContext the graphicsContext of the canvas being drawn on.
     * @param zoomFactor the zoomFactor for when user has zoomed.
     */
    public void drawBoats(GraphicsContext graphicsContext, double zoomFactor) {
        drawBoats(graphicsContext, zoomFactor, true);
    }

    /**
     * draws all the boats in the fleet
     * @param graphicsContext the graphicsContext of the canvas being drawn on.
     * @param zoomFactor the zoomFactor for when user has zoomed.
     * @param drawWakes whether or not to draw wakes!
     */
    public void drawBoats(GraphicsContext graphicsContext, double zoomFactor, boolean drawWakes) {
        if (drawWakes) {
            for (Boat boat : boats) {
                boat.addWakePoint(boat.toPoint());
            }
            for (Boat boat : boats) {
                BoatDrawer.drawWake(boat, graphicsContext, zoomFactor);
            }
        }
        for (Boat boat: boats) {
            BoatDrawer.drawOars(boat, graphicsContext, zoomFactor);
        }
        for (Boat boat: boats) {
            BoatDrawer.drawBoat(boat, graphicsContext, zoomFactor);
        }
        for (Boat boat: boats) {
            BoatDrawer.drawSail(boat, ClientMain.getRace(), graphicsContext, zoomFactor);
        }
        for (Boat boat: boats) {
            BoatDrawer.drawFlag(boat, ClientMain.getRace().getWindDirection(), graphicsContext, zoomFactor);
        }
    }

    /**
     * Checks for collisions between any two boats, and calls handleCollision for both colliding boats
     */
    public void checkForBoatCollision() {
        double boatLength = 0.0015;
        double boatWidth = 0.00025;
        for (Boat boat1 : boats) {
            if (!boat1.isFinished()) {
                List<Point> points1 = Arrays.asList(
                        Calculator.projectedPoint(boat1.getLatitude(), boat1.getLongitude(), boatLength / 2.0, boat1.getHeading()),
                        Calculator.projectedPoint(boat1.getLatitude(), boat1.getLongitude(), boatLength / 2.0, boat1.getHeading() + 180));
                for (Boat boat2 : boats) {
                    if (boat1.getSourceID() != boat2.getSourceID() && !boat2.isFinished()) {
                        List<Point> points2 = Arrays.asList(
                                Calculator.projectedPoint(boat2.getLatitude(), boat2.getLongitude(), boatLength / 2.0, boat2.getHeading()),
                                Calculator.projectedPoint(boat2.getLatitude(), boat2.getLongitude(), boatLength / 2.0, boat2.getHeading() + 180));
                        boat1.checkForPointCollision(points2, boatWidth / 2.0);
                        boat2.checkForPointCollision(points1, boatWidth / 2.0);
                    }
                }
            }
        }
    }

    /**
     * For each boat in the fleet it checks whether they are leaving the bounds and
     * sets their heading to the last known location that they were in.
     */
    public void checkCrossingBoundary() {
        for(Boat boat: boats){

            if(!ServerDataStream.isInBoundary(boat)){
                //Not in bounds
                //Work out heading to the previous lastIn positions
                double lastLat = boat.getLastLat();
                double lastLong = boat.getLastLong();
                double currentLat = boat.getLatitude();
                double currentLong = boat.getLongitude();
                Point point1 = new Point(currentLat,currentLong);
                Point point2 = new Point(lastLat, lastLong);

                double newHeading = Calculator.bearingBetweenPoints(point1,point2);

                boat.setHeading(newHeading);

            } else {
                //In boundary
                boat.setLast();
                boat.setInBounds(true);
            }
        }
    }


    public int getUserAssignId() {
        return userAssignId;
    }

    public void setUserAssignId(int userAssignId) {
        this.userAssignId = userAssignId;
    }

    public Boat getUserAssignBoat() {
        return getBoat(userAssignId);
    }

    /**
     * Remove the boat from the boat list
     *
     * @param boatToRemove Boat that should be removed from the fleet
     */
    public void remove(Boat boatToRemove) {
        boats.remove(boatToRemove);
    }

    public List<Boat> getAresAbilityOnList() {
        return aresAbilityOnList;
    }

    public void addAresAbilityOnList(Boat boat) {
        this.aresAbilityOnList.add(boat);
    }
    public void removeAresAbilityOnList(Boat boat){
        this.aresAbilityOnList.remove(boat);
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
