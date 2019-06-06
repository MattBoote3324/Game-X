package seng302.Client.Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import seng302.Client.ClientMain;
import seng302.Client.SceneName;
import seng302.Common.Fleet;
import seng302.Common.GUIHelper;
import seng302.Common.Messages.RaceStatusMessage;
import seng302.Common.RaceStatus;
import seng302.Server.ServerDataStream;
import seng302.Server.ServerMain;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;

import static seng302.Client.ClientMain.getPrimaryScene;
import static seng302.Client.ClientMain.getRace;
import static seng302.Client.ClientMain.mainWindowController;

/**
 * Controller for all lobby functions.
 */
public class LobbyViewController implements Initializable, Observer {

    @FXML
    public AnchorPane backgroundPane;

    @FXML
    public Pane fadeAnimationPane ;
    public Button btnLeaveLobby;
    @FXML
    public Button soundButton;

    @FXML
    private TableView lobbyTable;

    @FXML
    private TableColumn nameCol;

    @FXML
    public TableColumn imageCol;

    @FXML
    public ImageView backgroundImageView;

    @FXML
    public GridPane elementPane;
    private int TIME_TILL_START = 15000;


    @FXML
    private ImageView leftImageView;

    @FXML
    private ImageView rightImageView;

    @FXML
    private Button btnStart;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientMain.lobbyViewController = this;

        //Load the image here
        Image bkg = new Image(getClass().getClassLoader().getResourceAsStream("images/MenuJPG.jpg"));
        backgroundImageView.setImage(bkg);

        initiateColumnImages();
        imageCol.minWidthProperty().bind(lobbyTable.widthProperty().multiply(0.25));
        imageCol.maxWidthProperty().bind(imageCol.minWidthProperty());
        nameCol.minWidthProperty().bind(lobbyTable.widthProperty().multiply(0.65));
        nameCol.maxWidthProperty().bind(nameCol.minWidthProperty());
        leftImageView.fitHeightProperty().bind(lobbyTable.heightProperty());
        rightImageView.fitHeightProperty().bind(lobbyTable.heightProperty());

        //"Make the image full screen" Binds H and W to it's parents
        GUIHelper.bindImageDimensionsToParent(backgroundImageView, backgroundPane);
    }

    private void initiateColumnImages() {

        //Images are 251 wide
        Image left = new Image(getClass().getClassLoader().getResourceAsStream("images/leftColumn.png"));
        Image right = new Image(getClass().getClassLoader().getResourceAsStream("images/rightColumn.png"));
        //leftImageView.setPreserveRatio(false);
        //rightImageView.setPreserveRatio(false);
        leftImageView.setImage(left);
        rightImageView.setImage(right);

        //leftImageView.setLayoutY(backgroundPane.getHeight()+leftImageView.getFitHeight());

//

    }

    /**
     * Creates a button for the game to be started with after
     * the server has been created.
     */
    public void setStartButtonVisibility(boolean visible) {
        btnStart.setVisible(visible);
    }

    @FXML
    private void start(){
        mainWindowController.hideInitialMenu();
        if (ClientMain.debug) {
            TIME_TILL_START = 2000; // Set up a test start time
        }
        ServerMain.startRace(TIME_TILL_START);
    }

    /**
     * Exit the lobby and disconnect.
     * If client, close all server connection.
     * If host, close all client connections.
     */
    public void exit(){
        mainWindowController.exitGame();
    }

    /**
     * sets the table in lobby and sets it to observe boats in fleet to update when a new boat is added to fleet
     * @param fleet the fleet of racing boats.
     */
    public void setTable(Fleet fleet) {
        Platform.runLater(() -> {
            //idCol.setCellValueFactory(new PropertyValueFactory<>("sourceID"));
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            imageCol.setCellValueFactory(new PropertyValueFactory<>("godImage"));
            lobbyTable.setItems(FXCollections.observableArrayList(fleet.getBoats()));
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof RaceStatusMessage) {
            if (getRace().getRaceState().compareTo(RaceStatus.PREPARATORY) <= 0) {  //  in inactive, warning or preparatory stages
                ClientMain.timerController.startRaceTimer();
                // Delete the observer from the clientmain datastream... its now no longer needed
                ClientMain.getDataStream().deleteObserver(this);
                Platform.runLater(() -> {
                    ClientMain.mainWindowController.hideInitialMenu();
                    ClientMain.mainWindowController.setMuteIcon();
                    ClientMain.changeScene(SceneName.MAIN);
                    ClientMain.startClientRaceView();
                });
            }
        }
    }

    public void muteSounds(ActionEvent actionEvent) {
        ClientMain.muteSounds(soundButton);
        ClientMain.muteAll();
    }
    public void setMuteIcon(){
        ClientMain.setSoundIcon(soundButton);

    }
}
