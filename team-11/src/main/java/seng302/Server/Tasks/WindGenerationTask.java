package seng302.Server.Tasks;

import seng302.Server.Model.Wind;

import java.util.TimerTask;

/**
 * Begins the wind generation and calls the wind to update
 */
public class WindGenerationTask extends TimerTask {

    private final Wind wind;

    /**
     * Create a new task with the wind supplied
     *
     * @param wind wind object
     */
    public WindGenerationTask(Wind wind) {
        this.wind = wind;
        wind.startWind();
    }

    /**
     * Update the direction and speed of the wind
     */
    @Override
    public void run() {
        wind.updateDirection();
        wind.updateSpeed();
    }
}