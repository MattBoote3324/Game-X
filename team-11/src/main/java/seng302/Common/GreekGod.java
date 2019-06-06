package seng302.Common;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Polygon;
import seng302.Server.Model.Race;

/**
 * Created by mbo57 on 26/08/17.
 */
public abstract class GreekGod {
    private GodType godType;


    protected final double COOLDOWN_PERIOD = 5000;
    protected final double ICON_SIZE = 100;

    /**
     * corresponds to the Q key press for an ability
     * @param race race
     * @param boatID boatid
     */
    public void abilityQ(Race race, int boatID){
        Boat sourceBoat = race.getFleet().getBoat(boatID);
        double heading = Math.toRadians(sourceBoat.getHeading());
        double maxDistance = 0.004;
        double width = 0.0007;

//        double startX = boat.getX() + (20 * imageScale * Math.cos(angle) + BoatDrawer.BOAT_HEIGHT / 2 * Math.sin(angle)) * zoomFactor;
//        double startY = boat.getY() + (20 * imageScale * Math.sin(angle) - BoatDrawer.BOAT_HEIGHT / 2 * Math.cos(angle)) * zoomFactor;

        Polygon abilityArea = new Polygon();

        Point point1 = new Point(sourceBoat.getLatitude() + width / 2 * Math.sin(heading),
                sourceBoat.getLongitude() - width / 2 * Math.cos(heading));
        Point point2 = new Point(sourceBoat.getLatitude() - width / 2 * Math.sin(heading),
                sourceBoat.getLongitude() + width / 2 * Math.cos(heading));
        Point point3 = new Point(sourceBoat.getLatitude() - width / 2 * Math.sin(heading) + maxDistance * Math.cos(heading),
                sourceBoat.getLongitude() + width / 2 * Math.cos(heading) + maxDistance * Math.sin(heading));
        Point point4 = new Point(sourceBoat.getLatitude() + width / 2 * Math.sin(heading) + maxDistance * Math.cos(heading),
                sourceBoat.getLongitude() - width / 2 * Math.cos(heading) + maxDistance * Math.sin(heading));

        abilityArea.getPoints().addAll(point1.getLatitude(), point1.getLongitude(),
                point2.getLatitude(), point2.getLongitude(),
                point3.getLatitude(), point3.getLongitude(),
                point4.getLatitude(), point4.getLongitude());

        for (Boat boat : race.getFleet().getBoats()) {
            if (boat.getSourceID() != boatID) {
                if (abilityArea.contains(boat.getLatitude(), boat.getLongitude())) {
                    Point currentPoint;
                    if(boat.getCourseProgress()>0) {
                        currentPoint = race.getCourse().getCourseFeatureById(boat.getCourseProgress()).getMidPoint();
                    }else{
                        currentPoint = race.getCourse().getCourseFeatureById(1).getMidPoint();
                    }
                    boat.setLatitude(currentPoint.getLatitude());
                    boat.setLongitude(currentPoint.getLongitude());
                }
            }
        }
    }

    /**
     * corresponds to the W key press for an individual ability
     * @param race race
     * @param boatID boatid
     */
    public abstract void abilityW(Race race, int boatID);

    /**
     * returns the godType Enum that represents the class
     * @return GodType of the class
     */
    public abstract GodType getGodType();

    /**
     * returns the image that is used to represent the god
     * @return ImageView of the god.
     */
    public abstract ImageView getGodImage();

    /**
     * returns the icon that represents the god during a race
     * @return ImageView of the icon.
     */
    public abstract Image getGodIcon();


    public abstract double getCOOLDOWN_PERIOD() ;


}
