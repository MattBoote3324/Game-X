package seng302.Client;

import seng302.Common.Messages.*;
import seng302.Common.Utils.MessageUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Observable;
import java.util.Observer;

/**
 * Client data stream for sending data to a server and receiver data from a server.
 *
 * Should only contain generic methods that return binary data.
 * No methods that process binary data.
 */
public class ClientDataStream extends Observable implements Runnable, Observer {
    private static Socket socket;
    private static BufferedInputStream bufferedInputStream;
    private static OutputStream outputStream;

    private boolean shutdown;

    private MessageTypeHandler messageHandler;

    private int clientSourceID;

    /**
     * Initialise the input and output streams to the server
     * @param host The server's host address
     * @param port The server's port
     * @throws IOException Throw exception if this connection fails
     */
    private void initialise(String host, int port) throws IOException {
        socket = new Socket(host, port);    // Creates a stream socket for the client and connects it to the specified port number on the named host.
        socket.setSoTimeout(2000);
        bufferedInputStream = new BufferedInputStream(socket.getInputStream()); // client's receiving stream from server
        outputStream = socket.getOutputStream();    // client's output to the server
        makeMessageHandlers();
    }

    /**
     * Construct the chain of message handlers, a message will be
     * passed down the chain until it finds a matching message class type
     *
     * It has been constructed in the same order as listed in the Messages folder
     * But this could be done in an order of most used for O(n) time.
     */
    private void makeMessageHandlers() {
        // Start place for the message handling
        messageHandler = new RaceStatusMessage();

        // Make the next message handler in the change
        // start->boatActionHandler
        MessageTypeHandler bActionHandler = new BoatActionMessage();
        messageHandler.setNextMessageHandler(bActionHandler);

        // start->boatActionHandler->boatAssignHandler (get the idea now??)
        MessageTypeHandler heartBeatHandler = new HeartbeatMessage();
        bActionHandler.setNextMessageHandler(heartBeatHandler);

        MessageTypeHandler regRequestHandler = new RegistrationRequestMessage();
        heartBeatHandler.setNextMessageHandler(regRequestHandler);

        MessageTypeHandler regResponseHandler = new RegistrationResponseMessage();
        regRequestHandler.setNextMessageHandler(regResponseHandler);

        MessageTypeHandler bAvatarHandler = new BoatAvatarMessage();
        regResponseHandler.setNextMessageHandler(bAvatarHandler);

        MessageTypeHandler bLocationHandler = new BoatLocationMessage();
        bAvatarHandler.setNextMessageHandler(bLocationHandler);

        MessageTypeHandler bNameHandler = new BoatNameMessage();
        bLocationHandler.setNextMessageHandler(bNameHandler);

        MessageTypeHandler raceStatusHandler = new RaceStatusMessage();
        bNameHandler.setNextMessageHandler(raceStatusHandler);

        MessageTypeHandler xmlMsgHandler = new XmlMessage();
        raceStatusHandler.setNextMessageHandler(xmlMsgHandler);

        MessageTypeHandler yCodeHandler = new YachtEventCodeMessage();
        xmlMsgHandler.setNextMessageHandler(yCodeHandler);

        MessageTypeHandler abilityHandler = new AbilityMessage();
        yCodeHandler.setNextMessageHandler(abilityHandler);
        // End of the message handling chain
    }

    /**
     * Connects the client to a server
     * @param host Host address to connect to
     * @param port Host port to connect to
     * @throws IOException thrown if connection fails
     */
    void connect(String host, int port) throws IOException {
        initialise(host, port);
        Thread clientThread = new Thread(this, "Client_DataStream_Thread");
        clientThread.start();
    }

    /**
     * Objects are passed in here that are being observed.
     *
     * @param o   object sending the notification.
     * @param arg object that is to be dealt with.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Integer) {
            int action = ((int) arg);
            send(new BoatActionMessage(clientSourceID, (byte) action));
        }
    }

    @Override
    public void run() {
        // Send a Registration Request Message. Part of two-way handshake.
        // 0x01 means we are a player client (not a spectator or other).
        RegistrationRequestMessage registrationRequestMessage = new RegistrationRequestMessage((byte) 0x01);
        send(registrationRequestMessage);

        while (!shutdown) {
            // Receive from server
            try {
                Message message = new Message(bufferedInputStream);
                if (message.getHeader().isValid() && MessageUtils.crc(message)) {
                    setChanged();
                    delegateMessage(message);
                } else {
                    System.err.println("Invalid message dropped.");
                }
            } catch (SocketTimeoutException e) {
                System.err.println("Client's socket timed out.");
            } catch (SocketException e) {
                System.err.println("Client Socket Exception - closing");
                shutdown = true;
            } catch (Exception e) {
                System.err.println("Error in Client loop");
                e.printStackTrace();
            }
        }

        try {
            socket.close();
            bufferedInputStream.close();
            outputStream.close();
        } catch (IOException e) {
            System.err.println("Couldn't close socket or buffer stream");
            e.printStackTrace();
        }
    }

    /**
     * receives a message and delegates it depending on what type of message it is.
     * @param message message being received from server
     */
    private void delegateMessage(Message message) {
        // pass the message into the handler, from here it will return the right message type (or null if not matched)
        Message m = messageHandler.getMessageType(message);

        if (m != null) {
            // Notify everyone else..
            setChanged();
            notifyObservers(m);
            // If we have an assign message, we have to send back some stuff
            if (m instanceof RegistrationResponseMessage) {
                clientSourceID = ((RegistrationResponseMessage) m).getSourceID();
                // Also send the Boat name message
                // TODO: put short name in on the join screen?
                send(new BoatNameMessage(clientSourceID, "shortName", ClientMain.getPlayerName()));
                // Send out the avatar type
                send(new BoatAvatarMessage(clientSourceID, ClientMain.getAvatarType()));
                ClientMain.setAssignedSourceID(clientSourceID);
            } else if (m instanceof HeartbeatMessage) {
                ClientMain.heartBeatRxd = true;
            } else {
                // Update the race from client main
                m.updateRace(ClientMain.getRace());
            }
        }
    }

    /**
     * Send a message out the client dataStream
     *
     * @param message message to be sent
     */
    private void send(Message message) {
        try {
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            System.err.println("Client error: writing on output stream.");
            e.printStackTrace();
        }
    }

    /**
     * Sets the shutdown flag to true, which
     * will shutdown this thread.
     */
    void shutdown() {
        shutdown = true;
        if (ClientMain.getWatchDog() != null) {
            ClientMain.getWatchDog().cancel();
        }
    }
}