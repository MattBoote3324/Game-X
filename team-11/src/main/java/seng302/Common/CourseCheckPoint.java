package seng302.Common;

/**
 * Define how a course feature needs to be passed in order for a boats race progress to increase.
 */
public class CourseCheckPoint {
    private int courseFeatureID;
    private String rounding;
    private int zoneSize;

    public CourseCheckPoint(int courseFeatureID, String rounding, int zoneSize) {
        this.courseFeatureID = courseFeatureID;
        this.rounding = rounding;
        this.zoneSize = zoneSize;
    }

    public int getCourseFeatureID() {
        return courseFeatureID;
    }

    public String getRounding() {
        return rounding;
    }

    public int getZoneSize() {
        return zoneSize;
    }
}
