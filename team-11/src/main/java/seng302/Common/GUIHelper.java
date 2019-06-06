package seng302.Common;

import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.css.PseudoClass;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Various helpers for use with the Gui
 */
public class GUIHelper {

    private static double VIEWPORT_WIDTH = 1920;
    private static double VIEWPORT_HEIGHT = 1080;
    public static javafx.util.Duration scrollDuration = javafx.util.Duration.seconds(1);

    public static void toggleJFXButtonPseudoClass(JFXButton button, String className){
        if (button.getPseudoClassStates().contains(PseudoClass.getPseudoClass(className))) {
            button.pseudoClassStateChanged(PseudoClass.getPseudoClass(className), false);
        } else {
            button.pseudoClassStateChanged(PseudoClass.getPseudoClass(className), true);
        }
    }

    public static void setJFXButtonPseudoClass(JFXButton button, String className, boolean active){
            button.pseudoClassStateChanged(PseudoClass.getPseudoClass(className), active);
    }


    /**
     * Creates a new JFXButton with the passed in button name and
     * an optional way of passing in an image to be used on the button
     *
     * @param buttonName String to put on a button
     * @return a new JFX button
     */
    public static JFXButton buttonMaker(String buttonName){
            JFXButton button = new JFXButton();
            button.setTooltip(new Tooltip(buttonName));
            button.setButtonType(JFXButton.ButtonType.RAISED);
            button.getStyleClass().addAll("animated-custom-button");
            button.setText(buttonName);
            return button;
    }


    /**
     * Fades the pane in using a duration
     *
     * @param pane     pane to be faded in
     * @param duration time in seconds to fade in
     * @param in       set pane visable
     */
    public static void fader(Pane pane, Double duration, Boolean in) {

        pane.setVisible(true);

        FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(duration), pane);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.play();
        ft.setOnFinished(event -> pane.setVisible(false));
    }

    /**
     * Sets the viewport of an imageview withthe given x, y coridinates.
     * width and height are a standard 1920 x 1080
     * @param backgroundImageView the imageview to set properly
     * @param x the x offset of the image coords
     * @param y the x offset of the image coords
     */
    public static void setViewPort(ImageView backgroundImageView, int x, int y) {
        Rectangle2D rect = new Rectangle2D(x, y, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        backgroundImageView.setViewport(rect);
    }


    /**
     * Fades a pane across a given amount of time
     * @param pane the pane to be faded. Should be the parent of all elements in the scene
     * @param duration the length of time to fade the elements
     * @param in where the fade is from visible to inviible or not. In fades from 0% to 100%
     */
    public static void fadeElementPane(Pane pane, Double duration, boolean in){
        if(in){
            pane.setOpacity(0);
            pane.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(duration), pane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            fadeIn.setOnFinished(e -> pane.setVisible(true));
        } else {
            pane.setOpacity(1);
            pane.setVisible(true);
            FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(duration), pane);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
            fadeOut.setOnFinished(e -> pane.setVisible(false));
        }
    }

    /**
     * Binds the Imageview to its parent pane with the correct dimensions.
     * Does not preserve aspect ratio so may become skewed at inconsistent resolutions
     * @param backgroundImageView  background imageview
     * @param mainPane main pane to be used
     */
    public static void bindImageDimensionsToParent(ImageView backgroundImageView, Pane mainPane) {
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.fitWidthProperty().bind(mainPane.widthProperty());
        backgroundImageView.fitHeightProperty().bind(mainPane.heightProperty());
    }
}

