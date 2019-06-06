package steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import seng302.Server.Model.Wind;
import seng302.Server.Tasks.WindGenerationTask;

import java.util.Timer;

/**
 * Created by mch230 on 8/08/17.
 */
public class WindSteps {
    private Wind wind = new Wind(0, 0);
    private double initialDirection;

    @Given("^The initial wind speed is (\\d+) knots$")
    public void the_initial_wind_speed_is_knots(int arg1) throws Throwable {
        wind.setCurrentSpeed(arg1);
    }

    @Given("^The initial wind direction is (\\d+) degrees$")
    public void the_initial_wind_direction_is_degrees(int arg1) throws Throwable {
        initialDirection = arg1;
        wind.setCurrentDirection(arg1);
    }

    @When("^the wind is oscillating$")
    public void the_wind_is_oscillating() throws Throwable {
        wind.setVeerChance(0.0);
        Timer t = new Timer();
        t.schedule(new WindGenerationTask(wind), 0, 500);
        Thread.sleep(1000);
    }

    @Then("^the wind updates between (\\d+) degrees each way, and the speed has changed$")
    public void the_wind_updates_between_degrees_each_way_and_the_speed_has_changed(int arg1) throws Throwable {
        Assert.assertTrue(Math.abs(wind.getCurrentDirection() - wind.getTargetDirection()) < arg1);
        Assert.assertTrue(wind.getCurrentSpeed() != wind.getTargetSpeed());
    }

    @When("^The wind is veering$")
    public void the_wind_is_veering() throws Throwable {
        wind.setVeerChance(1.0);
        Timer t = new Timer();
        t.schedule(new WindGenerationTask(wind), 0, 500);
        Thread.sleep(1200);
    }

    @Then("^the wind direction will change and oscillate from this new direction$")
    public void the_wind_direction_will_change_and_oscillate_from_this_new_direction() throws Throwable {
        Assert.assertTrue(wind.getOriginalDirection() != initialDirection); // The oscillation direction has changed
        Assert.assertTrue(wind.getOriginalDirection() == wind.getTargetDirection());    // The target for the wind is now the veer direction
        Assert.assertTrue(wind.getCurrentDirection() != initialDirection);  // The wind has shifted
    }

}
