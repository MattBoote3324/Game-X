package seng302.Common;

public enum RaceStatus {
    NOT_ACTIVE(0, "Not Active"),
    WARNING(1, "Warning (between 3:00 and 1:00 before start)"),
    PREPARATORY(2, "Preparatory (less than 1:00 before start)"),
    STARTED(3, "Started"),
    FINISHED(4, "Finished"),
    RETIRED(5, "Retired"),
    ABANDONED(6, "Abandoned"),
    POSTPONED(7, "Postponed"),
    TERMINATED(8, "Terminated"),
    NO_RACE_TIME(9, "No Race Time Set Yet"),
    PRESTART(10, "Prestart (more than 3:00 until start)");

    private final String statusType;
    private final int value;

    RaceStatus(int i, String type) {
        statusType = type;
        value = i;
    }

    public static String getString(int raceStatus) {
        RaceStatus rs = RaceStatus.values()[raceStatus];
        return rs.toString();
    }

    public String toString() {
        return statusType;
    }

    public int getValue() {return value;}

}
