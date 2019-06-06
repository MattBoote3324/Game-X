package seng302;

import org.junit.Before;
import org.junit.Test;
import seng302.Server.Model.Wind;
import seng302.Server.Tasks.WindGenerationTask;

import java.util.Timer;

import static org.junit.Assert.assertTrue;

/**
 * Created by mch230 on 4/08/17.
 */
public class WindTest {
    private Wind wind;

    @Before
    public void setup() {
        wind = new Wind(12, 0);
        wind.setRandomSeed((long) 1.0);
        wind.setVeerChance(0.0);
        Timer t = new Timer();
        t.schedule(new WindGenerationTask(wind), 0, 500);
    }

    @Test
    public void testVeer() throws InterruptedException {
        wind.setVeerChance(1.0);
        Thread.sleep(500);
        assertTrue(Math.min((wind.getCurrentDirection() - wind.getTargetDirection() + 360) % 360, (wind.getTargetDirection() - wind.getCurrentDirection() + 360) % 360) < 180);
        assertTrue(wind.getTargetSpeed() > 4 && wind.getTargetSpeed() < 30);
    }

}
