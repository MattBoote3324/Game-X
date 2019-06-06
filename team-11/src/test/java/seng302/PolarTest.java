package seng302;

import org.junit.Before;
import org.junit.Test;
import seng302.Common.Polar;
import seng302.Common.PolarCSVReader;

import static org.junit.Assert.assertEquals;

public class PolarTest {
    private Polar polar;
    private double expected;
    private double result;

    @Before
    public void createPolar() {
        polar = new Polar(new PolarCSVReader("/csv/polarsAC35.csv"));
    }

    @Test
    public void findSpeedTws12Twa90() {
        expected = 23.0;
        result = polar.getBoatSpeedAtTrueWindSpeed(12.0, 90.0);
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void findSpeedTws16Twa42() {
        expected = 19.2;
        result = polar.getBoatSpeedAtTrueWindSpeed(16.0, 42.0);
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void findSpeedTws6Twa30() { //unknown Tws, known Twa
        expected = 5.5;
        result = polar.getBoatSpeedAtTrueWindSpeed(6.0, 30.0);
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void findSpeedTws20Twa80() { //known Tws, unknown Twa
        expected = 37.0 * 2 / 3 + 39.0 / 3;
        result = polar.getBoatSpeedAtTrueWindSpeed(20.0, 80.0);
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void findSpeedTws6Twa95() { //unknown Tws and Twa
        expected = 10.5 * 4 / 5 + 11.0 / 5;
        result = polar.getBoatSpeedAtTrueWindSpeed(6.0, 95.0);
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void findUpwindAngle16() {
        expected = 42.0;
        result = polar.getBestUpwindAngleAtTrueWindSpeed(16.0);
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void findUpwindAngle18() {
        expected = 41.5;
        result = polar.getBestUpwindAngleAtTrueWindSpeed(18.0);
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void findUpwindSpeed25() {
        expected = 30.0;
        result = polar.getBestUpwindSpeedAtTrueWindSpeed(25.0);
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void findUpwindSpeed10() {
        expected = 12.2;
        result = polar.getBestUpwindSpeedAtTrueWindSpeed(10.0);
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void findDownwindAngle6() {
        expected = 154.0;
        result = polar.getBestDownwindAngleAtTrueWindSpeed(6.0);
        assertEquals(expected, result, 0.001);
    }

    @Test
    public void findDownwindSpeed14() {
        expected = 28.8 / 2 + 21.6 / 2;
        result = polar.getBestDownwindSpeedAtTrueWindSpeed(14.0);
        assertEquals(expected, result, 0.001);
    }
}
