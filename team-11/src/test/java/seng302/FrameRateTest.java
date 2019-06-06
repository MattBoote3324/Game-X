package seng302;

import org.junit.Before;
import org.junit.Test;
import seng302.Common.FrameRateMeasure;

import static org.junit.Assert.assertTrue;


public class FrameRateTest {
    FrameRateMeasure frm;

    @Before
    public void setUpObject() {
        frm = new FrameRateMeasure(9);
    }

    @Test
    public void testFrameRate() {
        double average = 0;

        long[] list = {1000000L, 2000000L, 3000000L, 4000000L, 6000000L, 7000000L, 8000000L, 9000000L, 10000000L};

        long i = 0;

        for (int j = 0; j < list.length; j++) {
            average += frm.measure(list[j]);
        }
        assertTrue(average == 75);
    }
}
