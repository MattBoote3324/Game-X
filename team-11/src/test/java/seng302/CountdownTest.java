package seng302;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seng302.Common.RaceStatus;
import seng302.Server.Model.Race;
import seng302.Server.ServerMain;

import java.io.IOException;

/**
 * Class to test the countdown works properly
 */
public class CountdownTest {
    private ServerMain server;

    @Before
    public void setup() throws IOException {
        server = new ServerMain();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDownServer() {
        server.shutdownServer();
    }

    @Test
    public void countdownTest() {
        Race race = server.getRace();

        Assert.assertEquals(RaceStatus.NOT_ACTIVE, race.getRaceState());

        ServerMain.startRace(1000);
        Assert.assertEquals(RaceStatus.PREPARATORY, race.getRaceState());

        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (System.currentTimeMillis() > race.getExpectedStartTime()) {
            race.setRaceState(RaceStatus.STARTED);
        }

        Assert.assertEquals(RaceStatus.STARTED, race.getRaceState());
    }
}
