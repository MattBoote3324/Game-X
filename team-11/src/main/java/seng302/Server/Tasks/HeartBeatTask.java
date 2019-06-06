package seng302.Server.Tasks;

import seng302.Common.Messages.HeartbeatMessage;
import seng302.Server.ServerDataStream;

import java.util.TimerTask;

/**
 * Sends HeartBeat messages as a task, via the Timer scheduler
 */
public class HeartBeatTask extends TimerTask {

    private final ServerDataStream.ClientHandler client;
    private int heartbeatSeqNo;

    public HeartBeatTask(ServerDataStream.ClientHandler clientHandler) {
        client = clientHandler;
    }

    @Override
    public void run() {
        if (!client.isShutdown()) {
            client.send(new HeartbeatMessage(heartbeatSeqNo));
            heartbeatSeqNo += 1;
        } else {
            cancel();
        }
    }
}
