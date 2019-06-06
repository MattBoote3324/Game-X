package seng302.Common.Messages;

import seng302.Common.Boat;
import seng302.Common.Boat.BoatStatus;
import seng302.Common.BoatStatusWrapper;
import seng302.Common.Fleet;
import seng302.Common.RaceStatus;
import seng302.Common.Utils.Calculator;
import seng302.Common.Utils.MessageUtils;
import seng302.Server.Model.Race;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Model a Race Status message.
 *
 * Hold all the necessary attributes / fields, for ease of use.
 */
public class RaceStatusMessage extends Message implements MessageTypeHandler {
    private static final byte MESSAGE_TYPE = (byte) 12;

    private static final short FIXED_LENGTH = 24;  // The absolute minimum byte length of a Race Status message.
    private static final short VARIABLE_LENGTH = 20;  // The byte length of the 'For loop' section of the message.

    // Note: Unsigned fields must be stored in a larger data type.
    private byte messageVersionNumber = (byte) 2;
    private long currentTime;
    private int raceID = 8801;  // Currently not needed. Might be needed if one Server can host multiple races.
    private RaceStatus raceStatus;
    private long expectedStartTime;
    private int courseWindDirection;  // Unsigned.
    private int courseWindSpeed;      // Unsigned.
    private byte numBoatsInRace;      // Unsigned.
    private byte raceType;            // Unsigned.

    private Map<Integer, BoatStatusWrapper> boatStatuses = new HashMap<>();

    /**
     * Constructor used when sending.
     *
     * @param raceStatus          The race status.
     * @param expectedStartTime   The expected start time (millis since Jan 1 1970).
     * @param courseWindDirection The course wind direction, in range (0,360].
     * @param courseWindSpeed     The course wind speed, in knots.
     * @param raceType            The race type, 1 for match, 2 for fleet.
     * @param fleet               The fleet of boats in the race.
     */
    public RaceStatusMessage(RaceStatus raceStatus, long expectedStartTime, double courseWindDirection,
                             double courseWindSpeed, int raceType, Fleet fleet) {
        currentTime = System.currentTimeMillis();
        this.raceStatus = raceStatus;
        this.expectedStartTime = expectedStartTime;
        this.courseWindDirection = Calculator.directionToHex(courseWindDirection);
        this.courseWindSpeed = Calculator.speedKnotsToMms(courseWindSpeed);
        this.numBoatsInRace = (byte) fleet.getSize();
        this.raceType = (byte) raceType;
        for (Boat boat : fleet.getBoats()) {
            boatStatuses.put(boat.getSourceID(),
                    new BoatStatusWrapper(
                            boat.getBoatStatus(), boat.getCourseProgress(), boat.getNumberPenaltiesAwarded(),
                            boat.getNumberPenaltiesServed(), boat.getEstTimeToNextMark(), boat.getEstTimeToFinish()
                    )
            );
        }

        ByteBuffer bodyBuffer = ByteBuffer.allocate(FIXED_LENGTH + numBoatsInRace * VARIABLE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        bodyBuffer.put(messageVersionNumber);
        bodyBuffer.put(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(currentTime).array(), 0, 6);
        bodyBuffer.putInt(raceID);
        bodyBuffer.put(((byte) raceStatus.getValue()));
        bodyBuffer.put(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(this.expectedStartTime).array(), 0, 6);
        bodyBuffer.putShort((short) this.courseWindDirection);
        bodyBuffer.putShort((short) this.courseWindSpeed);
        bodyBuffer.put(numBoatsInRace);
        bodyBuffer.put(((byte) raceType));

        // Note .. if your buffer index isn't at 24 by this point.. you're doing it wrong... since all the comments have
        // disappeared..
        for (Integer sourceID : boatStatuses.keySet()) {
            bodyBuffer.putInt(sourceID);
            bodyBuffer.put(((byte) boatStatuses.get(sourceID).getBoatStatus().getValue()));
            bodyBuffer.put(((byte) boatStatuses.get(sourceID).getLegNumber()));
            bodyBuffer.put(((byte) boatStatuses.get(sourceID).getNumberPenaltiesAwarded()));
            bodyBuffer.put(((byte) boatStatuses.get(sourceID).getNumberPenaltiesServed()));
            bodyBuffer.put(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(boatStatuses.get(sourceID).getEstTimeToNextMark()).array(), 0, 6);
            bodyBuffer.put(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(boatStatuses.get(sourceID).getEstTimeToFinish()).array(), 0, 6);
        }

        setBytes(MessageUtils.generateMessageBytes(bodyBuffer.array(), MESSAGE_TYPE, (short) (FIXED_LENGTH + numBoatsInRace * VARIABLE_LENGTH)));
    }

    /**
     * Constructor used after receiving a message and verifying its type.
     *
     * @param message The bytes of the received message.
     */
    public RaceStatusMessage(Message message) {
        super(message.getBytes());

        ByteBuffer bodyBuffer = ByteBuffer.wrap(getBody()).order(ByteOrder.LITTLE_ENDIAN);
        byte[] numberContainer = new byte[Long.BYTES];

        messageVersionNumber = bodyBuffer.get();
        bodyBuffer.get(numberContainer, 0, 6);  // 6 bytes to hold the time field.
        currentTime = ByteBuffer.wrap(numberContainer).order(ByteOrder.LITTLE_ENDIAN).getLong();
        raceID = bodyBuffer.getInt();
        raceStatus = RaceStatus.values()[((int) bodyBuffer.get())];
        bodyBuffer.get(numberContainer, 0, 6);  // 6 bytes to hold the time field.
        expectedStartTime = ByteBuffer.wrap(numberContainer).order(ByteOrder.LITTLE_ENDIAN).getLong();
        courseWindDirection = bodyBuffer.getShort() & 0xFFFF;
        courseWindSpeed = bodyBuffer.getShort() & 0xFFFF;
        numBoatsInRace = bodyBuffer.get();
        raceType = bodyBuffer.get();

        for (int i = 0; i < numBoatsInRace; i++) {
            int sourceId = bodyBuffer.getInt();
            BoatStatus boatStatus = BoatStatus.values()[((int) bodyBuffer.get())];
            byte legNumber = bodyBuffer.get();
            byte numberPenaltiesAwarded = bodyBuffer.get();
            byte numberPenaltiesServed = bodyBuffer.get();
            bodyBuffer.get(numberContainer, 0, 6);  // 6 bytes to hold the time field.
            long estTimeToNextMark = ByteBuffer.wrap(numberContainer).order(ByteOrder.LITTLE_ENDIAN).getLong();
            bodyBuffer.get(numberContainer, 0, 6);  // 6 bytes to hold the time field.
            long estTimeToFinish = ByteBuffer.wrap(numberContainer).order(ByteOrder.LITTLE_ENDIAN).getLong();

            BoatStatusWrapper boatStatusWrapper = new BoatStatusWrapper(boatStatus, legNumber, numberPenaltiesAwarded,
                    numberPenaltiesServed, estTimeToNextMark, estTimeToFinish);
            boatStatuses.put(sourceId, boatStatusWrapper);
        }
    }

    private MessageTypeHandler messageChain;

    public RaceStatusMessage() {

    }

    /**
     * Sets the next in the chain of message handlers
     *
     * @param nextHandler next handler in the chain
     */
    @Override
    public void setNextMessageHandler(MessageTypeHandler nextHandler) {
        this.messageChain = nextHandler;
    }

    /**
     * If the message matches this classes message type, then the class will return a
     * new message of this type, part of the chain of responsiblity
     *
     * @param message raw message type
     * @return a message type matching this class
     */
    @Override
    public Message getMessageType(Message message) {
        if (message.getHeader().getMessageType() == MESSAGE_TYPE) {
            return new RaceStatusMessage(message);
        } else {
            // Send it on to the next in the chain
            if (this.messageChain != null) {
                return this.messageChain.getMessageType(message);
            }
        }
        return null;
    }

    /**
     * Updates a given race according to this race status message.
     *
     * Client side method.
     *
     * @param race The race to update.
     */
    @Override
    public void updateRace(Race race) {
        race.setWindDirection(Calculator.hexToDirection(courseWindDirection));
        race.setWindSpeed(Calculator.hexToDirection(getCourseWindSpeed()));
        race.setRaceState(raceStatus);
        // Not updating any of the boat information from race Status message.
        // TODO: tidy up above comment.
        Fleet fleet = race.getFleet();
        try {
            for (Boat boat : fleet.getBoats()) {
                boat.setBoatStatus(getBoatStatuses().get(boat.getSourceID()).getBoatStatus());
                if (boat.getCourseProgress() != getBoatStatuses().get(boat.getSourceID()).getLegNumber()) { //If leg number has changed
                    boat.resetTimeSinceLastMark();
                }
                boat.setCourseProgress(getBoatStatuses().get(boat.getSourceID()).getLegNumber());
                //sets the distance between the boat and its next mark to be compared for the leaderboard.

                // Grant -> i commented this out cos it don't know where it goes?
//                boat.setDistanceToNextMark(Calculator.distanceBetweenAPointAndBoat(race.getCourse().getCourseFeatureById(getBoatStatuses().get(boat.getSourceID()).getLegNumber() + 1), boat));
                if (boat.getBoatStatus() == BoatStatus.FINISHED) {
                    boat.setFinished(true);
                    boat.setSpeed(0);
                } else {
                    boat.setFinished(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("RaceStatusMessage: Got a null pointer, possibly a boat left the race");
        }
    }

    public byte getMessageVersionNumber() {
        return messageVersionNumber;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public int getRaceID() {
        return raceID;
    }

    public RaceStatus getRaceStatus() {
        return raceStatus;
    }

    public long getExpectedStartTime() {
        return expectedStartTime;
    }

    public int getCourseWindDirection() {
        return courseWindDirection;
    }

    public int getCourseWindSpeed() {
        return courseWindSpeed;
    }

    public byte getNumBoatsInRace() {
        return numBoatsInRace;
    }

    public byte getRaceType() {
        return raceType;
    }

    public Map<Integer, BoatStatusWrapper> getBoatStatuses() {
        return boatStatuses;
    }

}

