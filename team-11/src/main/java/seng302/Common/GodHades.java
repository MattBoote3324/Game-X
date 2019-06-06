package seng302.Common;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import seng302.Server.Model.Race;


public class GodHades extends GreekGod {

    private Image godImage = (new Image("images/HadesIcon.png"));

    @Override
    public void abilityW(Race race, int boatID) {

    }

    @Override
    public GodType getGodType() {
        return GodType.HADES;
    }

    @Override
    public ImageView getGodImage() {
        ImageView godImage = new ImageView(new Image("characters/hades/hades.png"));
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
        return 20000;
    }
}
