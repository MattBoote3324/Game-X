

package seng302.Client.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import seng302.Client.ClientMain;
import seng302.Common.Messages.RaceStatusMessage;
import seng302.Common.Utils.Calculator;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

/**
 * Created by gmc125 on 19/07/17
 */
public class WindDirectionController implements Initializable, Observer {
    @FXML
    private Group windArrow;
    @FXML
    private Label windSpeedLabel;
    @FXML
    private Label fpsLabel;

    private double lastWindDirection;
    private double lastWindSpeed;

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof RaceStatusMessage) {
            RaceStatusMessage raceStatusMessage = (RaceStatusMessage) arg;
            double windSpeed = Calculator.speedMmsToKnots(raceStatusMessage.getCourseWindSpeed());
            double windDirection = Calculator.hexToDirection(raceStatusMessage.getCourseWindDirection());
            if (windSpeed != lastWindSpeed) {
                scaleWindArrow(windSpeed);
                Platform.runLater(() -> {
                    setWindSpeedLabel(Calculator.speedMmsToKnots(raceStatusMessage.getCourseWindSpeed()));
                });
                lastWindSpeed = windSpeed;
            }
            if (windDirection != lastWindDirection) {
                rotateWindArrow(windDirection);
                lastWindDirection = windDirection;
            }

        }
    }

    /**
     * Method to change the rotation of the wind direction arrow
     *
     * @param windDir direction the wind is coming FROM
     */
    private synchronized void rotateWindArrow(double windDir) {
        windArrow.setRotate(windDir % 360 + 180);
    }

    /**
     * Method to change the length of the wind direction arrow depending on the speed.
     * Note that there are maximum and minimum sizes, above and below which the arrow size will not scale
     *
     * @param windSpeed the speed of the wind in knots
     */
    private synchronized void scaleWindArrow(double windSpeed) {
        /*AC35 races only run if average wind speed is between 6 and 24 knots. I have therefore used 6 knots as the
        minimum size bound and 20 as the max.
         */
        if (windSpeed >= 20) {
            windArrow.setScaleY(1);
        } else if (windSpeed <= 6) {
            windArrow.setScaleY(0.3);
        } else {
            double scaleFactor = 0.3 + 0.7 * (windSpeed - 6) / 14; // This is scaling the arrow between the max and min sizes depending on speed
            windArrow.setScaleY(scaleFactor);
        }
    }

    /**
     * Helper method to set the wind speed label to inform the users about the windspeed in knots
     *
     * @param windSpeed The windspeed in knots. Converted from mm/sec before passing to this method.
     */
    private void setWindSpeedLabel(double windSpeed) {
        windSpeedLabel.setText(String.format("%.2f knots", windSpeed));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientMain.windDirectionController = this;
    }

    public void setFpsText(String text) {
        fpsLabel.setText(text);
    }


    public double getLastWindDirection() {
        return lastWindDirection;
    }

    public double getLastWindSpeed() {
        return lastWindSpeed;
    }

}

