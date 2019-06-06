package seng302.Client.Controllers;

import javafx.animation.TranslateTransition;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import seng302.Client.ClientMain;
import seng302.Common.Boat;
import seng302.Common.RaceStatus;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Controller for the leaderboard
 */
public class LeaderboardController implements Initializable {
    private MainWindowController parentController;
    private double xOffset;
    private double yOffset;
    private ObservableList<Boat> boatList = FXCollections.observableArrayList(); //fleet list
    private ObservableList<Boat> currBoats = FXCollections.observableArrayList(boat -> new Observable[] {
            boat.speedProperty()
    });
    private boolean isInitialised = false;
    private Timer timer = new Timer("LeaderBoard Update Timer");

    @FXML
    private GridPane leaderboard;
    @FXML
    private TableView<Boat> leaderboardTableView;
    @FXML
    private TableColumn<Boat, String> positionColumn;
    @FXML
    private TableColumn<Boat, Double> speedColumn;
    @FXML
    private TableColumn<Boat, String> nameColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("placing"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nameAndAbbreviated"));
        speedColumn.setCellValueFactory(p -> {
            DecimalFormat df = new DecimalFormat("##.##");
            return new ReadOnlyObjectWrapper<Double>(Double.parseDouble(df.format(p.getValue().getSpeed())));
        });

        leaderboardTableView.setItems(currBoats); //set the leaderboard table
    }


    /**
     * Sets the parent controller for the leaderboard controller
     * @param parentController The parent of the leaderboard controller
     */
    public void setParentController(MainWindowController parentController) {
        this.parentController = parentController;
    }

    /**
     * Helper to set dimension offsets, so that a leaderboard of different sizes will still
     * toggle appropriately
     * @param x xoffset
     * @param y yoffset
     */
    public void setDimensionOffsets(double x, double y) {
        xOffset = x;
        yOffset = y;
    }

    /**
     * A toggle function to show the leaderboard
     * @param show If the leaderboard is showing or not
     */
    public void showLeaderboard(boolean show) {
        double xStart = 0;
        double xEnd = 0;

        // ---------------------------------

        if (show) {     // Updated with toggle logic
            //parentController.setDarkPane(true);
            xStart = -xOffset;
        } else {
            xEnd = -xOffset;
        }

        TranslateTransition tt = new TranslateTransition(Duration.millis(100), leaderboard);
        tt.setFromX(xStart);
        tt.setFromY(yOffset);
        tt.setToX(xEnd);
        tt.setCycleCount(1);
        tt.play();
    }

    /**
     * Starts a timer in order to regularly update the race placings in the leader board table.
     */
    public void startRacePlacings() {
        if (isInitialised) {
            timer.cancel();
            currBoats.clear();
        }
        //if (!isInitialised) {
            isInitialised = true;
            currBoats.setAll(ClientMain.getRace().getFleet().getBoats());

            List<Boat> finishedBoats = new ArrayList<>();
            timer = new Timer("LeaderBoard Update Timer");
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    currBoats.clear();
                    currBoats.setAll(ClientMain.getRace().getFleet().getBoats());
                    List<Boat> unfinishedBoats = new ArrayList<>();
                    for (Boat b : currBoats) { //to
                        String placing = b.getPlacing();
                        if (b.isFinished() && !finishedBoats.contains(b)) {
                            if (!placing.equals("-")) {
                                int index = Integer.parseInt(placing) - 1;
                                finishedBoats.add(index, b); //put finished boats in correct order
                            }
                        } else if (!b.isFinished()) {
                            unfinishedBoats.add(b); //add unfinished boats to a list
                        }
                    }

                    Collections.sort(unfinishedBoats, Comparator.reverseOrder()); //sort the unfinished boats to put in correct placing order (using comparator in Boat0
                    currBoats.clear();
                    currBoats.addAll(finishedBoats); //add finished and unfinished boats to current boat list (all in correct order)
                    currBoats.addAll(unfinishedBoats);
                    for (Boat b : currBoats) {
                        int place = currBoats.indexOf(b) + 1; //set the placings of each boat based on their position in the list
                        b.setPlacing(Integer.toString(place));
                        //update out boats placing.
                        if (b.getSourceID() == ClientMain.getAssignedSourceID()) {
                            ClientMain.mainWindowController.updatePlacingText(Integer.toString(place));
                        }
                    }
                    // If our race is finished, then stop the timer!
                    if (ClientMain.getRace().getRaceState().equals(RaceStatus.FINISHED)) {
                        timer.cancel();
                    }
                }
            }, 0, 500);
        //}
    }


    public ObservableList<Boat> getCurrBoats() {
        return currBoats;
    }
}
