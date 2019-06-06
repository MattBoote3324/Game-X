package seng302.Common;

/**
 * Points that contain a latitude, longitude , x and y values. Easier to store all on one class.
 */
public class Point {
    // Geo coordinates, relates to Earth.
    // NOTE: latitude changes North/South, longitude changes East/West.
    private double latitude;
    private double longitude;

    // Cartesian coordinates, relates to RaceVision canvas.
    private double x;
    private double y;

    // Dimensions of graphical representation.
    private double width;
    private double height;

    private int sourceId;

    public Point() {
    }
    public Point(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public Point(double latitude, double longitude, int sourceId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.sourceId = sourceId;
    }

    @Override
    public String toString() {
        return String.valueOf(sourceId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        return (Double.compare(point.latitude, latitude) == 0 &&
                Double.compare(point.longitude, longitude) == 0);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    ////////////////////////////////////////////
    // Only getters and setters from here on. //
    ////////////////////////////////////////////

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) { this.sourceId = sourceId; }
}
