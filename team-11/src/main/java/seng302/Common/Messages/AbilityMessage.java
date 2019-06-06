package seng302.Common.Messages;

import seng302.Client.ClientMain;
import seng302.Client.Controllers.AbilityDrawer;
import seng302.Common.Boat;
import seng302.Common.GodType;
import seng302.Common.Utils.MessageUtils;
import seng302.Server.Model.Race;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static seng302.Common.GodType.APHRODITE;

/**
 * Created by emr65 on 9/09/17.
 */
public class AbilityMessage extends Message implements MessageTypeHandler {

    private static final short MESSAGE_LENGTH = 12;
    public static final byte MESSAGE_TYPE = (byte) 118;

    private int boatId;
    private int godType;
    private int abilityType;
    private MessageTypeHandler messageChain;

    private int type;
    public AbilityMessage(){

    }


    public AbilityMessage(int boatId, int godType, int abilityType){
        this.boatId = boatId;
        this.godType = godType;
        this.abilityType = abilityType;


        ByteBuffer bodyBuffer = ByteBuffer.allocate(MESSAGE_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        bodyBuffer.putInt(boatId);
        bodyBuffer.putInt(godType);
        bodyBuffer.putInt(abilityType);

        setBytes(MessageUtils.generateMessageBytes(bodyBuffer.array(), MESSAGE_TYPE, MESSAGE_LENGTH));
    }

    public AbilityMessage(Message message){
        super(message.getBytes());
        ByteBuffer bodyBuffer = ByteBuffer.wrap(getBody()).order(ByteOrder.LITTLE_ENDIAN);
        boatId = bodyBuffer.getInt();
        godType = bodyBuffer.getInt();
        abilityType = bodyBuffer.getInt();
    }

    @Override
    public void setNextMessageHandler(MessageTypeHandler nextHandler) {
        this.messageChain = nextHandler;
    }

    /**
     * If the message matches this classes message type, then the class will return a
     * new message of this type, part of the chain of responsibility
     *
     * @param message raw message type
     * @return a message type matching this class
     */
    @Override
    public Message getMessageType(Message message) {
        if (message.getHeader().getMessageType() == MESSAGE_TYPE) {
            return new AbilityMessage(message);
        } else {
            // Send it on to the next in the chain
            if (this.messageChain != null) {
                return this.messageChain.getMessageType(message);
            }
        }
        return null;
    }

    @Override
    public void updateRace(Race race){
        //TODO probably want to check cooldown server side
        // this is run once the ability message reach all clients.
        // this is where you would call the ability drawer.
        // any logic should be done server side inside the Gods Concrete ability
            if (getAbilityType() == 102) {
                AbilityDrawer.abilityQ(godType, getSourceID());
            }
            if(getAbilityType() == 103){
                AbilityDrawer.abilityW(godType, getSourceID(), race);
            }

    }

    public int getSourceID() {
        return boatId;
    }

    public int getAbilityType() {
        return abilityType;
    }
}
