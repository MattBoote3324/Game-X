package seng302.Server;

import org.xml.sax.SAXException;
import seng302.Common.*;
import seng302.Common.Messages.Message;
import seng302.Common.Utils.Calculator;
import seng302.Server.Model.Race;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Entry point for the server side code. Should be thought of as a separate program.
 */
public class ServerMain extends Observable implements Runnable, Observer {
    private static String courseName = "Paphos";
    private static Race race;
    private static ServerDataStream serverDataStream;
    // Timer for tasks that need to be done inside the server
    private Timer taskTimer;

    /**
     * Start a new Server object in its own thread.
     */
    public ServerMain() throws IOException {
        serverDataStream = new ServerDataStream();

        Thread serverThread = new Thread(this, "Server_Thread");
        serverThread.start();
    }

    static String getCourseName() {
        return courseName;
    }

    public static void setCourseName(String courseDir) {
        courseName = courseDir;
    }

    /**
     * Starts the Race Countdown clock and sets the race in the stream object
     * @param millisecondsTilStart time till start in milliseconds
     */
    public static void startRace(int millisecondsTilStart) {
        race.setRaceState(RaceStatus.PREPARATORY);
        long raceStartsIn = System.currentTimeMillis() + millisecondsTilStart;
        race.setExpectedStartTime(raceStartsIn);
        resetBoats();
    }

    /**
     * Resets boat objects back to the beginning of the race.
     */
    private static void resetBoats(){
        double boatOffset = 0;
        for(Boat boat: race.getFleet().getBoats()){
            boat.setCourseProgress(0);
            int startLineID = race.getCourse().getCourseOrder().get(1).getCourseFeatureID();
            CourseFeature startLine = race.getCourse().getCourseFeatureById(startLineID);
            CourseFeature point1 = race.getCourse().getCourseFeatureById(1);
            CourseFeature point2 = race.getCourse().getCourseFeatureById(2);
            double angleToNextMark = Calculator.angleBetweenTwoPoints(point1.getPoint1(), point2.getPoint1());
            Point startPoint = Calculator.projectedPoint(startLine.getMidPoint().getLatitude(), startLine.getMidPoint().getLongitude(), 0.05, angleToNextMark + 180);
            boat.setLatitude(startPoint.getLatitude() + boatOffset);
            boat.setLongitude(startPoint.getLongitude() + boatOffset);
            boatOffset += 0.00025;
            boat.setFinished(false);
            boat.setBoatStatus(Boat.BoatStatus.RACING);
            boat.setHeading(angleToNextMark);
            boat.setSpeed(10);
            boat.setIsSailsOut(true);
            boat.setLastUpdateTime(0);
            race.getMarkCheckPoints().put(boat, false);
        }

    }

    @Override
    public void run() {
        // Start a new timer for the server main method
        taskTimer = new Timer("ServerMain_TaskTimer");
        // Initialize course.
        String pathToXml = "/courses/" + courseName + "/race.xml";
        Course course = Course.getCourseFromXmlPath(pathToXml);

        // Initialize race.
        race = new Race();
        race.setCourse(course);
        race.setFleet(new Fleet());
        race.initWindGenerator(taskTimer);


        serverDataStream.setRace(race);
        serverDataStream.addObserver(this);
//        serverDataStream.buildCollisionMap();
        serverDataStream.createBoundary();
    }

    /**
     * Receive objects from ServerDataStream via the Observer interface.
     * Each Message has it's own updateRace method which allows it to
     * update the race it is own way
     *
     * @param o Observable that notified of object.
     * @param arg Received object.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof RaceStatus) {
            RaceStatus rs = (RaceStatus) arg;
        } else {
            ((Message) arg).updateRace(race);
        }
    }

    public Race getRace() {
        return race;
    }

    public void shutdownServer() {
        try {
            serverDataStream.shutdownStream();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return serverDataStream.getPort();
    }
}


