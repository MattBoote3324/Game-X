package seng302.Client.Controllers;

import javafx.scene.canvas.Canvas;
import seng302.Common.Course;
import seng302.Common.CourseFeature;
import seng302.Common.FeatureType;
import seng302.Common.Point;

import java.util.List;

/**
 * Handle Client side course logic.
 */
public class CourseController {
    private static final double EDGE_BUFFER = 0.01;

    // Variables needed for converting latitude longitude to canvas x and y.
    private double courseWidth;
    private double courseHeight;
    private double lonXDifference;
    private double latYDifference;
    private double scaleGeoToCartesian;

    private Course course;
    private double canvasWidth;
    private double canvasHeight;

    private boolean zoomed;
    private double zoomFactor = 1;
    private List<Double> centreCoords;
    private double centerLat;
    private double centerLon;

    /**
     * Normal constructor.
     *
     * @param course The course to control.
     * @param canvas the canvas to scale the course to.
     */
    CourseController(Course course, Canvas canvas) {
        this.course = course;
        this.canvasWidth = canvas.getWidth();
        this.canvasHeight = canvas.getHeight();
        determineCourseDimensions();
        determineScaleGeoToCartesian();
        setFeaturePointDimensions(4, 4);
    }

    /**
     * Constructor used without a canvas.
     *
     * Used in testing.
     *
     * @param course The course to control.
     * @param canvasWidth The canvas width to scale the course to.
     * @param canvasHeight The canvas height to scale the course to.
     */
    public CourseController(Course course, double canvasWidth, double canvasHeight) {
        this.course = course;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        determineCourseDimensions();
        determineScaleGeoToCartesian();
        setFeaturePointDimensions(4, 4);
    }

    /**
     * Determine the dimensions of the course, and the geo to cartesian scale factor.
     */
    private void determineCourseDimensions() {
        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        for (Point p : course.getBoundaryPoints()) {
            maxLon = (p.getLongitude() > maxLon) ? p.getLongitude() : maxLon;
            minLon = (p.getLongitude() < minLon) ? p.getLongitude() : minLon;
            maxLat = (p.getLatitude() > maxLat) ? p.getLatitude() : maxLat;
            minLat = (p.getLatitude() < minLat) ? p.getLatitude() : minLat;
        }
        courseWidth = maxLon - minLon;
        courseHeight = maxLat - minLat;

        lonXDifference = minLon;
        latYDifference = maxLat;

        centerLat = (minLat + maxLat) / 2;
        centerLon = (minLon + maxLon) / 2;
    }

    /**
     * Determine the geo to cartesian scale factor based on the course aspect ratio and dimensions of the canvas.
     */
    private void determineScaleGeoToCartesian() {
        double smallestCanvasDimension = Math.min(canvasWidth, canvasHeight);
        scaleGeoToCartesian = (smallestCanvasDimension - smallestCanvasDimension * EDGE_BUFFER) / Math.max(courseWidth, courseHeight);
    }

    /**
     * Add cartesian co-ordinates to the course's feature points.
     */
    public void addCartesianToCourseFeatures() {
        for (CourseFeature courseFeature : course.getCourseFeatures()) {
            if (courseFeature.getType() == FeatureType.MARK) {
                addCartesianToPoint(courseFeature.getPoint1());
            } else {
                addCartesianToPoint(courseFeature.getPoint1());
                addCartesianToPoint(courseFeature.getPoint2());
                addCartesianToPoint(courseFeature.getMidPoint());
            }
        }
        for (Point point : course.getBoundaryPoints()) {
            addCartesianToPoint(point);
        }
    }

    /**
     * Add cartesian co-ordinates to a given point.
     *
     * @param point The point to add cartesian co-ordinates to.
     */
    private void addCartesianToPoint(Point point) {
        if (!zoomed) {
            double canvasCentreX = canvasWidth / 2;
            double canvasCentreY = canvasHeight / 2;
            point.setX((point.getLongitude() - centerLon) * scaleGeoToCartesian + canvasCentreX);
            point.setY((centerLat - point.getLatitude()) * scaleGeoToCartesian + canvasCentreY);
        } else {
            double centreLon = centreCoords.get(0);
            double centreLat = centreCoords.get(1);
            double canvasCentreX = centreCoords.get(2);
            double canvasCentreY = centreCoords.get(3);
            point.setX((point.getLongitude() - centreLon) * scaleGeoToCartesian * zoomFactor + canvasCentreX);
            point.setY((centreLat - point.getLatitude()) * scaleGeoToCartesian * zoomFactor + canvasCentreY);
        }
    }

    /**
     * Set size of the course's feature points.
     *
     * @param width Width of feature.
     * @param height Height of feature.
     */
    private void setFeaturePointDimensions(double width, double height) {
        for (CourseFeature feature : course.getCourseFeatures()) {
            if (feature.getType() == FeatureType.MARK) {
                Point featurePoint = feature.getPoint1();
                featurePoint.setWidth(width);
                featurePoint.setHeight(height);
            } else {
                Point featurePoint1 = feature.getPoint1();
                Point featurePoint2 = feature.getPoint2();
                featurePoint1.setWidth(width);
                featurePoint1.setHeight(height);
                featurePoint2.setWidth(width);
                featurePoint2.setHeight(height);
            }
        }
    }

    public double getLonXDifference() {
        return lonXDifference;
    }

    public double getLatYDifference() {
        return latYDifference;
    }

    public double getScaleGeoToCartesian() {
        return scaleGeoToCartesian;
    }

    void setZoomed(boolean zoomed) {
        this.zoomed = zoomed;
    }

    void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    void setCentreCoords(List<Double> centreCoords) {
        this.centreCoords = centreCoords;
    }

    public double getCenterLat() {
        return centerLat;
    }

    public double getCenterLon() {
        return centerLon;
    }
}
