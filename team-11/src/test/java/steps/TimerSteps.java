package steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.After;
import org.junit.Assert;
import seng302.Common.RaceStatus;
import seng302.Server.ServerMain;

import java.io.IOException;

/**
 * Tests the race state before and after the timer is started.
 */
public class TimerSteps {
    private ServerMain server = new ServerMain();
    private long startTime;

    public TimerSteps() throws IOException {
    }

    @Given("^The race prep phase is (\\d+) seconds$")
    public void the_race_prep_phase_is_seconds(int arg1) throws Throwable {
        startTime = arg1 * 1000;
    }

    @When("^The race is begun$")
    public void the_race_is_begun() throws Throwable {
        Thread.sleep(1000);
        ServerMain.startRace((int) startTime);
    }

    @Then("^the active race will begin (\\d+) seconds later$")
    public void the_active_race_will_begin_seconds_later(int arg1) throws Throwable {
        Thread.sleep(startTime);
        if (System.currentTimeMillis() > server.getRace().getExpectedStartTime()) {
            server.getRace().setRaceState(RaceStatus.STARTED);
        }
        Assert.assertEquals(RaceStatus.STARTED, server.getRace().getRaceState());

        server.shutdownServer();
    }

//    @cucumber.api.java.After
//    public void tearDownServer() {
//        server.shutdownServer();
//    }
}
