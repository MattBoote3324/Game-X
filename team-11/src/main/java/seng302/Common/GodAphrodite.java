package seng302.Common;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import seng302.Common.Utils.Calculator;
import seng302.Server.Model.Race;


/**
 * Aphrodite god class.
 * Implements Ability methods for aphrodite god and contains info e.g pictures
 */
public class GodAphrodite extends GreekGod {

    private Image godImage = new Image("images/AphroditeIcon.png");

    /**
     * Aphrodite's ability for the W keypress
     * Finds the boat that is in last place
     * Sets the aphrodite ability in use for all boats that aren't the source boat
     * Sets the attraction point of each boat that isn't the source boat
     * @param race race
     * @param boatID boatid of the boat that used the ability
     */
    @Override
    public void abilityW(Race race, int boatID) {

        Boat behindBoat = Calculator.boatInLastPlace(race.getFleet().getBoats(),race.getCourse());

        for(Boat boat: race.getFleet().getBoats()){
            if(boat.getSourceID() != boatID){
                boat.setAphroditeAbilityInUse(true);
                boat.setAphroditeAttractToPoint(behindBoat.toPoint());
            }
        }
    }

    @Override
    public GodType getGodType() {
        return GodType.APHRODITE;
    }

    @Override
    public ImageView getGodImage() {

        ImageView godImage = new ImageView(new Image("characters/aphrodite/aphrodite.png"));
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
