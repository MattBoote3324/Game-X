package seng302;

import cucumber.runtime.junit.Assertions;
import org.junit.Assert;
import org.junit.Test;
import seng302.Common.Boat;
import seng302.Server.ServerMain;

/**
 * Created by dki27 on 12/09/17.
 */
public class BoundaryTest {
    //TODO - Get the server testing working.
//    ServerMain serverMain = new ServerMain();
//
//    @Test
//    public void christchurchBoundaryTest() {
//        serverMain.setCourseName("Ionian Sea");
//
//        //Todo - Seems like the createBoundary is called, but it's null, probably before the course gets officially set, idk bruv
//
//        //A value in the bounds in bermuda, should print true
//        Boat inBounds = new Boat(1, "inBounds", "TRU");
//        inBounds.setLongitude(57.671551);
//        inBounds.setLatitude(11.838397);
//
//        //My childhood home, should print false
//        Boat notInBounds = new Boat(2, "notInBounds", "FAL");
//        inBounds.setLongitude(51.977483);
//        inBounds.setLatitude(-0.211382);
//
//        Assert.assertTrue(serverMain.getServerDataStream().isInBoundary(inBounds));
//
//        Assert.assertFalse(serverMain.getServerDataStream().isInBoundary(notInBounds));
//    }

}
