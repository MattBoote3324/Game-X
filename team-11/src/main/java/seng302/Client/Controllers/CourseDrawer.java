package seng302.Client.Controllers;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import seng302.Client.ClientMain;
import seng302.Common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Render any given course onto any given graphics context.
 */
class CourseDrawer {

    //Import mark images
    private static Image stone = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/stone.png"));
    private static Image startFlag = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/startFlag.gif"));
    private static Image endFlag = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/endFlag.gif"));
    private CourseController courseController;

    /**
     * Draw a given course onto a given graphics context.
     *
     */
    static void drawCourse(Course course, GraphicsContext graphicsContext, boolean zoomed, double zoomFactor) {
        if (zoomed) {
            CourseController courseController = new CourseController(course, graphicsContext.getCanvas());
            courseController.setZoomed(true);
            courseController.setZoomFactor(zoomFactor);
            Fleet fleet = ClientMain.getRace().getFleet();
            double centreLon = fleet.getUserAssignBoat().getLongitude();
            double centreLat = fleet.getUserAssignBoat().getLatitude();
            double canvasCentreX = graphicsContext.getCanvas().getWidth() / 2;
            double canvasCentreY = graphicsContext.getCanvas().getHeight() / 2;
            List<Double> centreCoords = new ArrayList<>(Arrays.asList(centreLon, centreLat, canvasCentreX, canvasCentreY));
            courseController.setCentreCoords(centreCoords);
            courseController.addCartesianToCourseFeatures();
        }

        drawCourseBoundary(course, graphicsContext);
        drawCourseFeatures(course, graphicsContext, zoomFactor);
    }

    /**
     * Draw the boundary for a given course.
     *
     * @param course The given course.
     */
    private static void drawCourseBoundary(Course course, GraphicsContext graphicsContext) {
        List<Point> boundaryPoints = course.getBoundaryPoints();
        int numPoints = boundaryPoints.size();
        double[] xPoints = new double[numPoints];
        double[] yPoints = new double[numPoints];
        int index = 0;
        for (Point point : boundaryPoints) {
            xPoints[index] = point.getX();
            yPoints[index] = point.getY();
            index++;
        }
        graphicsContext.setStroke(Color.rgb(255,255,255,0.75));
        graphicsContext.setLineWidth(ClientMain.raceViewController.getZoomFactor());
        graphicsContext.setLineDashes(5*ClientMain.raceViewController.getZoomFactor());
        graphicsContext.strokePolygon(xPoints, yPoints, numPoints);
        graphicsContext.setLineDashes(null);
        Color courseColor = Color.rgb(20, 20, 20, 0.2);
        graphicsContext.setFill(courseColor);
        graphicsContext.fillPolygon(xPoints, yPoints, numPoints);
    }

    /**
     * Draw the course features for a given course.
     *
     * @param course The given course.
     * @param graphicsContext graphics context on which to draw
     * @param zoomFactor zoom factor of current raceview
     */
    private static void drawCourseFeatures(Course course, GraphicsContext graphicsContext, double zoomFactor) {
        List<CourseFeature> courseFeatures = course.getCourseFeatures();
        for (CourseFeature feature : courseFeatures) {
            FeatureType type = feature.getType();
            if (type == FeatureType.MARK) {
                Point featurePoint = feature.getPoint1();
                drawFeaturePoint(featurePoint, type, graphicsContext, zoomFactor);
            } else {
                Point featurePoint1 = feature.getPoint1();
                Point featurePoint2 = feature.getPoint2();
                drawFeatureLine(feature, graphicsContext, zoomFactor);
                drawFeaturePoint(featurePoint1, type, graphicsContext, zoomFactor);
                drawFeaturePoint(featurePoint2, type, graphicsContext, zoomFactor);
            }
        }
    }

    /**
     * Method to draw images on course to represent marks
     * @param point the position of the mark
     * @param type the FeatureType of the mark
     * @param graphicsContext graphics context on which to draw
     * @param zoomFactor zoom factor of current raceview
     */
    private static void drawFeaturePoint(Point point, FeatureType type, GraphicsContext graphicsContext, double zoomFactor) {

        double sizeMultiplier;
        Image img;

        if(type == FeatureType.START) {
            sizeMultiplier = .020;
            img = startFlag;
        } else if (type == FeatureType.FINISH) {
            sizeMultiplier = .020;
            img = endFlag;
        } else {
            sizeMultiplier = .07;
            img = stone;
        }

        double width = point.getWidth() * zoomFactor * sizeMultiplier * img.getWidth();
        double height = point.getHeight() * zoomFactor * sizeMultiplier * img.getHeight();

        double x = point.getX() - width/2;
        double y = point.getY() - height/1.1;

        graphicsContext.drawImage(img, x, y,width, height);
    }

    /**
     * Draw a line between the two points of a given course feature.
     *
     * @param feature The feature (should be start, gate, or finish) to draw a line between.
     */
    private static void drawFeatureLine(CourseFeature feature, GraphicsContext graphicsContext, double zoomFactor) {
        switch (feature.getType()) {
            case START:
                graphicsContext.setStroke(Color.GREEN);
                break;
            case GATE:
                graphicsContext.setStroke(new Color(0, 0, 1, 0.2));
                break;
            case FINISH:
                graphicsContext.setStroke(Color.DARKRED);
                break;
            default:
                return;
        }
        //graphicsContext.setLineWidth(1);
        graphicsContext.setLineWidth(2*zoomFactor);
        graphicsContext.setLineDashes(3*zoomFactor);
        graphicsContext.strokeLine(feature.getPoint1().getX(), feature.getPoint1().getY(), feature.getPoint2().getX(), feature.getPoint2().getY());
        graphicsContext.setLineDashes(null);
    }

    /**
     * The main method for drawing arrows, delegates the responsibility to one of two methods
     * depending on whether we need to draw a straight or curved line for a gate or mark respectively
     * @param boat Boat that we want to draw guide arrows for, should just be the clients boat
     */
    static void drawDirectionArrow(Boat boat, GraphicsContext graphicsContext, double zoomFactor) {
        int featureID;
        // Get the boat's next course feature.
        if(!boat.isFinished()) {
            Map<Integer, CourseCheckPoint> courseOrder = ClientMain.getRace().getCourse().getCourseOrder();
            if(boat.getCourseProgress() + 1 <= courseOrder.size()) {
                featureID = courseOrder.get(boat.getCourseProgress() + 1).getCourseFeatureID();
            }else{
                featureID = courseOrder.get(boat.getCourseProgress()).getCourseFeatureID();
            }
            CourseFeature nextFeature = ClientMain.getRace().getCourse().getCourseFeatureById(featureID);

            //Drawing
            if (nextFeature.getType() != FeatureType.MARK) {
                drawStraightArrow(nextFeature, graphicsContext, zoomFactor);
            } else {
                drawCurvedArrow(nextFeature, graphicsContext, zoomFactor);
            }
        }
    }

    /**
     * Draw a curved direction arrow at the given feature
     *
     * @param feature feature which to draw the arrow around
     */
    private static void drawCurvedArrow(CourseFeature feature, GraphicsContext graphicsContext, double zoomFactor) {
        double radius = 40 * zoomFactor;
        double arcLength = 60;

        double pointX = feature.getPoint1().getX();
        double pointY = feature.getPoint1().getY();

        graphicsContext.setStroke(Color.WHITE);
        graphicsContext.setLineWidth(zoomFactor);

        if (feature.getRounding().equals("Port")) {
            graphicsContext.strokeArc(pointX - radius, pointY - radius, radius * 2, radius * 2, (System.currentTimeMillis() * 0.1) % 360, arcLength, ArcType.OPEN);
            graphicsContext.strokeArc(pointX - radius, pointY - radius, radius * 2, radius * 2, (180 + System.currentTimeMillis() * 0.1) % 360, arcLength, ArcType.OPEN);

            drawDirectionArrowHead(pointX - radius * Math.cos(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360 - arcLength)),
                    pointY - radius * Math.sin(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360 - arcLength)),
                    (0 - arcLength - System.currentTimeMillis() * 0.1) % 360,
                    graphicsContext, zoomFactor);
            drawDirectionArrowHead(pointX + radius * Math.cos(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360 - arcLength)),
                    pointY + radius * Math.sin(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360 - arcLength)),
                    (180 - arcLength - System.currentTimeMillis() * 0.1) % 360,
                    graphicsContext, zoomFactor);

            drawDirectionArrowTail(pointX - radius * Math.cos(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360)),
                    pointY - radius * Math.sin(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360)),
                    (0 - System.currentTimeMillis() * 0.1) % 360,
                    10, graphicsContext, zoomFactor);
            drawDirectionArrowTail(pointX + radius * Math.cos(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360)),
                    pointY + radius * Math.sin(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360)),
                    (180 - System.currentTimeMillis() * 0.1) % 360,
                    10, graphicsContext, zoomFactor);
        } else {
            graphicsContext.strokeArc(pointX - radius, pointY - radius, radius * 2, radius * 2, (0 - System.currentTimeMillis() * 0.1) % 360, arcLength, ArcType.OPEN);
            graphicsContext.strokeArc(pointX - radius, pointY - radius, radius * 2, radius * 2, (180 - System.currentTimeMillis() * 0.1) % 360, arcLength, ArcType.OPEN);

            drawDirectionArrowHead(pointX - radius * Math.cos(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360)),
                    pointY + radius * Math.sin(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360)),
                    (180 + System.currentTimeMillis() * 0.1) % 360,
                    graphicsContext, zoomFactor);
            drawDirectionArrowHead(pointX + radius * Math.cos(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360)),
                    pointY - radius * Math.sin(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360)),
                    (System.currentTimeMillis() * 0.1) % 360,
                    graphicsContext, zoomFactor);

            drawDirectionArrowTail(pointX - radius * Math.cos(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360 - arcLength)),
                    pointY + radius * Math.sin(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360 - arcLength)),
                    (180 - arcLength + System.currentTimeMillis() * 0.1) % 360,
                    10, graphicsContext, zoomFactor);
            drawDirectionArrowTail(pointX + radius * Math.cos(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360 - arcLength)),
                    pointY - radius * Math.sin(Math.toRadians((0 - System.currentTimeMillis() * 0.1) % 360 - arcLength)),
                    (0 - arcLength + System.currentTimeMillis() * 0.1) % 360,
                    10, graphicsContext, zoomFactor);
        }
    }


    /**
     * Draw a straight line arrow with head through feature
     *
     * @param feature to draw arrow through
     */
    private static void drawStraightArrow(CourseFeature feature, GraphicsContext graphicsContext, double zoomFactor) {
        double midX = feature.getMidPoint().getX();
        double midY = feature.getMidPoint().getY();
        double p1X = feature.getPoint1().getX();
        double p1Y = feature.getPoint1().getY();
        double p2X = feature.getPoint2().getX();
        double p2Y = feature.getPoint2().getY();
        double vec2Y = p2Y - p1Y;
        double vec2X = -(p2X - p1X);
        double lineScaleFactor1 = 0.25 + 0.125 * Math.sin(Math.toRadians(System.currentTimeMillis() * 0.2));
        double lineScaleFactor2 = 0.25 - 0.125 * Math.sin(Math.toRadians(System.currentTimeMillis() * 0.2));

        // Draw the arrow's line through the course feature.
        graphicsContext.setStroke(Color.WHITE);
        graphicsContext.setLineWidth(zoomFactor);
        graphicsContext.strokeLine(
                midX + vec2Y * lineScaleFactor1,
                midY + vec2X * lineScaleFactor1,
                midX - vec2Y * lineScaleFactor2,
                midY - vec2X * lineScaleFactor2);

        double angle = Math.toDegrees(Math.atan2(vec2Y, -vec2X));

        if (feature.getRounding().equals("PS")) {
            drawDirectionArrowHead(midX + vec2Y * lineScaleFactor1, midY + vec2X * lineScaleFactor1, 180 + angle, graphicsContext, zoomFactor);
            drawDirectionArrowTail(midX - vec2Y * lineScaleFactor2, midY - vec2X * lineScaleFactor2, 180 + angle, 20, graphicsContext, zoomFactor);
        } else {
            drawDirectionArrowHead(midX - vec2Y * lineScaleFactor2, midY - vec2X * lineScaleFactor2, angle, graphicsContext, zoomFactor);
            drawDirectionArrowTail(midX + vec2Y * lineScaleFactor1, midY + vec2X * lineScaleFactor1, angle, 20, graphicsContext, zoomFactor);
        }
    }

    /**
     * Draws an arrow head at point x, y
     *
     * @param x     point x
     * @param y     point y
     * @param angle angle at which to rotate head at
     */
    private static void drawDirectionArrowHead(double x, double y, double angle, GraphicsContext graphicsContext, double zoomFactor) {
        double[] headXPts = new double[4];
        double[] headYPts = new double[4];

        double headLength = 10;
        double headBack = 3;
        double headAngle = 15;

        headXPts[0] = x - headLength * zoomFactor * Math.sin(Math.toRadians(angle));
        headYPts[0] = y + headLength * zoomFactor * Math.cos(Math.toRadians(angle));

        headXPts[2] = x + headBack * zoomFactor * Math.sin(Math.toRadians(angle));
        headYPts[2] = y - headBack * zoomFactor * Math.cos(Math.toRadians(angle));

        headXPts[1] = headXPts[0] + headLength * zoomFactor * Math.sin(Math.toRadians(angle - headAngle));
        headYPts[1] = headYPts[0] - headLength * zoomFactor * Math.cos(Math.toRadians(angle - headAngle));
        headXPts[3] = headXPts[0] + (headLength * zoomFactor * Math.sin(Math.toRadians(angle + headAngle)));
        headYPts[3] = headYPts[0] - (headLength * zoomFactor * Math.cos(Math.toRadians(angle + headAngle)));


        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillPolygon(headXPts, headYPts, 4);
    }

    /**
     * Draws an arrow tail at point x, y
     *
     * @param x     point x
     * @param y     point y
     * @param angle angle at which to rotate head at
     */
    private static void drawDirectionArrowTail(double x, double y, double angle, double length, GraphicsContext graphicsContext, double zoomFactor) {
        double[] tailXPts = new double[6];
        double[] tailYPts = new double[6];

        double tailWidth = 3;
        double tailAngle = 60;

        tailXPts[0] = x;
        tailYPts[0] = y;
        tailXPts[3] = x + length * zoomFactor * Math.sin(Math.toRadians(angle));
        tailYPts[3] = y - length * zoomFactor * Math.cos(Math.toRadians(angle));

        tailXPts[1] = tailXPts[0] + tailWidth * zoomFactor * Math.sin(Math.toRadians(angle - tailAngle));
        tailYPts[1] = tailYPts[0] - tailWidth * zoomFactor * Math.cos(Math.toRadians(angle - tailAngle));
        tailXPts[2] = tailXPts[3] + tailWidth * zoomFactor * Math.sin(Math.toRadians(angle - tailAngle));
        tailYPts[2] = tailYPts[3] - tailWidth * zoomFactor * Math.cos(Math.toRadians(angle - tailAngle));

        tailXPts[4] = tailXPts[3] + tailWidth * zoomFactor * Math.sin(Math.toRadians(angle + tailAngle));
        tailYPts[4] = tailYPts[3] - tailWidth * zoomFactor * Math.cos(Math.toRadians(angle + tailAngle));
        tailXPts[5] = tailXPts[0] + tailWidth * zoomFactor * Math.sin(Math.toRadians(angle + tailAngle));
        tailYPts[5] = tailYPts[0] - tailWidth * zoomFactor * Math.cos(Math.toRadians(angle + tailAngle));

        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillPolygon(tailXPts, tailYPts, 6);
    }
}