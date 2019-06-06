package seng302.Client;

import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import seng302.Client.Controllers.*;
import seng302.Common.Fleet;
import seng302.Common.GUIHelper;
import seng302.Common.Utils.OsCheck;
import seng302.Common.Utils.Sound;
import seng302.Server.Model.Race;
import seng302.Server.ServerMain;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.*;

/**
 * Entry point for the client side. Should be thought of as a separate program.
 */
public class ClientMain extends Application implements Runnable {
    // SOUNDS here
    private MediaPlayer mediaPlayer;
    private static Media gameMedia = new Media(AbilityDrawer.class.getResource("/soundz/SkyeCuillin.mp3").toString());
    private static Media lobbyMedia = new Media(AbilityDrawer.class.getResource("/soundz/AgeOfSailing.mp3").toString());
    private static Media lobbyThunder = new Media(AbilityDrawer.class.getResource("/soundz/lightningstorm.mp3").toString());


    public static final List<MediaPlayer> mediaPlayers = new ArrayList<>();
    private static boolean muted = false;

    private static int assignedSourceID;  // Fields are static to be more thread safe.

    private static ClientDataStream clientDataStream;
    private static Race race;
    private static boolean FULLSCREEN = true;
    public static ServerMain clientServer;
    private static boolean qCoolDown;
    private static boolean wCoolDown;
    private double SPLASH_DELAY = 1.5; //In seconds

    // FXML loaded controllers.
    public static RaceViewController raceViewController;
    public static MainWindowController mainWindowController;
    public static TimerController timerController;
    public static WindDirectionController windDirectionController;
    public static InitialMenuController initialMenuController;
    public static LobbyViewController lobbyViewController;
    public static JoinGameController joinGameController;
    public static CreateGameController createGameController;

    private static KeyPressHandler keyPressHandler = new KeyPressHandler();

    // GUI attributes.
    private static Stage initialStage;
    private static Stage primaryStage;
    private static Scene primaryScene;

    private static Parent createGameRoot;
    private static Parent initialMenuRoot;
    private static Parent mainWindowRoot;
    private static Parent joinViewRoot;
    private static Parent lobbyViewRoot;

    public static SceneName currentSceneName = SceneName.INITIAL;

    private static String playerName = "Player"; // Our default player name.
    private static int avatarType;
    static boolean heartBeatRxd = true;  // Assume initially that we are getting heart beat messages

    private static int SERVER_TIMEOUT = 6000; // time in milliseconds before shutting down the client stream
    public static boolean debug = false;

    private static Timer watchDog;

    /**
     * Start the client data stream.
     *
     * Also starts a timer which will shutdown the datastream if
     * There is no Heartbeat messages for SERVER_TIMEOUT time
     *
     * Called when user presses button to connect to a server.
     * @param host String of a host to connect to
     * @param port Int of the hosts port to connect to
     */
    public static void connectTo(String host, int port) throws IOException {
        clientDataStream.connect(host, port);

        // Create a new
        watchDog = new Timer();
        TimerTask resetTask = new TimerTask() {
            // Ok so the game here, is that when the heart beat msg comes in it will set heartBeatRxd to True
            // We then set it back to false, if it remains at false, then we know we have had a heart beat
            // This could also be done with a counter, but we're talking over 6-7 seconds here of no network msg's.
            @Override
            public void run() {
                if (!heartBeatRxd) {


                    if (ClientMain.getDataStream() != null){
                        ClientMain.getDataStream().deleteObserver(mainWindowController);
                    }

                    ClientMain.resetClient();

                    Platform.runLater(() -> {
                        makeAlertBox("Banished!", "You were banished from the realm!");

                        changeScene(SceneName.INITIAL);
                        mainWindowController.clearFinishPane();
                    });
                } else {
                    heartBeatRxd = false;
                }
            }
        };
        // Schedule every SERVER_TIMEOUT time period with a 1/5 delay time.
        watchDog.schedule(resetTask, SERVER_TIMEOUT / 5, SERVER_TIMEOUT);
    }

    public static ClientDataStream getDataStream() {
        return clientDataStream;
    }

    public static Window getStage() {
        return primaryStage;
    }

    public static void setQCoolDown(boolean val) {
        qCoolDown = val;
    }

    public static boolean isCoolDownQ() {
        return qCoolDown;
    }

    public static void setWCoolDown(boolean val) {
        wCoolDown = val;
    }

    public static boolean isCoolDownW() {
        return wCoolDown;
    }

    @Override
    public void run() {
        race = new Race();
        race.setFleet(new Fleet());

        // Launch the GUI.
        launch();
    }

    public static Race getRace() {
        return race;
    }

    /**
     * A public void that allows other classes to change the scene shown in the stage, allows for easy switching
     * from the initial menu to a lobby, or the init menu to a sp game, or the lobby to the raceview etc...
     *
     * @param sceneName A sceneName corresponding to the scene code to be displayed
     */
    public static void changeScene(SceneName sceneName) {
        switch (sceneName) {
            case INITIAL:

                if(currentSceneName == SceneName.MAIN) {
                    stopSound(gameMedia);
                    try {
                        Sound lobbySound = new Sound(lobbyMedia, true, true);
                    } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                        e.printStackTrace();
                    }
                }

                initialMenuController.setMuteIcon();
                GUIHelper.setViewPort(initialMenuController.backgroundImageView, 0, 0);
                primaryStage.getScene().setRoot(initialMenuRoot);
                GUIHelper.fadeElementPane(initialMenuController.elements, 500.0,true);
                currentSceneName = SceneName.INITIAL;
                break;
            case MAIN:
                mainWindowController.setMuteIcon();
                primaryStage.getScene().setRoot(mainWindowRoot);
                currentSceneName = SceneName.MAIN;
                keyPressHandler.setup(primaryStage.getScene());
                // Add two classes that really need to know about key presses
                stopSound(lobbyMedia);
                try {
                    Sound gameSound = new Sound(gameMedia, true, true);
                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                    e.printStackTrace();
                }

                break;
            case JOIN:
                //Checks if host
                joinGameController.setMuteIcon();

                if (clientServer != null){
                    joinGameController.setIpAddressEnabled(false);
                    ClientMain.lobbyViewController.setStartButtonVisibility(true);
                    ClientMain.joinGameController.setTxtIpAddress(clientServer.getPort());
                }else {
                    joinGameController.setIpAddressEnabled(true);
                    ClientMain.lobbyViewController.setStartButtonVisibility(false);

                }
                // Start a new stream for the client to receive data from the server
                clientDataStream = new ClientDataStream();
                keyPressHandler.addObserver(clientDataStream);
                keyPressHandler.addObserver(raceViewController);
                clientDataStream.addObserver(timerController);
                GUIHelper.setViewPort(joinGameController.backgroundImageView, 1920, 0);
                primaryStage.getScene().setRoot(joinViewRoot);
                GUIHelper.fadeElementPane(joinGameController.elements, 500.0,true);
                currentSceneName = SceneName.JOIN;
                break;
            case BOWSER:
                System.out.println("Tough luck, Mario! Princess Toadstool isn't here...");
                System.out.println("Go ahead--just try to grab me by the tail! You'll never be able to swing ME around!");
                System.out.println("Gwa ha haaaaa!");
                currentSceneName = SceneName.BOWSER;
                break;
            case CREATE:
                createGameController.setMuteIcon();
//                createGameController.loadAvailableCourses();
                GUIHelper.setViewPort(createGameController.backgroundImageView, 0, 1080);
                primaryStage.getScene().setRoot(createGameRoot);
                GUIHelper.fadeElementPane(createGameController.elementPane, 500.0, true);
                //createGameController.makeCourseButtons();
                currentSceneName = SceneName.CREATE;
                break;
            case LOBBY:
                ClientMain.lobbyViewController.setMuteIcon();
                // Preload the images for the party...
                AbilityDrawer.loadAbilityImages();
                // Add a new key press handler to the current scene so we can capture the keypress's first
                // Gets removed when this user disconnects from the server
//                keyPressHandler.setup(primaryStage.getScene());
                // Add two classes that really need to know about key presses

                // Add the lobby view controller in, it will start the race when the right packet comes in
                clientDataStream.addObserver(lobbyViewController);
                clientDataStream.addObserver(windDirectionController);
                clientDataStream.addObserver(mainWindowController);

                // Add observers of race
                race.addObserver(raceViewController);
                race.addObserver(timerController);

                GUIHelper.setViewPort(lobbyViewController.backgroundImageView, 1920, 1080);
                primaryStage.getScene().setRoot(lobbyViewRoot);
                GUIHelper.fadeElementPane(lobbyViewController.elementPane, 1000.0, true);
                currentSceneName = SceneName.LOBBY;
                break;
        }

        primaryStage.setFullScreen(FULLSCREEN);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Store the screen dimensions
        Rectangle2D stageSize = Screen.getPrimary().getBounds();

        //Stores the stage in the class so that we can change the scenes at will.
        ClientMain.primaryStage = primaryStage;

        //primaryStage config
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("images/aoslogo.png")));
        primaryStage.setTitle("Age of Sailing");

        //InitialMenu scene setup
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("InitialMenu.fxml"));
        initialMenuRoot = fxmlLoader.load();

        // Set the scene's primary height...
        primaryScene = new Scene(initialMenuRoot, stageSize.getWidth(), stageSize.getHeight());

        //Join Game View scene setup
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("JoinGameView.fxml"));
        joinViewRoot = fxmlLoader.load();

        //Create Game  scene setup
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("CreateGameView.fxml"));
        createGameRoot = fxmlLoader.load();

        //Lobby View scene setup
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("LobbyView.fxml"));
        lobbyViewRoot = fxmlLoader.load();

        //MainWindow scene setup
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("MainWindow.fxml"));
        mainWindowRoot = fxmlLoader.load();

        // Set what happens when the user clicks the X in the corner.
        primaryStage.setOnCloseRequest(event -> {
            if(!mainWindowController.exitGame()){
                event.consume();    // If the user declines to exit, do not close the app
            } else {
                System.exit(0);
            }
        });

        // Lock out the full screen exit key combo - so the user can't escape in full screen
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        //In fair Verona, where we lay our scene,
        //From ancient grudge break to new mutiny...mutiny on board the GameX vessels!
        showSplashScreen();

        //Shows the mainstage after the splashScreen
        PauseTransition splashDelay = new PauseTransition(Duration.seconds(SPLASH_DELAY));
        splashDelay.setOnFinished(event -> showMainScreen());
        splashDelay.play();

    }

    /**
     * Shows a new stage at the beginning with transparent attributes and a PNG file
     * PNG should have an opacity layer to be used correctly
     */
    private void showSplashScreen(){
        initialStage = new Stage();
        final Rectangle2D bounds = Screen.getPrimary().getBounds();

        Image image = new Image("images/SplashLogo.png");
        ImageView imgView = new ImageView(image);

        imgView.setFitHeight(image.getHeight());
        imgView.setFitWidth(image.getWidth());

        Pane splashPane = new Pane();
        splashPane.getChildren().add(imgView);

        splashPane.setStyle("-fx-background-color: transparent;");

        Scene splashScene = new Scene(splashPane, image.getWidth(), image.getHeight(), Color.TRANSPARENT);
        initialStage.setScene(splashScene);
        initialStage.initStyle(StageStyle.TRANSPARENT);

        initialStage.setScene(splashScene);

        initialStage.setX(bounds.getMinX() + bounds.getWidth() / 2.0 - image.getWidth() / 2.0);
        initialStage.setY(bounds.getMinY() + bounds.getHeight() / 2.0 - image.getHeight() / 2.0);

        initialStage.show();
    }

    /**
     * Closes the splash screen before displaying the main screen and initiating
     * necessary keypresses and observers
     */
    private void showMainScreen(){
        initialStage.close();
        // This sizes it to the current scene... otherwise it looks horrible when you go out of full screen
        GUIHelper.setViewPort(initialMenuController.backgroundImageView, 0, 0);
        primaryStage.setScene(primaryScene);
        primaryStage.show();

        // Check if we are in Debug mode, if not normal is to have the app full screen
        if (!debug) {
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreen(true);
            primaryStage.setResizable(false);
            FULLSCREEN = true;
        } else {
            // Disable full screen mode
            FULLSCREEN = false;
        }
        //////////////// OS CHECKING //////////////////////////
        // Check for setMaximise on Mac here
        // Disable the setMax if we are - it's a bug with MacOs
        OsCheck.OSType osType = OsCheck.getOperatingSystemType();
        if (osType == OsCheck.OSType.MacOS) {
            primaryStage.setMaximized(false);
        } else {
            primaryStage.setMaximized(true);
        }

        try {
            Sound menuSound2 = new Sound(lobbyMedia, true, true);
            Sound menuSound = new Sound(lobbyThunder, false, false);


        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
        }
    }

    /**
     * Gets the playerName we are playing as
     *
     * @return String of the players name
     */
    static String getPlayerName() {
        return playerName;
    }

    /**
     * Sets the players name within the program
     *
     * @param playerName players chosen name
     */
    public static void setPlayerName(String playerName) {
        ClientMain.playerName = playerName;
    }

    public static void setAvatarType(int type) {
        avatarType = type;

    }

    public static int getAvatarType() {
        return avatarType;
    }

    /**
     * Tells the datastream to close down
     * removes the keypress handler from the scene
     */
    public static void closeDataStream() {
        if (clientDataStream != null) {
            keyPressHandler.deleteObservers();
            clientDataStream.shutdown();
            if(keyPressHandler!=null){
                keyPressHandler.remove(primaryScene);

            }
        }
    }

    public static void startClientRaceView() {
        Platform.runLater(() -> {
            raceViewController.startCanvasReDrawer();
            raceViewController.setupCourse(getRace().getCourse());
        });
    }

    /**
     * Sets this clients source ID
     *
     * @param assignedSourceID ID of the client
     */
    static void setAssignedSourceID(int assignedSourceID) {
        ClientMain.assignedSourceID = assignedSourceID;
        race.getFleet().setUserAssignId(assignedSourceID);
    }

    public static int getAssignedSourceID() {
        return assignedSourceID;
    }

    /**
     * Sets the ClientMain into "Debug" mode so that the windows will resize and we
     * can shift it around etc, if you need to use something ONLY in development mode
     * then this is the flag for you..
     * Add the keyword "debug" into intellij's prog arguments
     */
    public void setDebug() {
        debug = true;
    }

    public static Scene getPrimaryScene() {
        return primaryScene;
    }

    public static Timer getWatchDog() {
        return watchDog;
    }

    /**
     * Makes a alert box with the string passed in
     *
     * @param string string to be displayed on alert box
     */
    public static Optional<ButtonType> makeAlertBox(String title, String string) {

        Label label = new Label(string);
        label.setWrapText(true);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, null, ButtonType.OK, ButtonType.CANCEL);
//        Window currentMonitor = getPrimaryScene().getWindow();
//        alert.initModality(Modality.APPLICATION_MODAL);
//        alert.initOwner(primaryStage);
        alert.getDialogPane().setContent(label);
        alert.setHeaderText(null);
        alert.setTitle(title);

        alert.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                alert.setX(primaryStage.getX() + (primaryStage.getWidth()/2) - (newValue.doubleValue()/2.0));
            }
        });

        alert.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                alert.setY(primaryStage.getY() + (primaryStage.getHeight()/2) - (newValue.doubleValue()/2.0));
            }
        });

        return alert.showAndWait();
    }


    /**
     * Reset any and all variables used during the game that will need
     * to be put back to before.
     */
    public static void resetClient() {
        closeDataStream();  // close the data stream we had
        if (raceViewController.getAnimationTimer() != null) {
            raceViewController.getAnimationTimer().stop();
        }
        race.setFleet(new Fleet());  //remove all the boats
    }


    /**
     * Mutes all the sounds in the application
     */
    public static void muteAll() {
        muted = !muted;
        for (MediaPlayer mediaPlayer :
                mediaPlayers){
            mediaPlayer.setMute(muted);
        }
    }

    public static boolean isMuted(){
        return muted;
    }

    /**
     * Stops the track and deletes it
     * @param media media that is to be stopped and deleted.
     */
    public static void stopSound(Media media){
        for(MediaPlayer mediaPlayer: mediaPlayers){
            if(mediaPlayer.getMedia().equals(media)){
                mediaPlayer.setVolume(1.0);
                Transition fadeOut = new Transition() {
                    {
                        setCycleDuration(Duration.seconds(2));
                    }
                    @Override
                    protected void interpolate(double frac) {
                        mediaPlayer.setVolume(1 - frac);
                    }
                };
                fadeOut.setOnFinished(event -> mediaPlayer.stop());
                fadeOut.play();
            }
        }
    }
    public static void muteSounds(Button button){
        if(ClientMain.isMuted()){
            button.getStyleClass().removeAll("volume_button_Off");
            button.getStyleClass().add("volume_button_On");
        }else{
            button.getStyleClass().removeAll("volume_button_On");
            button.getStyleClass().add("volume_button_Off");
        }
    }
    public static void setSoundIcon(Button button){
        if(ClientMain.isMuted()){
            button.getStyleClass().removeAll("volume_button_On");
            button.getStyleClass().add("volume_button_Off");

        }else{
            button.getStyleClass().removeAll("volume_button_Off");
            button.getStyleClass().add("volume_button_On");
        }
    }
}