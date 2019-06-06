package seng302.Client.Controllers;

import javafx.animation.PauseTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Window;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import seng302.Client.ClientMain;
import seng302.Client.SceneName;
import seng302.Common.Course;
import seng302.Common.GUIHelper;
import seng302.Common.XMLParser;
import seng302.Server.ServerMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static seng302.Client.ClientMain.getPrimaryScene;

/**
 * Created by mbo57 on 3/08/17
 */
public class CreateGameController implements Initializable {
    @FXML
    public Button soundButton;
    @FXML
    private HBox hBoxCourseList;

    @FXML
    public GridPane elementPane;

    @FXML
    public ImageView backgroundImageView;

    @FXML
    public Button btnServerStart;

    @FXML
    private GridPane backgroundPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientMain.createGameController = this;

        //Load the image here
        Image bkg = new Image(getClass().getClassLoader().getResourceAsStream("images/MenuJPG.jpg"));
//        backgroundImageView.setViewport(new Rectangle2D(0, 0, WIDTH, HEIGHT));
        backgroundImageView.setImage(bkg);

        //"Make the image full screen" Binds H and W to it's parents
        GUIHelper.bindImageDimensionsToParent(backgroundImageView, backgroundPane);

        hBoxCourseList.widthProperty().addListener((observable, oldValue, newValue) -> makeCourseButtons());
        hBoxCourseList.heightProperty().addListener((observable, oldValue, newValue) -> makeCourseButtons());
    }

    /**
     * Reads the courses.manifest file for the available courses
     * course manifest is a JSON file containing
     *   - Course name
     *   - Course preview image
     *   - Course XML File name
     */
    private void makeCourseButtons() {
        // Remove any previous children.
        hBoxCourseList.getChildren().clear();

        JSONParser parser = new JSONParser();
        Object courseManifest = new Object();

        // Load the course manifest.
        try {
            courseManifest = parser.parse(
                new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/courses.manifest"))
                )
            );
        } catch (IOException | ParseException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
//            Window currentMonitor = getPrimaryScene().getWindow();
//            alert.initOwner(currentMonitor);

            alert.setTitle(null);
            alert.setHeaderText("No courses found.");
            System.err.println("Could not load any courses from manifest.");
            e.printStackTrace();
        }

        // Convert the course manifest into a JSON array.
        JSONArray courseManifestJson = (JSONArray) courseManifest;

        List<Button> courseButtons = new ArrayList<Button>();

        // Iterate now over the objects in the course JSON array. First to build the button list,
        // then again to build the buttons themselves
        for (Object jsonObject : courseManifestJson) {
            courseButtons.add(new Button());
        }
        int buttonIndex = 0;
        for (Object jsonObject : courseManifestJson) {
            // A v box so that a label can go below each course button.
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);

            Button button = courseButtons.get(buttonIndex);
            button.getStyleClass().set(0, "no_image_button");
            double buttonSize = Math.min(
                hBoxCourseList.getWidth() / courseManifestJson.size() - hBoxCourseList.getSpacing(),
                hBoxCourseList.getHeight() / 2.0
            );
            button.setMinWidth(buttonSize);
            button.setMaxWidth(button.getMinWidth());
            button.setMinHeight(button.getMinWidth());
            button.setMaxHeight(button.getMinHeight());

            String courseName = ((JSONObject) jsonObject).get("name").toString();
            // If we are not in development mode, then don't load the Test course
            if (!ClientMain.debug && courseName.equals("END")) {
                continue;
            }
            Canvas canvas = new Canvas(buttonSize, buttonSize);
            Course course = Course.getCourseFromXmlPath("/courses/" + courseName + "/race.xml");
            CourseController courseController = new CourseController(course, canvas);
            courseController.addCartesianToCourseFeatures();
            CourseDrawer.drawCourse(course, canvas.getGraphicsContext2D(), false, 1.0);
            button.setGraphic(canvas);

            vBox.getChildren().add(button);

            // Tell the server main about the chosen course.
            button.setOnAction(e -> {
                ServerMain.setCourseName(courseName);
                for (Button otherButton : courseButtons) {
                    otherButton.getStyleClass().set(0, "no_image_button");
                }
                button.getStyleClass().set(0, "no_image_button_selected");
            });

            Label label = new Label(courseName);
            label.setFont(Font.font(35));
            vBox.getChildren().add(label);

            // Finally the v box to the courses h box.
            hBoxCourseList.getChildren().add(vBox);
            buttonIndex++;
        }
    }

    /**
     * Start the server going, creates a new server object and
     * adds the lobby to it so that it can update the table
     */
    public void startServer() throws InterruptedException {
        // Change into the join scene, so user can set their things


        if (ClientMain.clientServer != null) {

            Optional<ButtonType> result = ClientMain.makeAlertBox("Host Server", "There is already a server running on your " + "computer. Do you want to close it?");
            if (result.get() == ButtonType.OK) {
                ClientMain.clientServer.shutdownServer();
                ClientMain.clientServer = null;
                ClientMain.changeScene(SceneName.JOIN);
                ClientMain.closeDataStream();
            } else {
                return;
            }

        }
        // Create our new server.
        try {
            ClientMain.clientServer = new ServerMain();

        } catch (IOException e) {
            Optional<ButtonType> result = ClientMain.makeAlertBox("Host Server", "There is already a server running on your " + "computer. Do you want to join it?");
            if (result.get() == ButtonType.OK) {
                ClientMain.changeScene(SceneName.JOIN);
            } else {
                return;
            }
        }
        ClientMain.changeScene(SceneName.JOIN);

    }


    private void slideUp(){
        double start = 0;
        double target = 1080;
        Transition slideUp = new Transition() {
            {
                setCycleDuration(GUIHelper.scrollDuration);
            }
            @Override
            protected void interpolate(double frac) {
                double offset = start + frac * (target - start);
                Rectangle2D rect = new Rectangle2D(0, 1080 - offset, 1920, 1080);
                backgroundImageView.setViewport(rect);
            }
        };
        slideUp.playFromStart();
    }


    public void exit() {
        GUIHelper.fadeElementPane(elementPane, 500.0, false);
        slideUp();
        PauseTransition p = new PauseTransition(GUIHelper.scrollDuration);
        p.setOnFinished(event -> ClientMain.changeScene(SceneName.INITIAL));
        p.play();
    }

    public void muteSounds(ActionEvent actionEvent) {
        ClientMain.muteSounds(soundButton);
        ClientMain.muteAll();
    }
    public void setMuteIcon(){
        ClientMain.setSoundIcon(soundButton);

    }
}
