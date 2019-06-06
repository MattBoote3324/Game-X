package seng302.Common.Messages;

import seng302.Common.Boat;
import seng302.Common.Utils.MessageUtils;
import seng302.Server.Model.Race;
import seng302.Server.ServerDataStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Model a boat action message.
 *
 * Hold all the necessary attributes / fields, for ease of use.
 */
public class BoatActionMessage extends Message implements MessageTypeHandler {
    private static final short MESSAGE_LENGTH = 5; // 4 bytes for sourceID, 1 byte for action.
    public static final byte MESSAGE_TYPE = (byte) 100;
    private static final long W_ABILITY_COOL_DOWN = 5;  // Value in milliseconds the W Ability will be unavailable
    private static final long Q_ABILITY_COOL_DOWN = 5;  // Value in milliseconds the Q Ability will be unavailable

    private int sourceID;
    private byte action;
    private MessageTypeHandler messageChain;

    /**
     * Constructor used when sending.
     *
     * @param sourceID The source ID of the player controller boat.
     * @param action The action the boat is performing, as defined in the controller protocol.
     */
    public BoatActionMessage(int sourceID, byte action) {
        this.sourceID = sourceID;
        this.action = action;

        ByteBuffer bodyBuffer = ByteBuffer.allocate(MESSAGE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        bodyBuffer.put(action);
        bodyBuffer.putInt(sourceID);


        setBytes(MessageUtils.generateMessageBytes(bodyBuffer.array(), MESSAGE_TYPE, MESSAGE_LENGTH));
    }

    /**
     * Constructor used after receiving a message and verifying its type.
     *
     * @param message The received message.
     */
    public BoatActionMessage(Message message) {
        super(message.getBytes());

        ByteBuffer byteBuffer = ByteBuffer.wrap(getBody()).order(ByteOrder.LITTLE_ENDIAN);
        action = byteBuffer.get();
        sourceID = byteBuffer.getInt();

    }

    public BoatActionMessage() {

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
            return new BoatActionMessage(message);
        } else {
            // Send it on to the next in the chain
            if (this.messageChain != null) {
                return this.messageChain.getMessageType(message);
            }
        }

        return null;
    }

    /**
     * Handle our boat Action messages here.
     * These are messages of what keys user pressed and what actions to perform
     * on their boat.
     * TODO: Refactor these INTO the boataction message class where they belong.
     *
     * @param race passed in message
     */
    @Override
    public void updateRace(Race race) {
        Boat boat = race.getFleet().getBoat(getSourceID());
        switch (getAction()) {
            case 1:  // Snap to VMG:
                boat.setBestAngle(race.getWindDirection(), race.getWindSpeed());
                break;
            case 2:  // Sails in (Going).
                boat.sailsIn(race.getWindDirection(), race.getWindSpeed());
                break;
            case 3:  // Sails out (Not going).
                boat.sailsOut(race.getWindDirection(), race.getWindSpeed());
                break;
            case 4:  // Tack / gybe.
                boat.tack(race.getWindDirection());
                break;
            case 5:  // Head upwind.
                boat.upwindHeadingChange(race.getWindDirection());
                break;
            case 6:  // Head downwind.
                boat.downwindHeadingChange(race.getWindDirection());
                break;
            case 7: // Turn left
                boat.counterClockwiseHeadingChange();
                break;
            case 8: // Turn right
                boat.clockwiseHeadingChange();
                break;
            case 102: // abilityQ
                Timer qTimeout;
                if (boat.canUseQ()) {
                    ServerDataStream.abilityUsed(boat.getSourceID(), getAction());
                    boat.qSetUnavailable();
                    qTimeout = new Timer("qTimeout for boat " + boat.getSourceID());
                    qTimeout.schedule(new TimerTask() {
                        int timeElapsed = 0;
                        @Override
                        public void run() {
                            if (timeElapsed++ >= Q_ABILITY_COOL_DOWN) {
                                boat.qSetAvailable();
                                cancel();
                            } else {
                                boat.setqTimeLeft(timeElapsed);
                            }
                        }
                    }, 0, 1000);
                }
                break;
            case 103: // abilityW
                if (boat.canUseW()) {
                    ServerDataStream.abilityUsed(boat.getSourceID(), getAction());
                    boat.wSetUnavailable();
                    Timer wTimeout = new Timer("wTimeout for boat " + boat.getSourceID());
                    wTimeout.schedule(new TimerTask() {
                        int timeElapsed = 0;
                        @Override
                        public void run() {
                            if (timeElapsed++ >= (boat.getGreekGod().getCOOLDOWN_PERIOD())/1000) {
                                boat.wSetAvailable();
                                cancel();
                            } else {
                                boat.setwTimeLeft(timeElapsed);
                        }
                        }
                    }, 0, 1000);
                }
                break;
            default:
                break;
        }
    }

    ////////////////////////
    // GETTERS // SETTERS //
    ////////////////////////

    public int getSourceID() {
        return sourceID;
    }

    public byte getAction() {
        return action;
    }
}
