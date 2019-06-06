package seng302.Server.Model;

import java.util.Random;

/**
 * Internal wind class to handle wind speed and direction oscillations
 */
public class Wind {
    // Variables shared by both client and server winds
    private double currentSpeed;   // wind speed in knots
    private double currentDirection;   // direction as an angle (0-359 degrees)

    // These variables are for random wind generation (server side)
    private double originalSpeed;
    private double originalDirection;
    private double targetDirection;
    private double targetSpeed;
    private Random rand = new Random();

    // Bringing out as a class variable to help with tests
    private double veerChance  = 0.005;

    /**
     * Constructs new wind object with initial speed and direction
     * @param currentSpeed Wind speed in knots
     * @param currentDirection Wind angle in degrees (0-359 degrees)
     */
    public Wind(double currentSpeed, double currentDirection) {
        this.currentSpeed = currentSpeed;
        this.currentDirection = currentDirection;
    }

    /**
     * Starts the wind generation- initialises required variables for randomisation.
     * Handles the timer task, change in wind every 500ms
     */
    public void startWind() {
        this.originalSpeed = currentSpeed;
        this.originalDirection = currentDirection;

        this.targetDirection = currentDirection;
        this.targetSpeed = currentSpeed;
    }

    /**
     * Updates the wind direction towards the target direction, with a small chance of "veering" (changing the overall direction of wind)
     *
     * Performs a check for whether the target direction is reached, else modifies the current wind direction towards the target
     */
    public void updateDirection() {
        determineVeer();

        double changeInDirection = 1.0; // variable for how much the direction changes on each update
        if (Math.abs(currentDirection - targetDirection) <= (changeInDirection / 2.0)) {    // Target direction reached, obtain a new target
            changeTargetDirection();
        } else {
            if (Math.floorMod((long) (currentDirection - targetDirection), (long) 360) < 180) { // Else, modify current wind direction towards target
                currentDirection -= changeInDirection;
            } else {
                currentDirection += changeInDirection;
            }
            currentDirection = Math.floorMod((long) currentDirection, (long) 360);  // Floor mod to keep angle between 0 and 359 degrees
        }
    }

    /**
     * Helper method to determine if the wind will veer (changing the overall direction of wind)
     * Oscillations should be from this new veer direction
     */
    private void determineVeer() {
        Double veer = rand.nextDouble();
        if (veer <= veerChance) {
            targetDirection = rand.nextInt(360);
            originalDirection = targetDirection;
        }
    }

    /**
     * Updates the wind speed - note units are in knots
     *
     * Performs a check for whether the target speed is reached, else modifies the current wind speed towards the target
     */
    public void updateSpeed() {
        double scale = 0.01; // variable for how much the speed changes on each update
        double changeInSpeed = rand.nextDouble() * scale;   // Uses a random double (betw 0 and 1) so that changes in speed are less granular
        if (Math.abs(currentSpeed - targetSpeed) <= (changeInSpeed * scale / 2)) {    // Target speed met, obtain a new target
            changeTargetSpeed();
        } else {
            if (currentSpeed > targetSpeed) {   // Else, modify current wind speed towards target
                currentSpeed -= changeInSpeed;
            } else {
                currentSpeed += changeInSpeed;
            }
        }
    }

    /**
     * Obtain a new target direction, using a normal distribution probability
     */
    private void changeTargetDirection() {
        double scale = 10.0;
        double degreeShift = (rand.nextDouble() - 0.5) * scale;

        //double randResult = rand.nextGaussian(); // Returns a double chosen from a normal distribution over a mean of 0.0
        targetDirection = Math.floorMod((long) (originalDirection + degreeShift), (long) 360);
    }

    /**
     * Obtain a new target speed, using a normal distribution probability
     */
    private void changeTargetSpeed() {
        double speedShift = 2;

        double randResult = rand.nextGaussian(); // Returns a double chosen from a normal distribution over a mean of 0.0
        targetSpeed = originalSpeed + (randResult * speedShift);
    }



    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public double getCurrentDirection() {
        return currentDirection;
    }

    public double getTargetDirection() {
        return targetDirection;
    }

    public double getTargetSpeed() {
        return targetSpeed;
    }

    public double getOriginalDirection() {
        return originalDirection;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public void setCurrentDirection(double currentDirection) {
        this.currentDirection = currentDirection;
    }

    public void setVeerChance(double veerChance) {
        this.veerChance = veerChance;
    }

    /**
     * For testing purposes
     * @param seed Seed the random number generator with a starting value
     */
    public void setRandomSeed(long seed) {
        rand.setSeed(seed);
    }
}
