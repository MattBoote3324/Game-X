package seng302.Common;

/**
 * Model the overall status of a boat, as defined by a Race Status Message.
 * A wrapper for Boat object attributes, for easier message receiving.
 */
public class BoatStatusWrapper {
    private Boat.BoatStatus boatStatus;
    private int legNumber;
    private int numberPenaltiesAwarded;
    private int numberPenaltiesServed;
    private long estTimeToNextMark;
    private long estTimeToFinish;

    public BoatStatusWrapper(Boat.BoatStatus boatStatus, int legNumber, int numberPenaltiesAwarded, int numberPenaltiesServed, long estTimeToNextMark, long estTimeToFinish) {

        this.boatStatus = boatStatus;
        this.legNumber = legNumber;
        this.numberPenaltiesAwarded = numberPenaltiesAwarded;
        this.numberPenaltiesServed = numberPenaltiesServed;
        this.estTimeToNextMark = estTimeToNextMark;
        this.estTimeToFinish = estTimeToFinish;
    }

    public Boat.BoatStatus getBoatStatus() {
        return boatStatus;
    }

    public int getLegNumber() {
        return legNumber;
    }

    public int getNumberPenaltiesAwarded() {
        return numberPenaltiesAwarded;
    }

    public int getNumberPenaltiesServed() {
        return numberPenaltiesServed;
    }

    public long getEstTimeToNextMark() {
        return estTimeToNextMark;
    }

    public long getEstTimeToFinish() {
        return estTimeToFinish;
    }
}
