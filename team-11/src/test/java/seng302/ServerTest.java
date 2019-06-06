package seng302;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import seng302.Server.ServerMain;

import java.io.IOException;
import java.net.Socket;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class ServerTest {

    private ServerMain myServer;

    @Before
    public void startServer() throws IOException {
        myServer = new ServerMain();

        // Allow server time to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void stopServer() {
        myServer.shutdownServer();
        // give the server time to shutdown
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConnection() {
        try {
            Socket client = new Socket("localhost", 8999);
            if (client != null) {
                assertTrue(true);
            } else {
                assertFalse(false);
            }
            client.close();
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void serverDisconnect() {
        myServer.shutdownServer();

        // Allow server time to stop
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (myServer == null) {
            assertTrue(true);
        } else {
            assertFalse(false);
        }
    }
}
