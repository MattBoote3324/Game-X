package seng302.Common;

import seng302.Common.Utils.Calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * creates a courseFeature (mark)
 */
public class CourseFeature {
    private String name;
    private int id;
    private FeatureType type;
    private Point point1;
    private Point point2;
    private Point midPoint;
    private String rounding;
    private List<Point> points = new ArrayList<>();

    public CourseFeature(String name, int id, FeatureType type, Point point1) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.point1 = point1;
        this.midPoint = point1;
        points.add(point1);
    }

    public CourseFeature(String name, int id, FeatureType type, Point point1, Point point2) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.point1 = point1;
        this.point2 = point2;
        points.add(point1);
        points.add(point2);
        this.midPoint = Calculator.midPoint(point1, point2);
    }

    @Override
    public boolean equals(Object feature) {
        if(feature instanceof CourseFeature) {
            if(this.name.equals(((CourseFeature) feature).name)) {
                return true;
            }
        }
        return false;
    }

    ////////////////////////////////////////////
    // Only getters and setters from here on. //
    ////////////////////////////////////////////

    public int getId() {
        return id;
    }

    public FeatureType getType() {
        return type;
    }

    public Point getPoint1() {
        return point1;
    }

    public Point getPoint2() {
        return point2;
    }

    public Point getMidPoint() {
        return midPoint;
    }

    public String getName() {
        return name;
    }

    public String getRounding() {
        return rounding;
    }

    public void setRounding(String rounding) {
        this.rounding = rounding;
    }

    public List<Point> getPoints() {
        return points;
    }
}

