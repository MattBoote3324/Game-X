package seng302.Common.Messages;

import seng302.Client.ClientMain;
import seng302.Common.Boat;
import seng302.Common.Utils.Calculator;
import seng302.Common.Utils.MessageUtils;
import seng302.Server.Model.Race;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Model a boat location message.
 *
 * Hold all the necessary attributes / fields, for ease of use.
 */
public class BoatLocationMessage extends Message implements MessageTypeHandler {
    private static final short MESSAGE_LENGTH = 56;
    private static final byte MESSAGE_TYPE = (byte) 37;

    // Note: Unsigned fields must be stored in a larger data type.
    // E.g. heading is an unsigned short, so must be stored in int.
    private byte messageVersionNumber = 1;
    private long time;
    private int sourceID;
    private int sequenceNum;
    private byte deviceType;
    private int latitude;
    private int longitude;
    private int altitude;
    private int heading;              // Unsigned.
    private short pitch;
    private short roll;
    private int boatSpeed;            // Unsigned.
    private int cog;                  // Unsigned.
    private int sog;                  // Unsigned.
    private int apparentWindSpeed;    // Unsigned.
    private short apparentWindAngle;
    private int trueWindSpeed;        // Unsigned.
    private int trueWindDirection;    // Unsigned.
    private short trueWindAngle;
    private int currentDrift;         // Unsigned.
    private int currentSet;           // Unsigned.
    private short rudderAngle;

    /**
     * Constructor used when sending.
     *
     * @param boat The boat to get the information from in order to form the message.
     */
    public BoatLocationMessage(Boat boat) {
        this.time = System.currentTimeMillis();
        this.sourceID = boat.getSourceID();
        this.deviceType = (byte) 1; // 1 for a Racing yacht.
        this.latitude = Calculator.latLonToHex(boat.getLatitude());
        this.longitude = Calculator.latLonToHex(boat.getLongitude());
        this.altitude = 0; // All fields set to 0 are because we do not consider them.
        this.heading = Calculator.directionToHex(boat.getHeading());
        this.pitch = 0;
        this.roll = 0;
        this.boatSpeed = Calculator.speedKnotsToMms(boat.getSpeed());
        this.cog = 0;
        this.sog = 0; //TODO: move SOG value into Boat class, calculate server side.

        // Wind is currently the same in every part of the course.
        this.apparentWindSpeed = 0;
        this.apparentWindAngle = 0;
        // TODO: Make sure these are calculated server side, store in Boat class.
        this.trueWindSpeed = 0;
        this.trueWindDirection = 0;
        this.trueWindAngle = 0;

        this.currentDrift = 0;
        this.currentSet = 0;
        this.rudderAngle = 0;

        ByteBuffer bodyBuffer = ByteBuffer.allocate(MESSAGE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        bodyBuffer.put(messageVersionNumber);
        bodyBuffer.put(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(time).array(), 0, 6);
        bodyBuffer.putInt(sourceID);
        bodyBuffer.putInt(sequenceNum);
        bodyBuffer.put(deviceType);
        bodyBuffer.putInt(latitude);
        bodyBuffer.putInt(longitude);
        bodyBuffer.putInt(altitude);
        bodyBuffer.putShort((short) heading);
        bodyBuffer.putShort(pitch);
        bodyBuffer.putShort(roll);
        bodyBuffer.putShort((short) boatSpeed);
        bodyBuffer.putShort((short) cog);
        bodyBuffer.putShort((short) sog);
        bodyBuffer.putShort((short) apparentWindSpeed);
        bodyBuffer.putShort(apparentWindAngle);
        bodyBuffer.putShort((short) trueWindSpeed);
        bodyBuffer.putShort((short) trueWindDirection);
        bodyBuffer.putShort(trueWindAngle);
        bodyBuffer.putShort((short) currentDrift);
        bodyBuffer.putShort((short) currentSet);
        bodyBuffer.putShort(rudderAngle);

        setBytes(MessageUtils.generateMessageBytes(bodyBuffer.array(), MESSAGE_TYPE, MESSAGE_LENGTH));
    }

    /**
     * Constructor used after receiving a message and verifying its type.
     *
     * @param message received
     */
    public BoatLocationMessage(Message message) {
        super(message.getBytes());

        ByteBuffer bodyBuffer = ByteBuffer.wrap(getBody()).order(ByteOrder.LITTLE_ENDIAN);
        byte[] numberContainer = new byte[Long.BYTES];

        messageVersionNumber = bodyBuffer.get();
        bodyBuffer.get(numberContainer, 0, 6);  // 6 bytes to hold the time field.
        time = ByteBuffer.wrap(numberContainer).order(ByteOrder.LITTLE_ENDIAN).getLong();
        sourceID = bodyBuffer.getInt();
        sequenceNum = bodyBuffer.getInt();
        deviceType = bodyBuffer.get();
        latitude = bodyBuffer.getInt();
        longitude = bodyBuffer.getInt();
        altitude = bodyBuffer.getInt();
        heading = bodyBuffer.getShort() & 0xFFFF;  // To convert to unsigned short range.
        pitch = bodyBuffer.getShort();
        roll = bodyBuffer.getShort();
        boatSpeed = bodyBuffer.getShort() & 0xFFFF;
        cog = bodyBuffer.getShort() & 0xFFFF;
        sog = bodyBuffer.getShort() & 0xFFFF;
        apparentWindSpeed = bodyBuffer.getShort() & 0xFFFF;
        apparentWindAngle = bodyBuffer.getShort();
        trueWindSpeed = bodyBuffer.getShort() & 0xFFFF;
        trueWindDirection = bodyBuffer.getShort() & 0xFFFF;
        trueWindAngle = bodyBuffer.getShort();
        currentDrift = bodyBuffer.getShort() & 0xFFFF;
        currentSet = bodyBuffer.getShort() & 0xFFFF;
        rudderAngle = bodyBuffer.getShort();
    }

    private MessageTypeHandler messageChain;

    public BoatLocationMessage() {

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
            return new BoatLocationMessage(message);
        } else {
            // Send it on to the next in the chain
            if (this.messageChain != null) {
                return this.messageChain.getMessageType(message);
            }
        }
        return null;
    }

    /**
     * Updates the Race model with a Boat Location Message
     *
     * @param race race that should be updated
     */
    @Override
    public void updateRace(Race race) {
        Boat boat = race.getFleet().getBoat(sourceID);
        if (boat != null) {
            boat.setLatitude(Calculator.hexToLatLon(latitude));
            boat.setLongitude(Calculator.hexToLatLon(longitude));
            boat.setHeading(Calculator.hexToDirection(heading));
            boat.setSpeed(Calculator.speedMmsToKnots(boatSpeed));
            boat.setX(ClientMain.raceViewController.getXPos(boat.getLongitude(), ClientMain.raceViewController.isZoomed()));
            boat.setY(ClientMain.raceViewController.getYPos(boat.getLatitude(), ClientMain.raceViewController.isZoomed()));
        }
    }

    ////////////////////////
    // GETTERS // SETTERS //
    ////////////////////////

    public byte getMessageVersionNumber() {
        return messageVersionNumber;
    }

    public long getTime() {
        return time;
    }

    public int getSourceID() {
        return sourceID;
    }

    public int getSequenceNum() {
        return sequenceNum;
    }

    public byte getDeviceType() {
        return deviceType;
    }

    public int getLatitude() {
        return latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public int getAltitude() {
        return altitude;
    }

    public int getHeading() {
        return heading;
    }

    public short getPitch() {
        return pitch;
    }

    public short getRoll() {
        return roll;
    }

    public int getBoatSpeed() {
        return boatSpeed;
    }

    public int getCog() {
        return cog;
    }

    public int getSog() {
        return sog;
    }

    public int getApparentWindSpeed() {
        return apparentWindSpeed;
    }

    public short getApparentWindAngle() {
        return apparentWindAngle;
    }

    public int getTrueWindSpeed() {
        return trueWindSpeed;
    }

    public int getTrueWindDirection() {
        return trueWindDirection;
    }

    public short getTrueWindAngle() {
        return trueWindAngle;
    }

    public int getCurrentDrift() {
        return currentDrift;
    }

    public int getCurrentSet() {
        return currentSet;
    }

    public short getRudderAngle() {
        return rudderAngle;
    }
}
