package seng302.Common;

public class FrameRateMeasure {
    private long lastUpdate = 0;
    private int index = 0;
    private double[] frameRates = new double[100];
    private int averageAmount;
    private int i = 0;
    private double averagedFrameRate = 0;

    public FrameRateMeasure(int averageAmount) {
        // Passed in value should be greater than zero.
        if(averageAmount <= 0) {
            this.averageAmount = 50;
        } else {
            this.averageAmount = averageAmount;
        }
    }

    /**
     * measures frame rate based on the averageAmount set
     * @param now current time in nanoseconds.
     * @return averageFrame rate
     */
    public double measure(long now) {
        i++;
        if (lastUpdate > 0) {
            long nanosElapsed = now - lastUpdate;
            double frameRate = 1000000000.0 / nanosElapsed;
            index %= frameRates.length;
            frameRates[index++] = frameRate;
        }
        lastUpdate = now;
        if (i % averageAmount == 0) {
            double total = 0.0d;

            for (double frameRate : frameRates) {
                total += frameRate;
            }
            averagedFrameRate = total / frameRates.length;
        }
        return averagedFrameRate;
    }
}