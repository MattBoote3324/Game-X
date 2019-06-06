package seng302.Server.Tasks;

import seng302.Common.Boat;
import seng302.Common.Messages.BoatLocationMessage;
import seng302.Server.Model.Race;
import seng302.Server.ServerDataStream;

import java.util.TimerTask;

/**
 * Sends boat location messages as a task, via the Timer scheduler
 */
public class BoatLocationMessageTask extends TimerTask {

    private final ServerDataStream.ClientHandler client;
    private final Race race;

    public BoatLocationMessageTask(ServerDataStream.ClientHandler clientHandler, Race race) {
        client = clientHandler;
        this.race = race;
    }

    /**
     * Form and send boat location messages for each boat in the fleet.
     */
    @Override
    public void run() {
        if (!client.isShutdown()) {
            for (Boat boat : race.getFleet().getBoats()) {
                BoatLocationMessage boatLocationMessage = new BoatLocationMessage(boat);
                client.send(boatLocationMessage);
            }
        } else {
            cancel();
        }
    }
}
