package seng302.Server.Tasks;

import seng302.Common.Messages.RaceStatusMessage;
import seng302.Common.RaceStatus;
import seng302.Server.Model.Race;
import seng302.Server.ServerDataStream;

import java.util.TimerTask;

/**
 * Class for a timer task relating to RaceStatusMessages, sent via the Timer scheduler
 */
public class RaceMessageTask extends TimerTask {

    private final ServerDataStream.ClientHandler client;
    private final Race race;

    public RaceMessageTask(ServerDataStream.ClientHandler clientHandler, Race race) {
        client = clientHandler;
        this.race = race;
    }

    @Override
    public void run() {
        if (!client.isShutdown()) {
            // Don't think that this needs to be here... it's not used
            if (!race.getRaceState().equals(RaceStatus.NOT_ACTIVE)) {
                sendRaceStatusMessage();
            }
        } else {
            cancel();
        }
    }

    /**
     * Form and send a race status message based on the race and fleet model.
     */
    public void sendRaceStatusMessage() {
        RaceStatusMessage raceStatusMessage = new RaceStatusMessage(
                race.getRaceState(),
                race.getExpectedStartTime(),
                race.getWindDirection(),
                race.getWindSpeed(),
                2, // 2 for a fleet race. Probably won't ever be 1 for match race.
                race.getFleet()
        );
        client.send(raceStatusMessage);
    }
}
