package seng302.Client.Controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXButton.ButtonType;
import com.jfoenix.controls.JFXNodesList;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;
import seng302.Client.ClientMain;
import seng302.Client.SceneName;
import seng302.Common.*;
import seng302.Common.Messages.RaceStatusMessage;
import seng302.Common.Utils.Calculator;

import java.net.URL;
import java.util.*;

import static seng302.Client.ClientMain.getPrimaryScene;
import static seng302.Client.ClientMain.getRace;

/**
 *
 */
public class MainWindowController implements Initializable, Observer {

    private boolean leaderboardShown;
    private boolean helpShown;
    private final int Q_COOLDOWN_DURATION = 5000;
    private final int W_COOLDOWN_DURATION = 5000;
    @FXML
    private Pane helpViewPane;

    @FXML
    public ImageView soundIcon;
    private Image soundOn = new Image("images/speakerOn.png");
    private Image soundOff = new Image("images/speakerOff.png");
    @FXML
    private AnchorPane finishingPane;
    @FXML
    private GridPane mainBorder;
    @FXML
    private AnchorPane raceViewAnchor;
    @FXML
    private GridPane leaderboard;
    @FXML
    private Pane initialMenuPane;
    @FXML
    private LeaderboardController leaderboardController;
    @FXML
    private ImageView keyboardLayout;
    @FXML
    private JFXNodesList options;
    @FXML
    private ImageView fadeImageView;
    @FXML
    private Pane backgroundColourPane;
    @FXML
    private ImageView backgroundImageView;

    @FXML
    private Label placingText;

    @FXML
    private Label suffixText;

    @FXML
    private ImageView q_img;
    @FXML
    private ImageView w_img;

    private Course course;
    private int listenCount;
    private StackPane finishPane;
    private ObservableList<Boat> finishedBoats;

    private DropShadow ds = new DropShadow(BlurType.GAUSSIAN, Color.LIGHTYELLOW, 10, 0.8, 0, 0);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientMain.mainWindowController = this;
        leaderboardController.setParentController(this);
        //Only sets up the initial menu after the main menu has been created, so sizes are available
        initAnnotatePopup();
        initImageView();
        //initGodCoolDownImage();
        q_img.setEffect(ds);
        w_img.setEffect(ds);
    }

    /**
     *
     */
    private void initGodCoolDownImage() {

        ImageView q_img = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/q_icon.png")));

        ImageView w_img = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/w_icon.png")));



        q_img.setFitHeight(100);
        q_img.setFitWidth(100);

        w_img.setFitHeight(100);
        w_img.setFitWidth(100);
        AnchorPane.setBottomAnchor(w_img, 50.0);
        AnchorPane.setLeftAnchor(w_img, 100.0);
        raceViewAnchor.getChildren().add(q_img);
        raceViewAnchor.getChildren().add(w_img);

    }

    /**
     * Adds the main background image to the scene so that a fade is possible and
     * Binds the Imageview to its parent pane with the correct dimensions.
     * Does not preserve aspect ratio so may become skewed at inconsistent resolutions
     */
    private void initImageView(){
        Image img = new Image(getClass().getClassLoader().getResourceAsStream("images/MenuJPG.jpg"));
        fadeImageView.setImage(img);
        GUIHelper.bindImageDimensionsToParent(fadeImageView, mainBorder);
//        fadeImageView.fitHeightProperty().bind(backgroundColourPane.heightProperty());
//        fadeImageView.fitWidthProperty().bind(backgroundColourPane.widthProperty());
        GUIHelper.setViewPort(fadeImageView, 1920, 1080);
        fadeImageView.setPreserveRatio(false);
    }

    /**
     * Creates a popup box for the annotations to be adjusted...
     */
    private void initAnnotatePopup() {

        // Base Button
        JFXButton annotationShow = new JFXButton();

        annotationShow.setButtonType(ButtonType.RAISED);
        annotationShow.getStyleClass().add("button-icon-annotation");
        annotationShow.setText("Aa");
        annotationShow.setFocusTraversable(false);
        annotationShow.setTooltip(new Tooltip("Show an annotation"));

        // Show all Annotations
        JFXButton allAnnotations = new JFXButton("Show");
        allAnnotations.setButtonType(ButtonType.RAISED);
        allAnnotations.getStyleClass().addAll("animated-option-button");
        allAnnotations.setTooltip(new Tooltip("Show all annotations"));

        JFXButton showBoatSpeed = GUIHelper.buttonMaker("Speed");
        JFXButton showBoatTracks = GUIHelper.buttonMaker("Tracks");
        JFXButton showBoatNames = GUIHelper.buttonMaker("Show Boat Names");
        JFXButton showTimeSinceMark = GUIHelper.buttonMaker("Time Since Last Mark");
        JFXButton showFrameRate = GUIHelper.buttonMaker("Frame Rate");
        JFXButton showVmgVectors = GUIHelper.buttonMaker("VMG SOG Vectors");
        JFXButton showGodIcons = GUIHelper.buttonMaker("God Icons");
        GUIHelper.setJFXButtonPseudoClass(showGodIcons, "checked", true);

        allAnnotations.setOnMouseClicked(e -> {
            if(allAnnotations.getText().equals("Hide"))  {
                Annotations.setNone();
                GUIHelper.setJFXButtonPseudoClass(showBoatSpeed, "checked", false);
                GUIHelper.setJFXButtonPseudoClass(showBoatTracks, "checked", false);
                GUIHelper.setJFXButtonPseudoClass(showBoatNames, "checked", false);
                GUIHelper.setJFXButtonPseudoClass(showTimeSinceMark, "checked", false);
                GUIHelper.setJFXButtonPseudoClass(showFrameRate, "checked", false);
                GUIHelper.setJFXButtonPseudoClass(showVmgVectors, "checked", false);
                GUIHelper.setJFXButtonPseudoClass(showGodIcons, "checked", false);

                allAnnotations.setText("Show");
                allAnnotations.getStyleClass().removeAll("animated-option-button-toggled");
                allAnnotations.getStyleClass().addAll("animated-option-button");
                allAnnotations.setTooltip(new Tooltip("Show all annotations"));

            }  else {
                Annotations.setAll();
                GUIHelper.setJFXButtonPseudoClass(showBoatSpeed, "checked", true);
                GUIHelper.setJFXButtonPseudoClass(showBoatTracks, "checked", true);
                GUIHelper.setJFXButtonPseudoClass(showBoatNames, "checked", true);
                GUIHelper.setJFXButtonPseudoClass(showTimeSinceMark, "checked", true);
                GUIHelper.setJFXButtonPseudoClass(showFrameRate, "checked", true);
                GUIHelper.setJFXButtonPseudoClass(showVmgVectors, "checked", true);
                GUIHelper.setJFXButtonPseudoClass(showGodIcons, "checked", true);

                allAnnotations.setText("Hide");
                allAnnotations.getStyleClass().removeAll("animated-option-button");
                allAnnotations.getStyleClass().addAll("animated-option-button-toggled");
                allAnnotations.setTooltip(new Tooltip("Hide all annotations"));
            }

        });

        // Custom list of annotations
        JFXButton customAnnotations = new JFXButton("Custom");
        customAnnotations.setButtonType(ButtonType.RAISED);
        customAnnotations.getStyleClass().addAll("animated-option-button");
        customAnnotations.setTooltip(new Tooltip("Show custom annotations"));


        showBoatSpeed.setOnMouseClicked(e -> {
            updateCustomAnnotations(AnnotationType.SPEED);
            GUIHelper.toggleJFXButtonPseudoClass(showBoatSpeed, "checked");

        });


        showBoatTracks.setOnMouseClicked(e -> {
            updateCustomAnnotations(AnnotationType.SHOW_TRACKS);
            GUIHelper.toggleJFXButtonPseudoClass(showBoatTracks, "checked");
        });


        showBoatNames.setOnMouseClicked(e -> {
            updateCustomAnnotations(AnnotationType.SHOW_NAMES);
            GUIHelper.toggleJFXButtonPseudoClass(showBoatNames, "checked");
         });


        showTimeSinceMark.setOnMouseClicked(e -> {
            updateCustomAnnotations(AnnotationType.TIME_SINCE_MARK);
            GUIHelper.toggleJFXButtonPseudoClass(showTimeSinceMark, "checked");
        });


        showFrameRate.setOnMouseClicked(e -> {
            updateCustomAnnotations(AnnotationType.FRAME_RATE);
            GUIHelper.toggleJFXButtonPseudoClass(showFrameRate, "checked");
        });


        showVmgVectors.setOnMouseClicked(e -> {
            updateCustomAnnotations(AnnotationType.VMG_SOG);
            GUIHelper.toggleJFXButtonPseudoClass(showVmgVectors, "checked" );
        });

        showGodIcons.setOnMouseClicked(e -> {
            updateCustomAnnotations(AnnotationType.GOD);
            GUIHelper.toggleJFXButtonPseudoClass(showGodIcons, "checked" );
        });

        // Add the custom node list
        JFXNodesList customList = new JFXNodesList();
        customList.setSpacing(10);
        customList.addAnimatedNode(customAnnotations);
        customList.addAnimatedNode(showBoatNames);
        customList.addAnimatedNode(showBoatSpeed);
        customList.addAnimatedNode(showBoatTracks);
        customList.addAnimatedNode(showTimeSinceMark);
        customList.addAnimatedNode(showFrameRate);
        customList.addAnimatedNode(showVmgVectors);
        customList.addAnimatedNode(showGodIcons);
        customList.setRotate(180);

        // Add all the nodes to the main list
        options.setSpacing(10);
        options.addAnimatedNode(annotationShow); // base button
        options.addAnimatedNode(allAnnotations); // All on
        //options.addAnimatedNode(hideAllAnnotations); // All off
        options.addAnimatedNode(customList); // Our custom list
        options.setRotate(180);
        options.setAlignment(Pos.BOTTOM_RIGHT);

    }

    /**
     * method that decides which annotation should be toggled.
     * @param type Annotation type that will be toggled.
     */
    private void updateCustomAnnotations(AnnotationType type) {
        switch (type) {
            case SPEED:
                Annotations.toggleSpeed();
                break;

            case SHOW_TRACKS:
                Annotations.toggleTracks();
                break;

            case SHOW_NAMES:
                Annotations.toggleNames();
                break;

            case TIME_SINCE_MARK:
                Annotations.toggleTimeSinceMark();
                break;

            case FRAME_RATE:
                Annotations.toggleFPS();
                break;

            case VMG_SOG:
                Annotations.toggleVMG_SOG();
                break;
            case GOD:
                Annotations.toggleGodIcon();
        }
    }

    /**
     * No longer initiates listeners as they are broken and resize the canvas infinitely
     */
    //Todo - Re enable resizing
    public void initRaceViewAnchorListeners() {
    }

    public AnchorPane getRaceViewAnchor() {
        return raceViewAnchor;
    }

    public GridPane getMainBorder(){
        return mainBorder;
    }

    void hideInitialMenu(){
        FadeTransition ft = new FadeTransition(Duration.millis(3000), fadeImageView);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.play();
        ft.setOnFinished(event -> fadeImageView.setVisible(false));
    }


    /**
     * Function to toggle showing the leaderboard
     */
    public void showLeaderboard() {
        leaderboardController.setDimensionOffsets(leaderboard.getWidth(), 0);

        if (!leaderboardShown) {
            leaderboardController.showLeaderboard(true);
            leaderboard.setVisible(true);
            leaderboard.toFront();
        } else {
            leaderboardController.showLeaderboard(false);
        }
        leaderboardShown = !leaderboardShown;
    }

    /**
     * shows or hides the help screen after the button is pressed.
     */
    public void helpButtonPressed() {
        keyboardLayout.setFitHeight(this.getRaceViewAnchor().getHeight());
        keyboardLayout.setFitWidth(this.getRaceViewAnchor().getWidth());
        if(helpShown){
            helpShown = false;
            helpViewPane.setVisible(false);
        }else{
            helpShown = true;
            helpViewPane.setVisible(true);
        }
    }

    /**
     * The Quit key has been pressed and user wants to leave game
     */
    public void leaveGame() {
        exitGame();
    }

    Pane getBackgroundColourPane() {
        return backgroundColourPane;
    }

    /**
     * This method is called when the user clicks their q button while their q god power is available. It will create a
     * animation on the q button at the bottom. It will grey it out and then adda  white border when it is ready.
     */
    public void animateQButtonCoolDown() {
        new AnimationTimer() {
            double stopTime = System.currentTimeMillis() + Q_COOLDOWN_DURATION;
            @Override
            public void handle(long now) {

                if(System.currentTimeMillis() >= stopTime) {
                    ClientMain.setQCoolDown(false);
                    q_img.setEffect(ds);
                    stop();
                }else{
                    double time = stopTime - System.currentTimeMillis();
                    ColorAdjust desaturate  = new ColorAdjust();
                    desaturate.setSaturation(-(time / Q_COOLDOWN_DURATION));
                    q_img.setEffect(desaturate);
                }
            }
        }.start();
    }
    /**
     * This method is called when the user clicks their W button while their q god power is available. It will create a
     * animation on the W button at the bottom. It will grey it out and then adda  white border when it is ready.
     */
    public void animateWButtonCoolDown(double cooldown) {

        new AnimationTimer() {
            double stopTime = System.currentTimeMillis() + cooldown;
            @Override
            public void handle(long now) {

                if(System.currentTimeMillis() >= stopTime) {
                    ClientMain.setWCoolDown(false);
                    w_img.setEffect(ds);
                    stop();
                }else{
                    double time = stopTime - System.currentTimeMillis();
                    ColorAdjust desaturate  = new ColorAdjust();
                    desaturate.setSaturation(-(time / cooldown));
                    w_img.setEffect(desaturate);
                }
            }
        }.start();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof RaceStatusMessage) {
            // Shorten the call to fleet
            Fleet fleet = ClientMain.getRace().getFleet();
            // convert message
            RaceStatusMessage rs = (RaceStatusMessage) arg;
            // get status of our boat
            Boat.BoatStatus bs = rs.getBoatStatuses().get(ClientMain.getAssignedSourceID()).getBoatStatus();
            // Get the race states of ALL the boats
            Map<Integer, BoatStatusWrapper> boatStates = rs.getBoatStatuses();

            // If the boat has finished, and we don't have a finishing table up
            if (bs == Boat.BoatStatus.FINISHED && finishPane == null) {

                // our boat is finished, so splash up the dandy finishers table
                finishPane = constructNewFinishersPane();

                // Add ourselves to the finishing list
                finishedBoats.add(ClientMain.getRace().getFleet().getUserAssignBoat());

                // Add the finishing pane onto the raceviewAnchor... SOOOO PRETTTTY!
                Platform.runLater(() -> {
                    finishingPane.getChildren().clear();
                    finishingPane.getChildren().add(finishPane);
                    finishingPane.setVisible(true);
                    AnchorPane.setBottomAnchor(finishPane, 50.0);
                    AnchorPane.setTopAnchor(finishPane, 50.0);
                    AnchorPane.setLeftAnchor(finishPane, 100.0);
                    AnchorPane.setRightAnchor(finishPane, 100.0);
                });
            }

            // Iterate over all the boats
            for (Map.Entry<Integer, BoatStatusWrapper> b : boatStates.entrySet()) {
                // if the boat in the list of statuses is finished, and not our own boat id
                String placing = getRace().getFleet().getBoat(b.getKey()).getPlacing();
                if (!placing.equals("-") && b.getValue().getBoatStatus() == Boat.BoatStatus.FINISHED && b.getKey() != fleet.getUserAssignId()) {
                    // Check if we have a finished boat list - if not, create it
                    if (finishedBoats == null) {
                        finishedBoats = FXCollections.observableArrayList();
                    }
                    // Check if the finished boat list contains the boat that is now finished?
                    if (!finishedBoats.contains(fleet.getBoat(b.getKey()))) {
                        finishedBoats.add(fleet.getBoat(b.getKey()));
                    }
                }
            }
        }
    }

    /**
     * Constructs a new finishers table to be displayed at the end of a race
     *
     * @return finishers StackPane
     */
    private StackPane constructNewFinishersPane() {
        if (finishedBoats == null) {
            finishedBoats = FXCollections.observableArrayList();
        }
        StackPane pane = new StackPane();
        // Hbox contains the table and headings and buttons
        VBox vboxMain = new VBox();

        // New Label for the heading
        Label heading = new Label("Final Placings");
        heading.setFont(new Font(34));
        vboxMain.alignmentProperty().set(Pos.CENTER);

        // construct the table
        TableView<Boat> ftv = new TableView<>();
        ftv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Boat, String> name = new TableColumn<>("Name");
        TableColumn<Boat, String> place = new TableColumn<>("Placing");
        TableColumn<Boat, String> god = new TableColumn<>("God");
        ftv.getColumns().add(place);
        ftv.getColumns().add(name);
        ftv.getColumns().add(god);

        name.setCellValueFactory(new PropertyValueFactory<>("Name"));
        place.setCellValueFactory(new PropertyValueFactory<>("Placing"));
        god.setCellValueFactory(new PropertyValueFactory<>("godImage"));
        ftv.setStyle("-fx-font-size: 24px");

        // Construct the quit button
        Button btnQuit = new Button("Quit to Main Menu");
        btnQuit.setOnAction(e -> {
            exitGame();
        });

        // Adding all the elements to hbox
        vboxMain.getChildren().add(heading);
        vboxMain.getChildren().add(ftv);
        vboxMain.getChildren().add(btnQuit);
        vboxMain.setSpacing(30);
        pane.getChildren().add(vboxMain);
        ftv.setItems(finishedBoats);

        return pane;
    }

    /**
     * Exit the game, cleaning up the client resources
     * @return A boolean for whether the user pressed confirm for exiting
     */
    public boolean exitGame() {
        Optional<javafx.scene.control.ButtonType> result;

        if (ClientMain.clientServer != null) {
            result = ClientMain.makeAlertBox("Abandon?!","Abandoning now will anger the Gods! Continue?!");
        } else {
            result = ClientMain.makeAlertBox("Abandon?!","Do you really want to abandon the battle?!");
        }

        if (result.get() == javafx.scene.control.ButtonType.OK) {
            if (ClientMain.clientServer != null) {
                ClientMain.clientServer.shutdownServer();
                ClientMain.clientServer = null;
            }
            if (ClientMain.getDataStream() != null){
                ClientMain.getDataStream().deleteObserver(this);

            }

            ClientMain.resetClient();
            ClientMain.mainWindowController.hideInitialMenu();
            ClientMain.changeScene(SceneName.INITIAL);
            clearFinishPane();
            return true;
        } else {
            return false;
        }

    }

    public void clearFinishPane() {
        finishPane = null;
        finishedBoats = null; // clear the list incase someone restarts the race
        finishingPane.setVisible(false);
    }

    /**
     * Displays a boat's placing in the race on the screen
     * @param placing the boats placing to display on screen
     */
    void updatePlacingText(String placing){
        if(ClientMain.getRace().getRaceState() == RaceStatus.STARTED){
            Platform.runLater(() -> {
                placingText.setText(placing);
                if (!placing.equals("-")) {
                    suffixText.setText(Calculator.ordinal(Integer.parseInt(placing)));
                }
            });
        } else if (ClientMain.getRace().getRaceState() == RaceStatus.PREPARATORY){
            Platform.runLater(() -> {
                placingText.setText("-");
                suffixText.setText("");
            });
        }
    }


    public void muteSounds(ActionEvent actionEvent) {
        if(ClientMain.isMuted()){
            soundIcon.setImage(soundOn);

        }else{
            soundIcon.setImage(soundOff);

        }
        ClientMain.muteAll();
    }
    public void setMuteIcon(){
        if(ClientMain.isMuted()){
            soundIcon.setImage(soundOff);

        }else{
            soundIcon.setImage(soundOn);

        }
    }
    public LeaderboardController getLeaderboardController() {
        return leaderboardController;
    }
}
