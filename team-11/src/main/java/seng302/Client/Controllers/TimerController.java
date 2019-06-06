package seng302.Client.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Duration;
import seng302.Client.ClientMain;
import seng302.Common.Messages.RaceStatusMessage;
import seng302.Common.Messages.XmlMessage;
import seng302.Common.RaceStatus;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This timer controller class is responsible for the TopDisplay.fxml, the pane at the top
 * of the window showing the regatta name and elapsed time
 */
public class TimerController implements Initializable, Observer {

    @FXML
    private Label lblRegattaName;
    @FXML
    private Label lblRaceState;
    @FXML
    private Label lblElapsedTime;
    @FXML
    private Label largeCountdownLabel;

    private int countdownLabelTime = 10;    // Variable for when the large countdown text is displayed
    private Timeline countdownTimeline = new Timeline();
    private long expectedStartTime;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ClientMain.timerController = this;
        lblRegattaName.setText("~RegattaName~");
        lblRaceState.setText("~RaceState~");
        largeCountdownLabel.setText(Integer.toString(countdownLabelTime));
    }

    /**
     * Update method from anything that the TimerController is watching
     *
     * @param o Object that is observered
     * @param arg Data that is changed
     */
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof RaceStatusMessage) {
            RaceStatusMessage rsm = ((RaceStatusMessage) arg);

            if (!lblRaceState.getText().equals(rsm.getRaceStatus().toString())) {
                Platform.runLater(() -> setLblRaceStatus(rsm.getRaceStatus().toString()));
            }

            if (rsm.getRaceStatus().compareTo(RaceStatus.STARTED) < 0) {  // before the race has started
                expectedStartTime = rsm.getExpectedStartTime();
            }
        }

        if (arg instanceof XmlMessage) {
            XmlMessage msg = ((XmlMessage) arg);
            if (msg.getXmlMsgSubType() == XmlMessage.REGATTA_SUBTYPE && !lblRegattaName.getText().equals(msg.getRegattaName())) {
                Platform.runLater(() -> lblRegattaName.setText(msg.getRegattaName()));
            }
        }
    }

    /**
     * This function is called to start updates of the elapsed time label as appropriate.
     *
     * Compares the current date/time of when it was called, to the expected start time (received from the
     * race status messages).
     * The currentTime variable is incremented to maintain the timeline.
     */
    public void startRaceTimer() {
        if (countdownTimeline.getStatus() == Animation.Status.STOPPED) {
            Date currentTime = new Date();  // the date/time of when the function is first called
            countdownTimeline.setCycleCount(Animation.INDEFINITE);
            countdownTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000),
                event -> {
                    long elapsedTimeMillis = currentTime.getTime() - expectedStartTime; // the time difference to the expected start time
                    if (Math.floorDiv(elapsedTimeMillis, 1000) + 1 == -countdownLabelTime) {    // start the large countdown at the appropriate time
                        startLargeCountdown();
                    }
                    String elapsedTimeString = String.format("%d minutes %d seconds",
                            // + 1000 due to floorDiv it rounding down - this fixed the extra long zeroth tick due to rounding issues
                            TimeUnit.MILLISECONDS.toMinutes(Math.floorDiv(elapsedTimeMillis, 1000) * 1000 + 1000),
                            TimeUnit.MILLISECONDS.toSeconds(Math.floorDiv(elapsedTimeMillis, 1000) * 1000 + 1000) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(Math.floorDiv(elapsedTimeMillis, 1000) * 1000 + 1000))
                    );
                    lblElapsedTime.setText(elapsedTimeString);
                    currentTime.setTime(currentTime.getTime() + (long) 1000);
                }));
            countdownTimeline.play();
        }
    }

    /**
     * Starts update of the large countdown label using a timertask.
     * Delay by 1 second to show the label without any modifications.
     */
    private void startLargeCountdown() {
        largeCountdownLabel.setVisible(true);

        fadeInText(Integer.toString(countdownLabelTime));

        Timer timer = new Timer("Countdown timer thread");
        timer.scheduleAtFixedRate(new TimerTask() {
            int countdown = countdownLabelTime;
            @Override
            public void run() {
                if (countdown == 1) {   // On this tick, show the Go! text
                    Platform.runLater(() -> fadeInText("GO!"));
                    countdown--;
                } else if (countdown == 0) {    // Stop the countdown once reaching zero
                    largeCountdownLabel.setVisible(false);
                    cancel();
                } else {
                    Platform.runLater(() -> fadeInText(Integer.toString(countdown)));
                    countdown--;
                }
            }
        }, 1000, 1000); // Delay by 1 second to show the label without any modifications
    }

    /**
     * Scale and Fades in the large countdown text
     * @param displayString The current string to display - the countdown number or the Go! text
     */
    private void fadeInText(String displayString) {
        largeCountdownLabel.setText(displayString);
        FadeTransition fadeTransition =
                new FadeTransition(Duration.millis(1000), largeCountdownLabel);
        fadeTransition.setFromValue(0.0f);
        fadeTransition.setToValue(1.0f);
        fadeTransition.setCycleCount(1);
        fadeTransition.setAutoReverse(false);

        ScaleTransition scaleTransition =
                new ScaleTransition(Duration.millis(1000), largeCountdownLabel);
        scaleTransition.setFromX(10f);
        scaleTransition.setFromY(10f);
        scaleTransition.setToX(1f);
        scaleTransition.setToY(1f);
        scaleTransition.setCycleCount(1);
        scaleTransition.setAutoReverse(false);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(
                fadeTransition,
                scaleTransition
        );
        parallelTransition.setCycleCount(1);
        parallelTransition.play();
    }

    private void setLblRegattaName(String regattaName) {
        lblRegattaName.setText(regattaName);
    }

    private void setLblRaceStatus(String raceStatus) {
        lblRaceState.setText(raceStatus);
    }
}
