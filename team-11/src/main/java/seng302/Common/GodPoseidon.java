package seng302.Common;

import javafx.animation.PauseTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import seng302.Server.Model.Race;


public class GodPoseidon extends GreekGod {
    private int DISABLE_DURATION = 3;
    private Image godImage = new Image("images/PoseidonIcon.png");

    /**
     * Stops all boats in their tracks for a duration
     * @param race the race object the ability is being activated in
     * @param boatID the id of the boat that triggered it, they are exempt from the effects
     */
    @Override
    public void abilityW(Race race, int boatID) {
        //Server side manipulation

        for (Boat boat: race.getFleet().getBoats()){
            if (boat.getSourceID() != boatID){ //For all boats that aren't your own
                boat.setDisable(true);
            }
        }

        //The length of the pause the other boats must wait for
        //Enables ALL BOATS, may interfere with things later
        PauseTransition pt = new PauseTransition(Duration.seconds(DISABLE_DURATION));
        pt.setOnFinished(event -> {
            for (Boat boat: race.getFleet().getBoats()){
                boat.setDisable(false);
            }
        });

        pt.playFromStart();

    }

    @Override
    public GodType getGodType() {
        return GodType.POSEIDON;
    }

    @Override
    public ImageView getGodImage() {
        ImageView godImage = new ImageView(new Image("characters/poseidon/poseidon.png"));
        godImage.setFitHeight(ICON_SIZE);
        godImage.setFitWidth(ICON_SIZE);
        return godImage;
    }

    @Override
    public Image getGodIcon() {
        return godImage;
    }

    @Override
    public double getCOOLDOWN_PERIOD() {
        return 30000;
    }
}
