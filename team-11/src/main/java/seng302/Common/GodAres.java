package seng302.Common;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import seng302.Common.Utils.Calculator;
import seng302.Server.Model.Race;

import java.util.List;

/**
 * Created by mbo57 on 26/08/17.
 */
public class GodAres extends GreekGod {
    private double cooldown = 10000;
    private Image godImage = new Image("images/AresIcon.png");

    /**
     * Ares W ability speeds up the source boat and if it collides with any of the other boats
     * it will reset the boats back the their previous marks
     * @param race race the race object the ability is being activated in
     * @param boatID boatid The Id of the boat that used the ability.
     */
    @Override
    public void abilityW(Race race, int boatID) {
        Fleet fleet = race.getFleet();
        Boat sourceBoat = fleet.getBoat(boatID);
        List<Boat> aresAbilityOnList = fleet.getAresAbilityOnList();

        if (aresAbilityOnList.contains(sourceBoat)) { // checks if the boats ability is already on
            for (Boat boat : fleet.getBoats()) {
                if (!aresAbilityOnList.contains(boat)) { //will only check for collision if the boat doesn't have the ares ability on
                    for (Boat aresBoat : aresAbilityOnList) {
                        handleCollision(race,aresBoat,boat);
                    }
                } else {//this is checking if the boats ability has run out.
                    if (System.currentTimeMillis() >= boat.getAresTimeOn()) {
                        fleet.removeAresAbilityOnList(boat);
                        boat.setAresAbilityInUse(false);
                    }
                }
            }
        } else{//init adding and turning on of the Ares W Ability.
            race.getFleet().addAresAbilityOnList(sourceBoat);
            sourceBoat.setAresAbilityInUse(true);
        }

    }

    @Override
    public GodType getGodType() {
        return GodType.ARES;
    }

    @Override
    public ImageView getGodImage() {
        ImageView godImage = new ImageView(new Image("characters/ares/ares.png"));
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
        return 20_000;
    }

    /**
     * checks for and handles the collision between an ares boat and a another boat.
     * @param race race the race object the ability is being activated in
     * @param aresBoat the boat using the ability
     * @param boat a boat not using the ares ability
     */
    private void handleCollision(Race race, Boat aresBoat, Boat boat){
        double distance = Calculator.distanceBetweenPoints(boat.toPoint(), aresBoat.toPoint());
        CourseFeature courseFeature;
        if (distance <= Boat.ARES_ABILITY_RADIUS_NAUTICAL_MILE) {
            if (boat.getCourseProgress() == 0) { //prestart sends boat back to first mark
                courseFeature = race.getCourse().getCourseFeatureById(boat.getCourseProgress() + 1);
            } else {
                courseFeature = race.getCourse().getCourseFeatureById(boat.getCourseProgress());
            }
            boat.setLatitude(courseFeature.getMidPoint().getLatitude());
            boat.setLongitude(courseFeature.getMidPoint().getLongitude());
        }
    }

    public double getCooldown() {
        return cooldown;
    }
}
