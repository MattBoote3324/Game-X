package seng302.Server;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.shape.Polygon;
import seng302.Common.*;
import seng302.Common.Messages.*;
import seng302.Common.Utils.Calculator;
import seng302.Common.Utils.MessageUtils;
import seng302.Server.Model.Race;
import seng302.Server.Tasks.BoatLocationMessageTask;
import seng302.Server.Tasks.HeartBeatTask;
import seng302.Server.Tasks.RaceMessageTask;
import seng302.Server.Tasks.UpdateModelTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Server data stream for sending data to clients, and receiving data from clients.
 *
 * Constantly listen for client connections.
 * For each client connection, spawn a new client handler on a new thread.
 * MAX_CONNECTIONS sets the limit for connections to this server
 */
public class ServerDataStream extends Observable implements Runnable {
    private static final int MAX_CONNECTIONS = 20;  // Max number of connections we can take
    private static XMLModifier xmlModifier;
    private final Thread serverThread;
    private static List<ClientHandler> connections;
    private int portNumber;
    private ServerSocket serverSocket;
    private ObservableSet<ClientHandler> connectedClients = FXCollections.observableSet();
    private ObservableList<Integer> connectClientsIds = FXCollections.observableArrayList();
    private static Map<Integer, GodType> clientGodMap = new HashMap<>();
    private Course course;
    private static Fleet fleet;
    private static Race race;
    private static Polygon boundary;
    private static final int ABILITY_Q = 102;
    private static final int ABILITY_W = 103;
    public static boolean aphroditeAbilityInUse = false; //todo this maybe shouldn't be public

//    private HashMap<Point, Point> markCollisionMap;
    private boolean streamShutdown = false;

    /**
     * Create a server data stream.
     *
     */
    ServerDataStream() throws IOException {
        streamShutdown = false; // initialise it to false, give the server a chance to start
        //this.portNumber = portNumber;
        initialise();
        serverThread = new Thread(this, "Server_DataStream_Thread");
        serverThread.start();
//        markCollisionMap = new HashMap<>();
        connections = new ArrayList<>();
        // Wipe out the xml modifier.. otherwise cos it's static it will leave artificates behind
        xmlModifier = new XMLModifier();
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            connections.add(null);
        }
    }

    /**
     * Initialise the server data stream.
     */
    private void initialise() throws IOException {
        serverSocket = new ServerSocket(0);
    }

    /**
     * Notifies all clients when a client uses a god ability
     * @param boatId id of the boat that used the power
     * @param abilityType int representing the ability type
     */
    public static void abilityUsed(int boatId, int abilityType){
        GreekGod greekGod  = fleet.getBoat(boatId).getGreekGod();
        switch (abilityType) {
            case ABILITY_Q:
                greekGod.abilityQ(race, boatId);
                break;
            case ABILITY_W:
                greekGod.abilityW(race, boatId);
                break;
            default:
                break;
        }

        //TODO: put the following for loop in "If not on cool down"
        for (ClientHandler clientHandler: connections) {
            if(clientHandler != null) {
                clientHandler.send(new AbilityMessage(boatId, greekGod.getGodType().getType(), abilityType));
            }
        }
    }

    /**
     * Run the server data stream, constantly listening and accepting client connections.
     * Spawn a client handler on a new thread for each client connection.
     */
    @Override
    public void run() {



        //TODO: figure out a way to break from this loop and close the socket.
        while (!streamShutdown) {
            try {
                ClientHandler clientHandler = null;
                Thread.sleep(10);
                for (int i = 0; i < connections.size(); i++) {
                    // If we have a free spot in our list
                    if (connections.get(i) == null) {
                        // Plonk the handler right there.
                        if (!streamShutdown) {
                            clientHandler = new ClientHandler(serverSocket.accept(), i);
                            clientHandler.setName("ClientID-" + i);
                            connections.set(i, clientHandler);
                        }
                        break;
                    }
                }
                if (clientHandler != null) {
                    clientHandler.start();
                } else {
                    // ok a much better way of doing this rather than just ignoring the client if we are full
                    // would be to accept the connection, then immediately tell the client that we are full
                    // and disconnect them.
                    // for now - sorry client, its not that i'm ignoring you... ok well yes.. it totally is.
                    throw new Exception("Error, we are either full or something in the connection went wrong");
                }
            } catch (Exception e) {
                System.err.println("Server error: attempt to accept client connection failed.");
                e.printStackTrace();
            }
        }
    }

//    void buildCollisionMap() {
//        Course course = race.getCourse();
//        for (CourseFeature courseFeature : course.getCourseFeatures()) {
//            for (Point point : courseFeature.getPoints()) {
//                Point approxPoint = Calculator.roundPointPosition(point, Boat.COLLISION_THRESHOLD);
//                markCollisionMap.put(approxPoint, point);
//            }
//        }
//    }

    void setRace(Race race) {
        ServerDataStream.race = race;
        fleet = race.getFleet();
        this.course = race.getCourse();
    }

    /**
     * Tells the server stream to start closing
     * @throws InterruptedException if interrupted
     */
    void shutdownStream() throws InterruptedException {

        for (ClientHandler c : connections) {
            // Tell the connected clients threads to shutdown
            if (c != null) {
                c.clientHandlerShutdown = true;
            }
        }
        // Yank the server socket closed..

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        streamShutdown = true;
    }

    private boolean checkAllDisconnected(List<ClientHandler> connections) {
        for (ClientHandler c : connections) {
            if (c != null) {
                return false;
            }
        }
        return true;
    }


    /**
     * Creates a polygon from the feature points
     * that will allow the server to check if the boat is in the course or not
     */
    void createBoundary() {
        boundary = new Polygon();
        //For example

        for (Point boundaryPoint : course.getBoundaryPoints()){
            //BP always "0", prints source ID, which seems pretty deprecated....
            //Points seem deprecated, they have a width value, why do we have so much useless stuff in this app now?!?!?!
            //It's crazy man, rip, ah well, it's still functional!
            boundary.getPoints().addAll(boundaryPoint.getLatitude(), boundaryPoint.getLongitude());
        }
    }

    /**
     * Returns a boolean indicating whhether the boat is in the course boundary or out of in, true and false respectively
     * @param boat boat to check position
     * @return true if boat in course bounds
     */
    public static boolean isInBoundary(Boat boat){
        return boundary.contains(boat.getLatitude(), boat.getLongitude());
    }

    boolean isShutdown() {
        return streamShutdown;
    }

    /**
     * Handle two way communication for a single client, on its own thread.
     */
    public class ClientHandler extends Thread {
        private Socket clientSocket;
        private BufferedInputStream bufferedInputStream;
        private OutputStream outputStream;
        private int clientSourceID;
        private Timer clientTasks;
        private boolean clientHandlerShutdown;
        private GodType godType;

        /**
         * Create a client handler.
         *
         * @param clientSocket   The socket used to communicate with the client.
         * @param clientSourceID The clients source ID.
         */
        ClientHandler(Socket clientSocket, int clientSourceID) {
            this.clientSocket = clientSocket;
            this.clientSourceID = clientSourceID;

        }

        /**
         * Run the client handler on a new thread.
         * Setup input and output streams, and begin two way communication with the client.
         */
        @Override
        public void run() {
            initialise();

            //TODO: update to two way handshake.
            if (initialiseClientConnection()) {
                receiveNameAndAvatar();
                xmlModifier.addBoat(race.getFleet().getBoat(clientSourceID));
                updateClients();
                sendRaceXml();
                sendRegattaXML();

                // Create a new Timer for tasks done by this thread.
                // NB this timer gets cancelled AS SOON as it cannot write to the socket (send())
                clientTasks = new Timer(Thread.currentThread().getName());

                // Create a new Timer Task for updating the model at @ 20mS or 50Hz (as fast as the boat location messages)
                clientTasks.schedule(new UpdateModelTask(this, race), 0, 20);

                clientTasks.schedule(new HeartBeatTask(this), 0, 5000);

                // Create a new Timer Task for sending the raceStatusMessags @ 40mS or 25Hz
                clientTasks.schedule(new RaceMessageTask(this, race), 0, 40);

                // Create a new Timer Task for sending the boatLocation Messages out at @ 20mS or 50Hz
                clientTasks.schedule(new BoatLocationMessageTask(this, race), 0, 20);

                // While we aren't being shutdown (or crashing) then sit forever waiting for key presses
                while (!clientHandlerShutdown) {
                    handleKeyPress(); // Message is using a blocking call, so it won't waste CPU in here.
                }
            }

            // Do a thread clean up before exiting
            cleanUp();
        }

        /**
         * Cleans up before exiting the thread,
         * - Removes itself from the list of connected clients
         * - Removes itself from the BoatConfig XML
         * - Removes itself from the race fleet
         * - Broadcasts change to other clients
         * - Closes socket / stream
         * - Exit
         */
        private void cleanUp() {
            // Remove us as a connected client
            clientTasks.cancel();

            connections.set(clientSourceID, null);

            // Remove us from the XML Config as well.
            if (race.getFleet().getBoat(clientSourceID) != null) {
                xmlModifier.removeBoat(race.getFleet().getBoat(clientSourceID));
            }

            // Remove us from the race fleet
            race.getFleet().remove(race.getFleet().getBoat(clientSourceID));

            // Update the other clients that we have left
            this.updateClients();

            // try performing a close socket ceremony with tea and crumpets
            try {
                this.clientSocket.close();
                this.bufferedInputStream.close();
            } catch (IOException e) {
                // No crumpets were harmed in the closing of streams and sockets
                e.printStackTrace();
            }
        }

        /**
         * Initialise the input and output streams.
         */
        private void initialise() {
            try {
                bufferedInputStream = new BufferedInputStream(clientSocket.getInputStream());
                outputStream = clientSocket.getOutputStream();
            } catch (IOException e) {
                System.err.println("Server error: initialising ServerDataStream.");
                e.printStackTrace();
            }
        }

        /**
         * Initialise basic client connection.
         * <p>
         * Wait for client to request a connection, then give a response.
         * Then receive client name and avatar.
         * <p>
         * Part of two-way handshake.
         *
         * @return True if successful
         */
        private boolean initialiseClientConnection() {
            // Try to get a Registration Request Message from the client. Try 5 times, with a 1 second break between.
            // Also get BoatNameMessage or BoatAvatarMessage.
            if (race.getRaceState().equals(RaceStatus.STARTED)) {
                return false;
            }
            int tryCount = 1;
            while (tryCount <= 5) {
                try {
                    if (bufferedInputStream.available() > 0) {
                        Message message = new Message(bufferedInputStream);
                        if (message.getHeader().isValid() && MessageUtils.crc(message)) {
                            if (message.getHeader().getMessageType() == RegistrationRequestMessage.MESSAGE_TYPE) {
                                RegistrationRequestMessage registrationRequestMessage = new RegistrationRequestMessage(message);
                                // For now, we only want to handle clients that are players, since we have not implemented spectating etc.
                                if (registrationRequestMessage.getClientType() == 1 && !race.getRaceState().equals(RaceStatus.STARTED)) {
                                    RegistrationResponseMessage registrationResponseMessage = new RegistrationResponseMessage(clientSourceID, (byte) 0x01);
                                    setChanged();
                                    notifyObservers(registrationResponseMessage);
                                    send(registrationResponseMessage);
                                    return true;
                                }
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                            tryCount++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        /**
         * Receive boat name and boat avatar messages.
         */
        private void receiveNameAndAvatar() {
            int amountReceived = 0; // How many valid messages have we received in this method?
            int tryCount = 1;
            while (tryCount <= 3) {
                try {
                    if (bufferedInputStream.available() > 0) {
                        Message message = new Message(bufferedInputStream);
                        if (message.getHeader().isValid() && MessageUtils.crc(message)) {
                            if (message.getHeader().getMessageType() == BoatNameMessage.MESSAGE_TYPE) {
                                // Check to see if we've got a boat name from the client
                                // Break now after we have this message
                                BoatNameMessage bnMsg = new BoatNameMessage(message);
                                setChanged();
                                notifyObservers(bnMsg);
                                if (++amountReceived == 2) {
                                    break;
                                }
                            } else if (message.getHeader().getMessageType() == BoatAvatarMessage.MESSAGE_TYPE) {
                                BoatAvatarMessage baMsg = new BoatAvatarMessage(message);
                                //Terrifying way to get the godType but it works for now
                                godType = GodType.values()[baMsg.getType()];
                                clientGodMap.put(clientSourceID, godType);
                                setChanged();
                                notifyObservers(baMsg);
                                if (++amountReceived == 2) {
                                    break;
                                }
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                            tryCount++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    System.err.print("shutting down connected client thread");
                    clientHandlerShutdown = true;
                    e.printStackTrace();
                    break;
                }
            }
        }

        /**
         * Send the Race XML to the client.
         */
        private void sendRaceXml() {
            // Send race.xml to client.
            InputStream inputStream = getClass().getResourceAsStream("/courses/" + ServerMain.getCourseName() + "/race.xml");
            String raceXmlText;
            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                raceXmlText = scanner.useDelimiter("\\A").next();
            }
            XmlMessage xmlMessage = new XmlMessage(XmlMessage.RACE_SUBTYPE, (short) raceXmlText.length(), raceXmlText);
            send(xmlMessage);
        }

        /**
         * Send the Boat XML to the client.
         */
        void sendBoatXml() {
            String boatConfigData = xmlModifier.getStringWriter();
            XmlMessage xmlMessage = new XmlMessage(XmlMessage.BOAT_SUBTYPE, (short) boatConfigData.length(), boatConfigData);
            send(xmlMessage);

        }

        /**
         * Send the Regatta XML to the client.
         */
        private void sendRegattaXML() {
            InputStream inputStream = getClass().getResourceAsStream("/xml/Regatta.xml"); //Should be obtained dynamically
            String regattaText;
            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                regattaText = scanner.useDelimiter("\\A").next();
                scanner.close();
                inputStream.close();
                XmlMessage xmlMessage = new XmlMessage(XmlMessage.REGATTA_SUBTYPE, (short) regattaText.length(), regattaText);
                send(xmlMessage);
            } catch (IOException e) {
                System.err.println("Server: Error in sending regatta XML");
                e.printStackTrace();
            }
        }

        /**
         * Updates the connected clients with new BoatXML
         */
        private void updateClients() {
            for (ClientHandler clientHandler : connections) {
                // Skip all the null entries in our list
                if (clientHandler != null) {
                    clientHandler.sendBoatXml();
                }
            }
        }

        /**
         * Handle a key press from the client.
         * Notify observers so that model can be updated according to key press.
         */
        private void handleKeyPress() {
            try {
                Message message = new Message(bufferedInputStream);
                if (message.getHeader().isValid() && MessageUtils.crc(message)) {
                    if (message.getHeader().getMessageType() == BoatActionMessage.MESSAGE_TYPE) {
                        BoatActionMessage boatActionMessage = new BoatActionMessage(message);
                        setChanged();
                        notifyObservers(boatActionMessage);
                    }
                }
            } catch (SocketException e) {
                System.err.println("Server got an Socket Exception... shuting down client handler");
                clientHandlerShutdown = true;
            } catch (IOException e) {
                if (e.getMessage().equals("Message: BufferInputStream Read Error!")) {
                    System.err.println("Couldn't write to Datastream? Server Disconnected?");
                    clientHandlerShutdown = true;
                } else {
                    System.err.println("Server error: getting message form input stream failed.");
                    e.printStackTrace();
                }
            }
        }

        /**
         * checks if any boat is using the ares W ability and if so calls the GodAres abilityW method to handle it.
         */
        private void aresAbilityCollision() {
            List<Boat> aresAbilityOnList = fleet.getAresAbilityOnList();
            if (aresAbilityOnList.size() != 0) {
                Boat boat = aresAbilityOnList.get(0);
                boat.getGreekGod().abilityW(race, boat.getSourceID());
            }
        }


        /**
         * Update the server model.
         * <p>
         * Update boat speeds, positions, and race progress.
         */
        public void updateModel() {
            if (System.currentTimeMillis() > race.getExpectedStartTime()) {
                race.setRaceState(RaceStatus.STARTED);
            }
            boolean raceFinished = true;
            Fleet fleet = race.getFleet();
            for (Boat boat : fleet.getBoats()) {
                if (!boat.isFinished()) {
                    if (!boat.isDisable()) { // Not disabled
                        boat.updateSpeed(race.getWindDirection(), race.getWindSpeed());
                        boat.updateLocation(System.currentTimeMillis());
                        Platform.runLater(() -> race.updateBoatProgress(boat));
                    } else {//Is disabled
                        boat.updateLocation(System.currentTimeMillis());
                    }
                    boat.checkForMarkCollision(course.getCourseFeatures());
                    raceFinished = false;
                } else {
                    boat.setSpeed(0);
                }
            }
            if (raceFinished && race.getRaceState() != RaceStatus.FINISHED) {
                race.setRaceState(RaceStatus.FINISHED);
                setChanged();
                notifyObservers(RaceStatus.FINISHED);
            }
            fleet.checkForBoatCollision();
            aresAbilityCollision();

            Platform.runLater(fleet::checkCrossingBoundary);
            //fleet.checkCrossingBoundary();
        }


        /**
         * Send a message on the output stream.
         *
         * @param message The message to send.
         */
        public void send(Message message) {
            try {
                if (!clientHandlerShutdown) {
                    outputStream.write(message.getBytes());
                }
            } catch (IOException e) {
                // This probably means that the client can't be there anymore (broken pipe?)
                System.err.println("Server error: writing on client output stream failed (client disconnected?)\n" +
                        "Client Handler shutdown now in progress");
                // e.printStackTrace();
                clientHandlerShutdown = true;  // Shutdown this thread.
                clientTasks.cancel();  //Cancel all the tasks.
            }
        }

        public boolean isShutdown() {
            return clientHandlerShutdown;
        }
        }

    public int getPort(){
        return serverSocket.getLocalPort();
    }

}
