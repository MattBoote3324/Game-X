package seng302.MessageTests;

import org.junit.Assert;
import org.junit.Test;
import seng302.Common.Messages.RegistrationRequestMessage;

public class RegistrationRequestMessageTest {

    @Test
    public void testRegistrationRequestMessage(){
        byte clientType = 0x03;
        RegistrationRequestMessage boatAssignMessage = new RegistrationRequestMessage(clientType);

        RegistrationRequestMessage boatAssignMessageFromBytes = new RegistrationRequestMessage(boatAssignMessage);

        Assert.assertEquals(clientType, boatAssignMessageFromBytes.getClientType());
    }
}