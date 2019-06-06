package seng302.Client.Controllers;

import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import seng302.Client.ClientMain;
import seng302.Client.SceneName;
import seng302.Common.GUIHelper;
import seng302.Common.Utils.Sound;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controls all aspects of the initial menu screen
 * Leads into the Join game and Create game lobbies
 */
public class InitialMenuController implements Initializable{

    @FXML
    public ImageView backgroundImageView;
    @FXML
    public ImageView keyboardLayoutImgvw;
    @FXML
    public ImageView soundIcon;
    @FXML
    public Button muteButton;
    @FXML
    private AnchorPane backgroundPane;
    @FXML
    public GridPane elements;

    private double HEIGHT = 1080;
    private double WIDTH = 1920;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientMain.initialMenuController = this;

        //Load the image here
        Image bkg = new Image(getClass().getClassLoader().getResourceAsStream("images/MenuJPG.jpg"));
        backgroundImageView.setImage(bkg);

        //"Make the image full screen" Binds H and W to it's parents
        GUIHelper.bindImageDimensionsToParent(backgroundImageView, backgroundPane);
    }

    /**
     * Exit the program using the close button from the main screen
     */
    public void exit() {
        // Exit using system code 0
        if(ClientMain.mainWindowController.exitGame()){
            System.exit(0);
        }
    }

    /**
     * Animates the viewport to the right
     */
    private void slideRight(){
        //Needs to slowly update the XOffset value from 0 -> 600
        double start = 0;
        double target = 1920;
        Transition slideRight = getTransition(start, target, true);
        slideRight.playFromStart();
    }

    /**
     * Returns the transition that will animate the background pane that is based on parameters.
     * @param start initial interpolate value
     * @param target final interporlate value
     * @param inerpolateX if true slide Right else slide down
     * @return created transition
     */
    private Transition getTransition(double start, double target, boolean inerpolateX) {
        return new Transition() {
            {
                setCycleDuration(GUIHelper.scrollDuration);
            }

            @Override
            protected void interpolate(double frac) {
                Rectangle2D rect;
                double offset = start + frac * (target - start);
                if (inerpolateX) {
                    rect = new Rectangle2D(offset, 0, WIDTH, HEIGHT);
                }else{
                    rect = new Rectangle2D(0, offset, WIDTH, HEIGHT);
                }
                backgroundImageView.setViewport(rect);
            }
        };
    }

    /**
     * Animates the viewport down
     */
    private void slideDown(){
        //Needs to slowly update the XOffset value from 0 -> 600
        double start = 0;
        double target = 1080;
        Transition slideDown = getTransition(start, target, false);
        slideDown.playFromStart();
    }

    /**
     * Animates the background appropriately then changes scene
     */
    public void lobby(){
        GUIHelper.fadeElementPane(elements, 500.0, false);
        slideRight();
        PauseTransition p = new PauseTransition(GUIHelper.scrollDuration);
        p.setOnFinished(event -> ClientMain.changeScene(SceneName.JOIN));
        p.play();
    }

    /**
     * Animates the background appropriately then changes scene
     * Checks to see if a client is hosting a server before starting another
     */
    public void host() {
        GUIHelper.fadeElementPane(elements, 500.0, false);
        slideDown();
        PauseTransition p = new PauseTransition(GUIHelper.scrollDuration);
        p.setOnFinished(event -> ClientMain.changeScene(SceneName.CREATE));
        p.play();
    }

    /**
     * Shows the pane with the keyboard layout on it and also sets up a listener
     * to "close" the pane (Make it not visible) when clicked.
     */
    public void showHelpMenu() {
        Image img = new Image(getClass().getClassLoader().getResourceAsStream("images/controllerlayout.png"));
        keyboardLayoutImgvw.setImage(img);
        keyboardLayoutImgvw.fitHeightProperty().bind(backgroundPane.heightProperty());
        keyboardLayoutImgvw.fitWidthProperty().bind(backgroundPane.widthProperty());
        keyboardLayoutImgvw.setPreserveRatio(false);
        keyboardLayoutImgvw.setVisible(true);

        EventHandler<MouseEvent> helpClick = event -> keyboardLayoutImgvw.setVisible(false);
        keyboardLayoutImgvw.setOnMouseClicked(helpClick);
    }

    /**
     * Changes the image on the toggle button for muting sounds
     * and calls mute all to handle the muting of sounds.
     */
    @FXML
    public void muteSounds(){
        ClientMain.muteSounds(muteButton);
        ClientMain.muteAll();
    }
    public void setMuteIcon(){
        ClientMain.setSoundIcon(muteButton);

    }

}
