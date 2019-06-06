package seng302.Common;

import javafx.animation.Transition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import seng302.Server.Model.Race;

import java.util.Random;

/**
 * Created by mbo57 on 26/08/17.
 */
public class GodZeus extends GreekGod {

    private final int BOUND = 359;
    private final int CYCLE_AMOUNT  = 1080; //3 360 degree cycles
    private final double SPIN_DURATION = 2.5;

    private Image godImage = (new Image("images/ZeusIcon.png"));

    /**
     *
     * @param race the current race which holds a fleet.
     * @param boatID this is the id of the boat that used the ability.
     * The abilityW concrete method for Zeus. Here it spins the boat around for 3 spins and then to a random heading.
     */


    @Override
    public void abilityW(Race race, int boatID) {

        for (Boat boat: race.getFleet().getBoats()){
            if (boat.getSourceID() != boatID){ //For all boats that aren't your own
                Random r = new Random();
                double heading = r.nextInt(BOUND) + CYCLE_AMOUNT; //3 spins extra for animation

                double start = boat.getHeading();
                double target = heading;
                Transition rotateBoat = new Transition() {
                    {
                        setCycleDuration(Duration.seconds(SPIN_DURATION));
                    }
                    @Override
                    protected void interpolate(double frac) {
                        double offset = start + frac * (target - start);
                        boat.setHeading(offset);
                    }
                };

                rotateBoat.playFromStart();
            }
        }
    }

    @Override
    public GodType getGodType() {
        return GodType.ZEUS;
    }

    @Override
    public ImageView getGodImage(){

        ImageView godImage = new ImageView(new Image("characters/zeus/zeus.png"));
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
