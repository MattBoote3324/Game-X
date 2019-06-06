package seng302;

import API.GoogleTimeZone;
import junit.framework.TestCase;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GoogleApiTest extends TestCase {

    public void testGoogleApi(){
        InetAddress hostname = null;
        try {
            hostname = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // Check if the hostname is the CI Machine
        if(!hostname.getHostName().equals("csse-s302g1")) {
            GoogleTimeZone gtz = new GoogleTimeZone(57.673945,11.84171);

            //looking at bermuda timezone
            assertEquals(3600.0000000, gtz.getRaw_offset(), 0.1);
            assertEquals(0.0000000, gtz.getDls_offset(), 0.0000001);
            assertEquals("Central European Standard Time", gtz.getTime_zone_name());
        }
    }
}
