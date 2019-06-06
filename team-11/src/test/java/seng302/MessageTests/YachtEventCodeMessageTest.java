package seng302.MessageTests;

import org.junit.Before;
import org.junit.Test;
import seng302.Common.Boat;
import seng302.Common.Messages.YachtEventCodeMessage;

import static org.junit.Assert.assertEquals;

public class YachtEventCodeMessageTest {

    private Boat boat;
    private YachtEventCodeMessage message;

    @Before
    public void setup(){
        boat = new Boat(3,"Emirates Team New Zealand", "NZL");
        message = new YachtEventCodeMessage(boat, 7, 15);    // Incident ID is arbitrarily 7, event ID is DNF
    }

    /**
     * Testing that the generated values are what we expect
     */
    @Test
    public void testCreateYachtEventCodeMessage() {
        assertEquals(3, message.getDestinationSrcId());
        assertEquals(7, message.getIncidentId());
        assertEquals(15, message.getEventId());
    }
}
