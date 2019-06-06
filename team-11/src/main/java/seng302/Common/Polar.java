package seng302.Common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tho63 on 24/05/17
 */
public class Polar {
    private HashMap<Double, HashMap<Double, Double>> polarMap = new HashMap<>();
    private HashMap<Double, Double>upwindAngle = new HashMap<>();
    private HashMap<Double, Double> downwindAngle = new HashMap<>();
    private HashMap<Double, HashMap<Double, Double>>upwindSpeed = new HashMap<>();
    private HashMap<Double, HashMap<Double, Double>> downwindSpeed = new HashMap<>();

    public Polar(PolarCSVReader polarCSVReader) {
        this.polarMap = polarCSVReader.getPolarMap();
        this.upwindAngle = polarCSVReader.getUpwindAngle();
        this.downwindAngle = polarCSVReader.getDownwindAngle();
        this.upwindSpeed = polarCSVReader.getUpwindSpeed();
        this.downwindSpeed = polarCSVReader.getDownwindSpeed();
    }

    /**
     * Interpolate values of a map based on one or two key values.
     * If one key specified, then does linear interpolation on the map.
     * If both keys specified, firstly does linear interpolation on map using key1,
     * giving a secondary HashMap, then interpolates that map using key2.
     * @param map a map of the desired values
     * @param key1 the key index for the first interpolation
     * @param key2 the key index for the second interpolation (optional)
     * @return the value gained from bilinear interpolation of the key(s) in the map
     */
    public double bilinearInterpolation(HashMap<Double, Object> map, double key1, double key2) {
        double lower = -1.0, upper = -1.0;
        //Find lower and upper bounds for key1 in map
        for (Map.Entry<Double, Object> entry : map.entrySet()) {
            if (entry.getKey() <= key1) {
                if (lower == -1.0 || entry.getKey() > lower) {
                    lower = entry.getKey();
                }
            } else if (entry.getKey() >= key1) {
                if (upper == -1.0 || entry.getKey() < upper) {
                    upper = entry.getKey();
                }
            }
        }
        //If value lies outside range, cap it to the highest/lowest
        if (lower == -1.0) {
            Object valueUpper = map.get(upper);
            if (valueUpper instanceof Double) { return (Double) valueUpper; }
            if (valueUpper instanceof HashMap) { return bilinearInterpolation((HashMap) valueUpper, key2, 0.0); }
        } else if (upper == -1.0) {
            Object valueLower = map.get(lower);
            if (valueLower instanceof Double) { return (Double) valueLower; }
            if (valueLower instanceof HashMap) { return bilinearInterpolation((HashMap) valueLower, key2, 0.0); }
        }

        Object valueLower = map.get(lower);
        Object valueUpper = map.get(upper);
        //Do the actual interpolation
        if (valueLower instanceof Double) { //Linear
            Double scale = (key1 - lower) / (upper - lower);
            return scale * ((Double) valueUpper - (Double) valueLower) + (Double) valueLower;
        } else { //Bilinear
            Double valueLower2, valueUpper2;
            valueLower2 = (Double) ((HashMap) valueLower).get(key2);
            if (valueLower2 == null) {
                valueLower2 = bilinearInterpolation((HashMap) valueLower, key2, 0.0);
            }
            valueUpper2 = (Double) ((HashMap) valueUpper).get(key2);
            if (valueUpper2 == null) {
                valueUpper2 = bilinearInterpolation((HashMap) valueUpper, key2, 0.0);
            }
            double scale = (key1 - lower) / (upper - lower);
            return scale * (valueUpper2 - valueLower2) + valueLower2;
        }
    }

    public Double getBoatSpeedAtTrueWindSpeed(Double trueWindSpeed,Double trueWindAngle){
        return bilinearInterpolation((HashMap) polarMap, trueWindSpeed, trueWindAngle);
    }

    public Double getBestUpwindAngleAtTrueWindSpeed(Double trueWindSpeed) {
        return bilinearInterpolation((HashMap) upwindAngle, trueWindSpeed, 0.0);
    }

    public Double getBestUpwindSpeedAtTrueWindSpeed(Double trueWindSpeed) {
        return bilinearInterpolation((HashMap) upwindSpeed, trueWindSpeed, 0.0);
    }

    public Double getBestDownwindAngleAtTrueWindSpeed(Double trueWindSpeed){
        return bilinearInterpolation((HashMap) downwindAngle, trueWindSpeed, 0.0);
    }

    public Double getBestDownwindSpeedAtTrueWindSpeed(Double trueWindSpeed){
        return bilinearInterpolation((HashMap) downwindSpeed, trueWindSpeed, 0.0);
    }


}
