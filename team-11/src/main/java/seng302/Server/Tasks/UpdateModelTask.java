package seng302.Server.Tasks;

import seng302.Common.RaceStatus;
import seng302.Server.Model.Race;
import seng302.Server.ServerDataStream;

import java.util.TimerTask;

/**
 * Calls the client to update its model, checking the boat progress and the collision state
 */
public class UpdateModelTask extends TimerTask {
    private final ServerDataStream.ClientHandler client;
    private final Race race;

    public UpdateModelTask(ServerDataStream.ClientHandler clientHandler, Race race) {
        client = clientHandler;
        this.race = race;
    }

    @Override
    public void run() {
        if (!client.isShutdown()) {
            // Don't think that this needs to be here... it's not used
            if (!race.getRaceState().equals(RaceStatus.NOT_ACTIVE) && !race.getRaceState().equals(RaceStatus.FINISHED)) {
                client.updateModel();
            }
        } else {
            cancel();
        }
    }
}
