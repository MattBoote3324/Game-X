package seng302;

import seng302.Client.ClientMain;

import java.util.Timer;
import java.util.TimerTask;

public class App {

    /**
     * Entry point for the program.
     * @param args Args that can be passed into the app
     */
    public static void main(String[] args) {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.gc();
            }
        }, 0, 1000);

        com.sun.javafx.application.PlatformImpl.startup(()->{}); //Stops toolkit not initialized exception
        ClientMain client = new ClientMain();
        if (args.length > 0 && args[0].equals("debug")) {
            System.out.println("In debug mode");
            client.setDebug();
        }

        Thread clientThread = new Thread(client, "Client_Thread");
        clientThread.start();
    }
}