package seng302.Client.Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Window;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import seng302.Client.ClientMain;
import seng302.Client.GodData;
import seng302.Client.SceneName;
import seng302.Common.GUIHelper;
import seng302.Common.Messages.RaceStatusMessage;
import seng302.Common.RaceStatus;
import seng302.SelectionWheel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.function.UnaryOperator;

import static seng302.Client.ClientMain.getPrimaryScene;
import static seng302.Client.ClientMain.getRace;

public class JoinGameController implements Initializable, Observer {

    @FXML
    public Button buttonJoinServer;
    @FXML
    public Label errorConnectLabel;
    @FXML
    public TextField txtIpAddress;

    @FXML
    public TextField txtUserName;
    @FXML
    public ImageView backgroundImageView;

    @FXML
    public AnchorPane mainPane;
    @FXML
    public Pane wheelPane;
    @FXML
    public VBox elements;
    @FXML
    public Label godChosenTitle;
    @FXML
    public Label godChosenDesc;
    public Button soundButton;


    private SelectionWheel characterSelector;
    public ArrayList<GodData> godDataArray;
    private List<ImageView> characterImages;
    private final Tooltip errorTip = new Tooltip();
    private String previousIpAddress;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientMain.joinGameController = this;

        //Load the image here
        Image bkg = new Image(getClass().getClassLoader().getResourceAsStream("images/MenuJPG.jpg"));
        backgroundImageView.setViewport(new Rectangle2D(1920, 0, 1920, 1080));
        backgroundImageView.setImage(bkg);

        GUIHelper.bindImageDimensionsToParent(backgroundImageView, mainPane);

        int maxLength = 30;

        // This stops anyone putting more than 30 Characters into the text box and
        // Overflowing the packet size for sending a players name (limited to 30 bytes)
        UnaryOperator<TextFormatter.Change> rejectChange = c -> {
            // check if the change might effect the validating predicate
            if (c.isContentChange()) {
                // check if change is valid
                if (c.getControlNewText().length() > maxLength) {
                    // invalid change
                    // sugar: show a context menu with error message
                    final ContextMenu menu = new ContextMenu();
                    // Displays a warning to the user that they have exceeded the max length.
                    menu.getItems().add(new MenuItem("This field takes\n" + maxLength + " characters only."));
                    menu.show(c.getControl(), Side.BOTTOM, 0, 0);
                    // return null to reject the change
                    return null;
                }
            }
            // valid change: accept the change by returning it
            return c;
        };

        txtUserName.setTextFormatter(new TextFormatter<>(rejectChange));

        ChangeListener wheelSizeListener = (observable, oldValue, newValue) -> {
            wheelPane.getChildren().clear();
            makeButtonChoices();
		};
        ClientMain.getPrimaryScene().widthProperty().addListener(wheelSizeListener);
        ClientMain.getPrimaryScene().heightProperty().addListener(wheelSizeListener);
    }

    /**
     * This joins the player to the server, It
     * - sets the player name,
     * - sets the details to connect to server (IP Address)
     * - Changes scene to the lobby screen
     */
    public void joinServer() {
        // set the username on the server
        String playerName = txtUserName.getText();
        if (playerName.isEmpty()) {
            playerName = txtUserName.getPromptText();
        }
        ClientMain.setPlayerName(playerName);
        // Set the avatar type
        // TODO: Replace 10 with the type selected from the menu

        //TODO - Move the set avatar to the lobby
        if (characterSelector.getSelected() == -1) {
            Random r = new Random();
            ClientMain.setAvatarType(r.nextInt(4));
        } else {
            ClientMain.setAvatarType(characterSelector.getSelected());
        }

        try {
            errorConnectLabel.setVisible(false);
            String host = txtIpAddress.getText();
            if (host == null || host.isEmpty()) {
                host = txtIpAddress.getPromptText();
            }
            String[] splitAddress = host.split(":");    // if port number not supplied
            if (splitAddress.length < 2) {
                showErrorTip("Port number not supplied");
                return;
            }
            String ipAddress = host.split(":")[0];
            String port = host.split(":")[1];
            ClientMain.connectTo(ipAddress, Integer.parseInt(port));
            GUIHelper.fadeElementPane(elements, 500.0, false);
            slideDown();
            PauseTransition p = new PauseTransition(GUIHelper.scrollDuration);
            p.setOnFinished(event -> ClientMain.changeScene(SceneName.LOBBY));
            p.play();
        } catch (IOException e) {
            showErrorTip("Cannot connect to the supplied IP!");
        }
    }

    private void showErrorTip(String suppliedString) {
        Point2D p = txtIpAddress.localToScene(txtIpAddress.getLayoutBounds().getMinX(), txtIpAddress.getLayoutBounds().getMaxY());
        Image image = new Image(getClass().getClassLoader().getResourceAsStream("./images/Warn.png"),
                30.0, 30.0, true, true);
        errorTip.setGraphic(new ImageView(image));
        errorTip.setText(suppliedString);
        errorTip.setFont(new Font(24));
        errorTip.show(ClientMain.getStage(), p.getX() + txtIpAddress.getScene().getX() +
                txtIpAddress.getScene().getWindow().getX(), p.getY() + txtIpAddress.getScene().getY() +
                txtIpAddress.getScene().getWindow().getY() + 10);
        txtIpAddress.setTooltip(errorTip);

        buttonJoinServer.setOnMouseExited(event -> errorTip.setAutoHide(true));
    }

    public void exit() {
        ClientMain.mainWindowController.exitGame();
    }


    private void slideLeft() {
        double start = 0;
        double target = 1920;
        Transition slideLeft = new Transition() {
            {
                setCycleDuration(GUIHelper.scrollDuration);
            }

            @Override
            protected void interpolate(double frac) {
                double offset = start + frac * (target - start);
                Rectangle2D rect = new Rectangle2D(target - offset, 0, 1920, 1080);
                backgroundImageView.setViewport(rect);
            }
        };
        slideLeft.playFromStart();
    }

    private void slideDown() {
        double start = 0;
        double target = 1080;
        Transition slideLeft = new Transition() {
            {
                setCycleDuration(GUIHelper.scrollDuration);
            }

            @Override
            protected void interpolate(double frac) {
                double offset = start + frac * (target - start);
                Rectangle2D rect = new Rectangle2D(1920, offset, 1920, 1080);
                backgroundImageView.setViewport(rect);
            }
        };
        slideLeft.playFromStart();
    }

    /**
     * Make a circular array of buttons to chose the gods from
     * Images are loaded from characters.manifest
     * Hard coded to 8 at the moment.
     */
    private void makeButtonChoices() {

        JSONParser parser = new JSONParser();

        Object obj = null;

        // Make sure we can actually load the manifest files... if not - let the user know about it
        try {
            obj = parser.parse(new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/characters.manifest"))));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            Window currentMonitor = getPrimaryScene().getWindow();
//            alert.initOwner(currentMonitor);

            alert.setTitle("Cannot find courses");
            alert.setContentText("Where the hell are the Gods?!?! Do they exist?");
            alert.showAndWait();
            e.printStackTrace();
        }

        // Holds all the characters we need to display... LIMIT OF 8 SO FAR
        // This is because there 360 deg / 8 buttons (45 deg apart)
        JSONArray characters = (JSONArray) ((JSONObject) obj).get("characters");
        // Incase we don't have 8 characters.. lets just load a default unknown character that can't be used.
        JSONArray unknownCharacter = (JSONArray) ((JSONObject) obj).get("default");

        // Create a list of images from the json manifest
        List<ImageView> imageList = loadGodData(characters, unknownCharacter);

        characterSelector = new SelectionWheel(wheelPane, ((ClientMain.getPrimaryScene().getWidth() - 30) * 0.5), ClientMain.getPrimaryScene().getHeight() - 327, 5, imageList);
        characterSelector.setController(this);
    }

    /**
     * Reads a Json array for the image location tag, returns a List of ImageViews
     *
     * @param characters       Character array
     * @param unknownCharacter Default character if not enough found in the Json File
     * @return List<ImageView>
     */
    private List<ImageView> loadGodData(JSONArray characters, JSONArray unknownCharacter) {
        characterImages = new ArrayList<>();
        godDataArray = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            JSONObject character;

            // We don't have enough characters in our array
            if (i >= characters.size()) {
                // So load the "Default" unknown Character
                character = ((JSONObject) unknownCharacter.get(0));
            } else {
                character = ((JSONObject) characters.get(i));
            }

            String charImageLocation = character.get("image_location").toString();
            String godName = character.get("name").toString();
            String godDesc = character.get("description").toString();

            godDataArray.add(new GodData(godName, godDesc, charImageLocation));
            ImageView image = new ImageView(new Image(getClass().getResourceAsStream(charImageLocation)));
            characterImages.add(image);
        }
        return characterImages;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof RaceStatusMessage) {
            if (getRace().getRaceState().compareTo(RaceStatus.PREPARATORY) <= 0) {    //  in inactive, warning or preparatory stages
                ClientMain.timerController.startRaceTimer();
                ClientMain.getDataStream().deleteObserver(this);
                Platform.runLater(() -> {
                    ClientMain.mainWindowController.hideInitialMenu();
                    ClientMain.changeScene(SceneName.MAIN);
                    ClientMain.raceViewController.setupCourse(getRace().getCourse());
                    //ClientMain.raceViewController.startCanvasReDrawer();
                });
            }
        }
    }

    public void setIpAddressEnabled(boolean enable) {
        txtIpAddress.setDisable(!enable);
    }

    public void updateTextDetails(int newSelection) {
        godChosenTitle.setText(godDataArray.get(newSelection).getName());
        godChosenDesc.setText(godDataArray.get(newSelection).getDesc());
    }

    public String getGodImage(int index) {
        return godDataArray.get(index).getImage_location();

    }

    public void muteSounds(ActionEvent actionEvent) {
       ClientMain.muteSounds(soundButton);
        ClientMain.muteAll();
    }

    public void setMuteIcon(){
        ClientMain.setSoundIcon(soundButton);

    }

    public void setTxtIpAddress(int port) {
        this.txtIpAddress.setText("127.0.0.1:"+port);
    }
}
