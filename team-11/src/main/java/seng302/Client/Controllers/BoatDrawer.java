package seng302.Client.Controllers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import seng302.Client.ClientMain;
import seng302.Common.Boat;
import seng302.Common.Point;
import seng302.Server.Model.Race;

import java.util.HashMap;
import java.util.Iterator;

import static java.lang.Math.abs;
import static java.lang.Math.floorMod;
import static java.lang.Math.toRadians;

/**
 * Render any given boat onto any given graphics context.
 */
public class BoatDrawer {
    public static final int BOAT_HEIGHT = 12;
    public static final int BOAT_WIDTH = 10;

    private static Image highlightImage = new Image("images/boatHighlight.png");
    private static Image boatImage = new Image("images/greekBoat.png");

    private static HashMap<Integer, ImageView> boatViews = new HashMap<>();

    // Storage for the sail angle for each boat, so they can be changed incrementally
    private static HashMap<Integer, Double> sailAngles = new HashMap<>();

    /**
     * Draw a triangle on the boat canvas representing a boat.
     * Also draw the boat's sail, and wake.
     *
     * @param boat The boat to be drawn.
     * @param graphicsContext graphicsContext to be drawn to
     * @param zoomFactor zoomFactor that is being used
     */
    public static void drawBoat(Boat boat, GraphicsContext graphicsContext, double zoomFactor) {
        boat.setAnnotationX(boat.getX() - 10);
        boat.setAnnotationY(boat.getY() - 20 * zoomFactor);

        double heading = boat.getHeading();
        double scale = 0.1 * zoomFactor; // Image scale

        graphicsContext.save();
        Rotate r = new Rotate(heading, boat.getX(), boat.getY());
        graphicsContext.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());

        if (boat.isAssigned()) {
            graphicsContext.drawImage(highlightImage,
                    boat.getX() - scale * highlightImage.getWidth() / 2.0,
                    boat.getY() - scale * highlightImage.getHeight() / 2.0,
                    highlightImage.getWidth() * scale,
                    highlightImage.getHeight() * scale);
        }

        graphicsContext.drawImage(boatImage,
                boat.getX() - scale * boatImage.getWidth() / 2.0,
                boat.getY() - scale * boatImage.getHeight() / 2.0,
                boatImage.getWidth() * scale,
                boatImage.getHeight() * scale);
        graphicsContext.restore();

//        //TODO UNCOMMENT TO SEE ARES W HIT BOX
//        double width = ClientMain.raceViewController.getScaleGeoToCartesian()*Boat.ARES_ABILITY_RADIUS_LATLONG * zoomFactor;
//        graphicsContext.setStroke(Color.BLACK);
//        graphicsContext.setLineWidth(2);
//        graphicsContext.strokeOval(boat.getX() - (width),boat.getY() - width, width*2, width*2);
//
//
//
//        //TODO UNCOMMENT TO SEE HIT BOXES
//        double maxDistance = 0.004;
//        width = 0.0007;
//
//        heading = Math.toRadians(heading);
//        Point point1 = new Point(boat.getLatitude() + width / 2 * Math.sin(heading),
//                boat.getLongitude() - width / 2 * Math.cos(heading));
//        Point point2 = new Point(boat.getLatitude() - width / 2 * Math.sin(heading),
//                boat.getLongitude() + width / 2 * Math.cos(heading));
//        Point point3 = new Point(boat.getLatitude() - width / 2 * Math.sin(heading) + maxDistance * Math.cos(heading),
//                boat.getLongitude() + width / 2 * Math.cos(heading) + maxDistance * Math.sin(heading));
//        Point point4 = new Point(boat.getLatitude() + width / 2 * Math.sin(heading) + maxDistance * Math.cos(heading),
//                boat.getLongitude() - width / 2 * Math.cos(heading) + maxDistance * Math.sin(heading));
//
//        double[] xdummyPoints = new double[5];
//        double[] ydummyPoints = new double[5];
//
//        xdummyPoints[0] = ClientMain.raceViewController.getXPos(point1.getLongitude());
//        xdummyPoints[1] = ClientMain.raceViewController.getXPos(point2.getLongitude());
//        xdummyPoints[2] = ClientMain.raceViewController.getXPos(point3.getLongitude());
//        xdummyPoints[3] = ClientMain.raceViewController.getXPos(point4.getLongitude());
//        xdummyPoints[4] = ClientMain.raceViewController.getXPos(point1.getLongitude());
//
//        ydummyPoints[0] = ClientMain.raceViewController.getYPos(point1.getLatitude());
//        ydummyPoints[1] = ClientMain.raceViewController.getYPos(point2.getLatitude());
//        ydummyPoints[2] = ClientMain.raceViewController.getYPos(point3.getLatitude());
//        ydummyPoints[3] = ClientMain.raceViewController.getYPos(point4.getLatitude());
//        ydummyPoints[4] = ClientMain.raceViewController.getYPos(point1.getLatitude());
//
//        graphicsContext.setStroke(Color.GREEN);
//        graphicsContext.setLineWidth(1);
//        graphicsContext.strokePolyline(xdummyPoints, ydummyPoints, 5);
//
//
//        maxDistance = 138;
//        width = 25;
//
//        point1 = new Point();
//        point1.setY(boat.getY() - width / 2 * Math.sin(heading));
//        point1.setX(boat.getX() - width / 2 * Math.cos(heading));
//
//        point2 = new Point();
//        point2.setY(boat.getY() + width / 2 * Math.sin(heading));
//        point2.setX(boat.getX() + width / 2 * Math.cos(heading));
//
//        point3 = new Point();
//        point3.setY(boat.getY() + width / 2 * Math.sin(heading) - maxDistance * Math.cos(heading));
//        point3.setX(boat.getX() + width / 2 * Math.cos(heading) + maxDistance * Math.sin(heading));
//
//        point4 = new Point();
//        point4.setY(boat.getY() - width / 2 * Math.sin(heading) - maxDistance * Math.cos(heading));
//        point4.setX(boat.getX() - width / 2 * Math.cos(heading) + maxDistance * Math.sin(heading));
//
//        xdummyPoints = new double[5];
//        ydummyPoints = new double[5];
//
//        xdummyPoints[0] = point1.getX ();
//        xdummyPoints[1] = point2.getX ();
//        xdummyPoints[2] = point3.getX ();
//        xdummyPoints[3] = point4.getX ();
//        xdummyPoints[4] = point1.getX ();
//
//        ydummyPoints[0] = point1.getY();
//        ydummyPoints[1] = point2.getY();
//        ydummyPoints[2] = point3.getY();
//        ydummyPoints[3] = point4.getY();
//        ydummyPoints[4] = point1.getY();
//
//        graphicsContext.setStroke(Color.RED);
//        graphicsContext.setLineWidth(1);
//        graphicsContext.strokePolyline(xdummyPoints, ydummyPoints, 5);
    }

    /**
     * Check which point on the boat's triangle representation is at the lowest Y position (highest on the
     * screen) so that when annotations are drawn, they can use this lowest Y as the starting point to offset from.
     * Overall, this prevents annotations form overlapping the boat's triangle.
     *
     * @param xPoints {baseP1X, baseP2X, triangleApexX} from drawBoat() calculations.
     * @param yPoints {baseP1Y, baseP2Y, triangleApexY} from drawBoat() calculations.
     * @param boat    The boat to set the annotation point for.
     */
    private static void determineBoatAnnotationPoint(double[] xPoints, double[] yPoints, Boat boat) {
        double annotationX = xPoints[2];
        double annotationY = yPoints[2];
        if (yPoints[0] < annotationY) {
            annotationX = xPoints[0];
            annotationY = yPoints[0];
        }
        if (yPoints[1] < annotationY) {
            annotationX = xPoints[1];
            annotationY = yPoints[1];
        }
        boat.setAnnotationX(annotationX);
        boat.setAnnotationY(annotationY);
    }

    /**
     * Draw a line behind the boat to represent a wake.
     *
     * @param boat The boat to be drawn.
     * @param graphicsContext graphicsContext to be drawn to
     * @param zoomFactor zoomfactor that is being used
     */
    public static void drawWake(Boat boat, GraphicsContext graphicsContext, double zoomFactor) {
        //TODO: needs optimising.

        double angle = Math.toRadians(boat.getHeading() - 90);

        double startX = boat.getX() + BOAT_HEIGHT / 2.0 * Math.cos(angle);
        double startY = boat.getY() + BOAT_HEIGHT / 2.0 * Math.sin(angle);

        graphicsContext.beginPath();
        graphicsContext.moveTo(startX, startY);

        graphicsContext.setLineWidth(zoomFactor);

        // The wake is made up of lines between boat track points
        int maxLineCount = 12;

        int lineCount = 0;
        int stepCount = 0;

        Iterator<Point> wakeIterator = boat.getWakePoints().descendingIterator();

        // Loop through the boat's track points, connecting lines between every fourth point..
        for (Point point; wakeIterator.hasNext(); ) {
            point = wakeIterator.next();
            if (stepCount % 8 == 0) {
                Color color = Color.rgb(255, 255, 255, ((double)(maxLineCount - lineCount)/(double)maxLineCount));
                graphicsContext.setStroke(color);
                graphicsContext.lineTo(ClientMain.raceViewController.getXPos(point.getLongitude(), ClientMain.raceViewController.isZoomed()), ClientMain.raceViewController.getYPos(point.getLatitude(), ClientMain.raceViewController.isZoomed()));
                graphicsContext.stroke();
                lineCount++;
            }
            stepCount++;
            if (lineCount == maxLineCount) {
                // Wake has reached maximum defined length.
                break;
            }
        }
    }

    /**
     * Draws a boat's sail.
     *
     * @param boat The boat to be drawn.
     * @param race The race to get the wind from
     * @param graphicsContext graphicsContext to be drawn to
     * @param zoomFactor zoomfactor that is being used
     */
    public static void drawSail(Boat boat, Race race, GraphicsContext graphicsContext, double zoomFactor) {
        double mastDistance = 5 * zoomFactor;  //how far forward the mast is from the centre of the boat
        double sailLength = 24; //how long the sail is
        double luffingSpeed = 10; //maximum speed at which the sail will luff;
        double sailSpeedScale = 0.25; //Scale by which the speed of sail motion is applied
        double luffingTime = 0.01; // Time scale for flappingness
        double sailThiccness = 3; // how big the sail is

        double windDirection = race.getWindDirection();
        double windSpeed = race.getWindSpeed();

        double heading = boat.getHeading();
        double trueWindAngle = Math.floorMod((long) (heading - windDirection), 360);

        double mastX = boat.getX() + mastDistance * Math.sin(Math.toRadians(heading));
        double mastY = boat.getY() - mastDistance * Math.cos(Math.toRadians(heading));

        //amount of luff, between 0 (full sail) and 1 (fully flapping)
        double luffingRatio = Math.max(0, 1 - Math.sqrt(boat.getSpeed() / luffingSpeed));

        // calculate desired sail angle
        double targetAngle = Math.floorMod((long) (heading - trueWindAngle / 2), 360);
        if (boat.getSpeed() < 1) { targetAngle = windDirection; }

        double sailAngle;
        if (sailAngles.containsKey(boat.getSourceID())) {
            sailAngle = sailAngles.get(boat.getSourceID());
        } else {
            sailAngle = targetAngle;
        }

        // If sail angle is "close enough" to target, set it to target
        if (abs(targetAngle - sailAngle) < sailSpeedScale * windSpeed ||
                abs((targetAngle + 180) % 360 - sailAngle) < sailSpeedScale * windSpeed) {
            sailAngle = targetAngle;
        // otherwise increment or decrement
        } else if (floorMod((long) (sailAngle - windDirection), 360) > floorMod((long) (targetAngle - windDirection), 360) ||
                floorMod((long) (sailAngle - windDirection), 360) > floorMod((long) ((targetAngle + 180) % 360 - windDirection), 360)) {
            sailAngle = Math.floorMod((long) (sailAngle - sailSpeedScale * windSpeed), 360);
        } else {
            sailAngle = Math.floorMod((long) (sailAngle + sailSpeedScale * windSpeed), 360);
        }
        sailAngles.put(boat.getSourceID(), sailAngle);

        int N = 16; //number of points on the sail
        int stripeWidth = 2; //width (number of points) of the stripes

        double[] sailXPoints = new double[N + 1];
        double[] sailYPoints = new double[N + 1];

        double[] stripeXPoints = new double[N + N / stripeWidth];
        double[] stripeYPoints = new double[N + N / stripeWidth];

        // Setting the ends of the sail (next 8 statements)
        sailXPoints[0] = mastX + sailLength / 2 * zoomFactor * Math.sin(Math.toRadians(sailAngle));
        sailYPoints[0] = mastY - sailLength / 2 * zoomFactor * Math.cos(Math.toRadians(sailAngle));

        sailXPoints[N] = mastX - sailLength / 2 * zoomFactor * Math.sin(Math.toRadians(sailAngle));
        sailYPoints[N] = mastY + sailLength / 2 * zoomFactor * Math.cos(Math.toRadians(sailAngle));

        stripeXPoints[0] = sailXPoints[0];
        stripeYPoints[0] = sailYPoints[0];

        stripeXPoints[N + N / stripeWidth - 1] = sailXPoints[N];
        stripeYPoints[N + N / stripeWidth - 1] = sailYPoints[N];

        int stripeStep = 1; // count for stripes

        for (int i = 1; i < N; i++) {
            double boomX = mastX - sailLength / N * zoomFactor *(i - N / 2) * Math.sin(Math.toRadians(sailAngle));
            double boomY = mastY + sailLength / N * zoomFactor *(i - N / 2) * Math.cos(Math.toRadians(sailAngle));

            double luffX = 0, luffY = 0, fullX = 0, fullY = 0;
            // Calculates the position of each point along the sail
            if (luffingRatio > 0) {
                if (floorMod((long) (sailAngle - windDirection), 180) < 90) {
                    luffX = boomX + sailLength / N * zoomFactor * Math.cos(Math.toRadians(windDirection)) * Math.sin(System.currentTimeMillis() * luffingTime - i);
                    luffY = boomY + sailLength / N * zoomFactor * Math.sin(Math.toRadians(windDirection)) * Math.sin(System.currentTimeMillis() * luffingTime - i);
                } else {
                    luffX = boomX + sailLength / N * zoomFactor * Math.cos(Math.toRadians(windDirection)) * Math.sin(System.currentTimeMillis() * luffingTime + i);
                    luffY = boomY + sailLength / N * zoomFactor * Math.sin(Math.toRadians(windDirection)) * Math.sin(System.currentTimeMillis() * luffingTime + i);
                }
            }
            if (luffingRatio < 1) {
                fullX = boomX - sailThiccness * sailLength / N * zoomFactor * Math.sin(Math.toRadians(windDirection)) * Math.sin(i * Math.PI / N);
                fullY = boomY + sailThiccness * sailLength / N * zoomFactor * Math.cos(Math.toRadians(windDirection)) * Math.sin(i * Math.PI / N);
            }

            sailXPoints[i] = luffX * luffingRatio + fullX * (1 - luffingRatio);
            sailYPoints[i] = luffY * luffingRatio + fullY * (1 - luffingRatio);

            if (i % (stripeWidth * 2) <= stripeWidth && i % (stripeWidth * 2) > 0) {
                stripeXPoints[stripeStep] = boomX;
                stripeYPoints[stripeStep] = boomY;
                stripeStep++;
            }
            if (i % (stripeWidth * 2) >= stripeWidth || i % (stripeWidth * 2) == 0) {
                stripeXPoints[stripeStep] = sailXPoints[i];
                stripeYPoints[stripeStep] = sailYPoints[i];
                stripeStep++;
            }
            if (i % (stripeWidth * 2) == 0) {
                stripeXPoints[stripeStep] = boomX;
                stripeYPoints[stripeStep] = boomY;
                stripeStep++;
            }
        }

        // Draw the sail.
        graphicsContext.setFill(Color.rgb(255,255,255,0.8));
        graphicsContext.fillPolygon(sailXPoints, sailYPoints, N + 1);

        // Draw the stripes
        graphicsContext.setFill(boat.getFillColor().deriveColor(1, 1, 1, 0.8)); //slightly transparent sail bits
        graphicsContext.fillPolygon(stripeXPoints, stripeYPoints, N + N / stripeWidth);

        // Draw the boom
        graphicsContext.setStroke(Color.rgb(159, 91, 0, 1.0)); // brown
        graphicsContext.setLineWidth(0.8 * zoomFactor);
        graphicsContext.strokeLine(sailXPoints[0], sailYPoints[0], sailXPoints[N], sailYPoints[N]);
    }

    /**
     * Draw a flag on the boat.
     * @param boat The boat for which to draw the flag
     * @param windDirection The direction the flag will point
     * @param graphicsContext The context for drawing the flag
     * @param zoomFactor The zoomFactor
     */
    public static void drawFlag(Boat boat, double windDirection, GraphicsContext graphicsContext, double zoomFactor) {
        int N = 8; // number of flag points
        double length = 6;
        double mastDistance = 5 * zoomFactor;
        double luffRatio = 0.5;
        double luffSpeed = 0.01;

        double[] flagXPoints = new double[N + 1];
        double[] flagYPoints = new double[N + 1];

        double heading = boat.getHeading();

        double mastX = boat.getX() + mastDistance * Math.sin(Math.toRadians(heading));
        double mastY = boat.getY() - mastDistance * Math.cos(Math.toRadians(heading));

        flagXPoints[0] = mastX;
        flagYPoints[0] = mastY;

        // loops through the flag points
        for (int i = 1; i < N; i++) {
            double flagX = mastX - length / N * zoomFactor * (i * Math.sin(Math.toRadians(windDirection)) -
                    luffRatio * Math.cos(Math.toRadians(windDirection)) * Math.sin(System.currentTimeMillis() * luffSpeed - i));
            double flagY = mastY + length / N * zoomFactor * (i * Math.cos(Math.toRadians(windDirection)) +
                    luffRatio * Math.sin(Math.toRadians(windDirection)) * Math.sin(System.currentTimeMillis() * luffSpeed - i));

            flagXPoints[i] = flagX;
            flagYPoints[i] = flagY;
        }

        // draw the flag
        graphicsContext.setStroke(boat.getFillColor());
        graphicsContext.setLineWidth(0.5 * zoomFactor);
        graphicsContext.strokePolyline(flagXPoints, flagYPoints, N);
    }

    /**
     * Draws oars for a boat.
     * @param boat for which to draw the oars
     * @param graphicsContext on which to draw the oars
     * @param zoomFactor at which to draw the oars
     */
    public static void drawOars(Boat boat, GraphicsContext graphicsContext, double zoomFactor) {
        int oars = 8; //number of oars
        double length = 8; //length of the oars
        double heading = boat.getHeading();
        double oarAngle = 90 + boat.getSpeed() * Math.sin(System.currentTimeMillis() / 300.0); //current angle of the oars

        graphicsContext.setStroke(Color.rgb(245, 197, 122, 1.0)); // light brown
        graphicsContext.setLineWidth(0.5 * zoomFactor);
        for (int i = 0; i < oars; i++) {
            double leftX1 = boat.getX() - zoomFactor * (2 * (i - oars / 2) * Math.sin(toRadians(heading)) -
                    3 * Math.cos(toRadians(heading)));
            double leftY1 = boat.getY() + zoomFactor * (2 * (i - oars / 2) * Math.cos(toRadians(heading)) +
                    3 * Math.sin(toRadians(heading)));

            double leftX2 = leftX1 + zoomFactor * length * Math.sin(toRadians(heading + oarAngle));
            double leftY2 = leftY1 - zoomFactor * length * Math.cos(toRadians(heading + oarAngle));

            double rightX1 = boat.getX() - zoomFactor * (2 * (i - oars / 2) * Math.sin(toRadians(heading)) +
                    3 * Math.cos(toRadians(heading)));
            double rightY1 = boat.getY() + zoomFactor * (2 * (i - oars / 2) * Math.cos(toRadians(heading)) -
                    3 * Math.sin(toRadians(heading)));

            double rightX2 = rightX1 + zoomFactor * length * Math.sin(toRadians(heading - oarAngle));
            double rightY2 = rightY1 - zoomFactor * length * Math.cos(toRadians(heading - oarAngle));

            graphicsContext.strokeLine(leftX1, leftY1, leftX2, leftY2);
            graphicsContext.strokeLine(rightX1, rightY1, rightX2, rightY2);
        }
    }

    /**
     * Gets the ApexX of the boat
     *
     * @param boat boat to get apex of
     * @return boat apex X position
     */
    public static double getApexX(Boat boat){
        return boat.getX() + BOAT_HEIGHT / 2 * Math.cos(boat.getHeading());

    }

    /**
     * Gets the ApexY of the boat
     * @param boat boat to get apex of
     * @return boat apex Y position
     */
    public static double getApexY(Boat boat){
        return boat.getY() + BOAT_HEIGHT / 2 * Math.cos(boat.getHeading());

    }



}
