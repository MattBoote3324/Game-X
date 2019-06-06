package seng302.Client.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.InnerShadow;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Screen;
import javafx.util.Duration;
import seng302.Client.ClientMain;
import seng302.Common.Boat;
import seng302.Common.Fleet;
import seng302.Common.GUIHelper;
import seng302.Common.GodType;
import seng302.Common.Utils.Calculator;
import seng302.Common.Utils.Sound;
import seng302.Server.Model.Race;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Random;

/**
 * Render any given ability onto any given graphics context.
 */
public class AbilityDrawer {

    //todo maybe we don't need this separate gc anymore?
    private static final int COOLDOWN_PERIOD = 0; //Todo - make not 0 cooldowns lmao
    private static Image posWGif;
    private static Image zeusWGif;
    private static Image aphroWGif;
    private static Image zeusQGif;
    private static Image posQGif;
    private static Image aphroQGif;
    private static Image hadesQGif;
    private static Image aresQGif;
    private static Image aresWGif;

    private static Media zeusWsoundEffect = new Media(AbilityDrawer.class.getResource("/soundz/lightningstorm.mp3").toString());
    private static Media aresWsoundEffect = new Media(AbilityDrawer.class.getResource("/soundz/ares_W_sound.mp3").toString());
    private static Media aphroditeWsoundEffect = new Media(AbilityDrawer.class.getResource("/soundz/aphrodite_W_sound.mp3").toString());
    private static Media poseidonWsoundEffect = new Media(AbilityDrawer.class.getResource("/soundz/poseidon_w_sound.mp3").toString());
    private static Media hadesWsoundEffect = new Media(AbilityDrawer.class.getResource("/soundz/hades_w_sound.mp3").toString());

    private static Media zeusQsoundEffect = new Media(AbilityDrawer.class.getResource("/soundz/zeusQZap.mp3").toString());
    private static Media aresQSoundEffect = new Media(AbilityDrawer.class.getResource("/soundz/ares_q_sound.mp3").toString());
    private static Media aphroditeQsoundEffect = new Media(AbilityDrawer.class.getResource("/soundz/aphrodite_Q_sound.mp3").toString());
    private static Media poseidonQsoundEffect = new Media(AbilityDrawer.class.getResource("/soundz/poseidon_Q_sound.mp3").toString());
    private static Media hadesQsoundEffect = new Media(AbilityDrawer.class.getResource("/soundz/hades_Q_sound.mp3").toString());
    private static Boolean isZeusActive = false;

    private static AnimationTimer ghostBoatAnimationTimer;

    /**
     * Preload the images for the abilities
     */
    public static void loadAbilityImages() {
        //posWGif = new File(AbilityDrawer.class.getResource("images/w_ability_poseidon.gif").toString());
        posWGif = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/w_ability_poseidon.gif"));
        zeusWGif = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/lightningstorm.gif"));
        aphroWGif = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/heartbeat.gif"));
        zeusQGif = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/lightning.gif"));
        posQGif = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/wave.gif"));
        aphroQGif = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/heart.gif"));
        hadesQGif = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/skeleton.gif"));
        aresQGif = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/flameThrower.gif"));
        aresWGif = new Image(AbilityDrawer.class.getClassLoader().getResourceAsStream("images/aresRing.gif"));
    }

    /**
     * Method that draws the selected god's ability on the ability graphics context
     * Next sprint there will probably be different methods for each ability,as they will react differently
     * @param boat boat who's ability has been used
     * @param qGif preloaded image to display the gif
     */
    private static void drawAbility(Boat boat, Image qGif, Media sound) {
        int duration = 250;
        double zoomFactor = ClientMain.raceViewController.getZoomFactor();

        ImageView abilityView = new ImageView();
        abilityView.setImage(qGif);

        abilityView.setPreserveRatio(false);

        double angle = Math.toRadians(boat.getHeading());
        double startX = boat.getX() + (10 * Math.cos(angle) + BoatDrawer.BOAT_HEIGHT / 2 * Math.sin(angle)) * zoomFactor;
        double startY = boat.getY() + (10 * Math.sin(angle) - BoatDrawer.BOAT_HEIGHT / 2 * Math.cos(angle)) * zoomFactor;

        abilityView.setX(startX);
        abilityView.setY(startY);

        abilityView.setFitWidth(25 * zoomFactor);
        abilityView.setFitHeight(138 * zoomFactor);

        abilityView.getTransforms().add(new Rotate(boat.getHeading() + 180, abilityView.getX(), abilityView.getY()));

        try {
            new Sound(sound, false, false); //note: this says not used, but don't remove.
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        long startTime = System.currentTimeMillis();
        Platform.runLater(() -> ClientMain.mainWindowController.getRaceViewAnchor().getChildren().add(abilityView));
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                now = System.currentTimeMillis();
//                if (finalAphroFlag) {
//                    int frameNumber = (int) (finalFrames.size() * (now - startTime) / (duration / 4.0));
//                    if (frameNumber < finalFrames.size()) {
//                        abilityView.setImage(SwingFXUtils.toFXImage(finalFrames.get(frameNumber), null));
//                    } else {
//                        abilityView.setImage(SwingFXUtils.toFXImage(finalFrames.get(finalFrames.size() - 1), null));
//                        if (now % 2 == 0) {
//                            abilityView.setEffect(new ColorAdjust(0,0.7,0.7,0.7));
//                        } else {
//                            abilityView.setEffect(null);
//                        }
//                    }
//                }
                if (now >= startTime + duration) {
                    ClientMain.mainWindowController.getRaceViewAnchor().getChildren().remove(abilityView);
                    stop();
                    abilityView.setEffect(null);
                }
            }
        }.start();
    }

    /**
     * Draws the poseidon w ability
     *
     * @param attackerID attacker id
     */
    private static void drawPoseidonAbility(int attackerID){
        try {
            Sound poseidonWSound  = new Sound(poseidonWsoundEffect, false, false);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        int posWWidth = 40;
        int posWHeight = 30;
        for(Boat boat: ClientMain.getRace().getFleet().getBoats()){
            if(boat.getSourceID() != attackerID) {

                long stopTime = System.currentTimeMillis() + 3000; //Thing is 3 seconds long

                new AnimationTimer() {

                    @Override
                    public void handle(long now) {
                        double zoomFactor = ClientMain.raceViewController.getZoomFactor();

                        double boatX = ClientMain.raceViewController.getXPos(boat.getLongitude(), ClientMain.raceViewController.isZoomed());
                        double boatY = ClientMain.raceViewController.getYPos(boat.getLatitude(), ClientMain.raceViewController.isZoomed());

                        ClientMain.raceViewController.getGraphicsContext().drawImage(posWGif, boatX - (posWWidth*zoomFactor)/2, boatY, posWWidth * zoomFactor, posWHeight * zoomFactor);

                        if(System.currentTimeMillis() >= stopTime) {
                            stop();
                        }
                    }
                }.start();

            }
        }

    }



    /**
     * The visual effect for the Zeus ability drawn on every client
     * except for the client that used the ability.
     * @param duration is how long the visual animation will take.
     */
    private static void drawZeusAbility(long duration, int sourceId) {
        try {
            Sound zeusLightning = new Sound(zeusWsoundEffect, false, false);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        if (sourceId != ClientMain.getAssignedSourceID()){
            ImageView abilityView = new ImageView(zeusWGif);


            abilityView.setFitHeight(Screen.getPrimary().getBounds().getHeight());
            abilityView.setFitWidth(Screen.getPrimary().getBounds().getWidth());

            Pane lightPane = new Pane();
            double canvasHeight = ClientMain.mainWindowController.getRaceViewAnchor().getHeight();
            double canvasWidth = ClientMain.mainWindowController.getRaceViewAnchor().getWidth();
            lightPane.setPrefHeight(canvasHeight);
            lightPane.setStyle("-fx-background-color: white");
            lightPane.setPrefWidth(canvasWidth);
            long stopTime = System.currentTimeMillis() + duration/2; //We only want the light pane to be active for half the animation
            Platform.runLater(() -> ClientMain.mainWindowController.getRaceViewAnchor().getChildren().add(lightPane));
            Platform.runLater(() -> lightPane.getChildren().add(abilityView));
            isZeusActive = true;
            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (System.currentTimeMillis() >= stopTime) {
                        lightPane.getChildren().remove(abilityView);
                        GUIHelper.fadeElementPane(lightPane, (double) duration/2 , false); //Fade out of light pane will take out other half of time.
                        isZeusActive = false;
                        stop();
                    }
                }
            }.start();
        } else {
            long stopTime = System.currentTimeMillis() + duration;
            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (System.currentTimeMillis() <= stopTime) {
                        ColorAdjust canvasColour = new ColorAdjust();
                        DropShadow dropShadow = new DropShadow(100, Color.web("ffffff"));
                        dropShadow.setInput(canvasColour);
                        ClientMain.mainWindowController.getRaceViewAnchor().setEffect(dropShadow);
                    } else {
                        ClientMain.mainWindowController.getRaceViewAnchor().setEffect(null);
                        stop();
                    }
                }
            }.start();
        }
    }

    /**
     * Draw the Hades ability.
     *
     * Flip the course horizontal, and turn course colour black, with red glowing background.
     * Lasts ~5 seconds.
     *
     * If clientPressed is true, the course is not flipped, and the course does not go dark.
     *
     * @param clientPressed Whether or not this client is the one who used the ability.
     */
    private static void drawHadesAbility(boolean clientPressed) {
        double animationTime = 1;  // The time of the animation transition in seconds
        double animationTotal = 7; // The total time of the ability effects in seconds

        DropShadow glowAroundCourse = new DropShadow(1000, Color.rgb(255,0,0,0));
        ClientMain.mainWindowController.getRaceViewAnchor().setEffect(glowAroundCourse);

        ColorAdjust backgroundColour = new ColorAdjust();
        InnerShadow glowAroundWindow = new InnerShadow(6000, Color.rgb(255,0,0,0));
        glowAroundWindow.setInput(backgroundColour);
        ClientMain.mainWindowController.getBackgroundColourPane().setEffect(glowAroundWindow);
        //TODO: timeline OPACITY PROPERTY

        try {
            new Sound(hadesWsoundEffect, false, false);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        final Canvas ghostCanvas = new Canvas();

        // This animation timeline reverses itself!
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                // Set glow around window edges
                new KeyFrame(Duration.seconds(animationTime),
                        new KeyValue(glowAroundWindow.colorProperty(), Color.rgb(255,0,0,1), Interpolator.EASE_BOTH)
                ),
                // Set glow around course edges
                new KeyFrame(Duration.seconds(animationTime),
                        new KeyValue(glowAroundCourse.colorProperty(), Color.rgb(255,20,20,1), Interpolator.EASE_BOTH)
                ),
                // So that the animation lasts for the duration of animationTotal
                new KeyFrame(Duration.seconds(animationTotal / 2.0), null)
        );
        if (!clientPressed) {
            timeline.getKeyFrames().addAll(
                    // Flip the canvas
                    new KeyFrame(Duration.seconds(animationTime),
                            new KeyValue(ClientMain.mainWindowController.getRaceViewAnchor().scaleXProperty(), -1.0, Interpolator.EASE_BOTH)
                    ),
                    // Set race canvas see through property
                    new KeyFrame(Duration.seconds(animationTime),
                            new KeyValue(ClientMain.raceViewController.getGraphicsContext().getCanvas().opacityProperty(), 0.1, Interpolator.EASE_BOTH)
                    ),
                    // Set background to red hue
                    new KeyFrame(Duration.seconds(animationTime),
                            new KeyValue(backgroundColour.hueProperty(), 0.82, Interpolator.EASE_BOTH)
                    ),
                    // Set background to red hue
                    new KeyFrame(Duration.seconds(animationTime),
                            new KeyValue(backgroundColour.saturationProperty(), -0.5, Interpolator.EASE_BOTH)
                    ),
                    // Set background to redder saturation
                    new KeyFrame(Duration.seconds(animationTime),
                            new KeyValue(backgroundColour.brightnessProperty(), -0.8, Interpolator.EASE_BOTH)
                    )
            );

            Random random = new Random();

            Fleet scatteredGhostArmy = new Fleet();
            for (int i = 0; i < 50; i++) {
                Boat ghostBoat = new Boat(666, "Ghost", "X");
                ghostBoat.setX(ClientMain.mainWindowController.getBackgroundColourPane().getWidth() / 4.0 + random.nextDouble() * ClientMain.mainWindowController.getBackgroundColourPane().getWidth() / 2.0);
                ghostBoat.setY(ClientMain.mainWindowController.getBackgroundColourPane().getHeight() / 4.0 + random.nextDouble() * ClientMain.mainWindowController.getBackgroundColourPane().getHeight() / 2.0);
                ghostBoat.setHeading(random.nextInt(359));
                ghostBoat.setSpeed(20);
                scatteredGhostArmy.addBoat(ghostBoat);
            }

            double yPos = 0.50;
            double xPos = 0.10;
            Fleet ghostArmy = new Fleet();
            for (int i = 0; i < 20; i++) {
                Boat ghostBoat = new Boat(666, "Ghost", "X");
                ghostBoat.setX(xPos * ClientMain.mainWindowController.getBackgroundColourPane().getWidth());
                ghostBoat.setY(yPos * ClientMain.mainWindowController.getBackgroundColourPane().getHeight());
                ghostBoat.setSpeed(20);
                ghostArmy.addBoat(ghostBoat);
                yPos += .03;
                xPos += .03;
            }

            ghostCanvas.setWidth(ClientMain.mainWindowController.getBackgroundColourPane().getWidth());
            ghostCanvas.setHeight(ClientMain.mainWindowController.getBackgroundColourPane().getHeight());
            ghostCanvas.setOpacity(0.1);
            Platform.runLater(()->ClientMain.mainWindowController.getRaceViewAnchor().getChildren().add(ghostCanvas));

            ghostBoatAnimationTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    for (Boat ghostBoat : ghostArmy.getBoats()) {
                        ghostBoat.setX(ghostBoat.getX() + (2 * random.nextDouble()));
                        ghostBoat.setY(ghostBoat.getY() - (2 * random.nextDouble()));
                        ghostBoat.setHeading(45);
                    }
                    ghostCanvas.getGraphicsContext2D().clearRect(0 ,0, ghostCanvas.getWidth(), ghostCanvas.getHeight());
                    ghostArmy.drawBoats(ghostCanvas.getGraphicsContext2D(), ClientMain.raceViewController.getZoomFactor(), false);
                    scatteredGhostArmy.drawBoats(ghostCanvas.getGraphicsContext2D(), ClientMain.raceViewController.getZoomFactor(), false);
                }
            };
            ghostBoatAnimationTimer.start();
        }

        timeline.setAutoReverse(true);
        timeline.setCycleCount(2); //Needed for the reverse effect
        timeline.playFromStart();
        timeline.setOnFinished(event -> {
            ClientMain.mainWindowController.getBackgroundColourPane().setEffect(null);
            ClientMain.mainWindowController.getRaceViewAnchor().setEffect(null);

            // Hard reset non Effect object effects
            ClientMain.mainWindowController.getRaceViewAnchor().setScaleX(1.0);
            ClientMain.raceViewController.getGraphicsContext().getCanvas().setOpacity(1.0);

            ClientMain.mainWindowController.getRaceViewAnchor().getChildren().remove(ghostCanvas);
            if (ghostBoatAnimationTimer != null) {
                ghostBoatAnimationTimer.stop();
            }
        });
    }

    /**
     * Draw the aphrodite W key ability
     * Adds a blur & pink tint to the screen
     * If client pressed is true, there should be no blur
     * @param clientPressed Whether or not this client is the one who used the ability.
     */
    private static void drawAphroditeAbility(boolean clientPressed, Race race, long abilityDuration){
        double imageScale = 4;
        double blurRadius = 5;

        double zoomFactor = ClientMain.raceViewController.getZoomFactor();

        long stopTime = System.currentTimeMillis() + abilityDuration;
        Boat lastBoat = Calculator.boatInLastPlace(race.getFleet().getBoats(), race.getCourse());
        double lastBoatX = lastBoat.getLongitude();
        double lastBoatY = lastBoat.getLatitude();


        GaussianBlur blur = new GaussianBlur();
        blur.setRadius(blurRadius);
        if(!clientPressed) ClientMain.mainWindowController.getRaceViewAnchor().setEffect(blur); //set blur on clients if they didn't activate ability

        StackPane pane = new StackPane();


        if(!clientPressed) {
            pane.setStyle("-fx-background-color: rgba(252, 181, 247, 0.3); -fx-background-radius: 10;");
            pane.setMinWidth(ClientMain.mainWindowController.getRaceViewAnchor().getWidth());
            pane.setMinHeight(ClientMain.mainWindowController.getRaceViewAnchor().getHeight());

            Platform.runLater(() -> ClientMain.mainWindowController.getRaceViewAnchor().getChildren().addAll(pane));
        } else {
            ColorAdjust canvasColour = new ColorAdjust();
            DropShadow dropShadow = new DropShadow(100, Color.web("ff66ff"));
            dropShadow.setInput(canvasColour);
            ClientMain.mainWindowController.getRaceViewAnchor().setEffect(dropShadow);
        }
        try {
            Sound aphroditeWsound = new Sound(aphroditeWsoundEffect, false, false);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        new AnimationTimer() {
            double zoom = zoomFactor;
            @Override
            public void handle(long now) {
                zoom = ClientMain.raceViewController.getZoomFactor();
                double aphroHeight = (aphroWGif.getWidth() / imageScale) * zoom;
                double aphroWidth = (aphroWGif.getWidth() / imageScale) * zoom;


                //If the client zooms it redraws the gif at the correct scale.
                double aphroX = ClientMain.raceViewController.getXPos(lastBoatX, ClientMain.raceViewController.isZoomed()) + ((BoatDrawer.BOAT_WIDTH / 2) - ((aphroWGif.getWidth() / 2) / imageScale)) * zoom;
                double aphroY = ClientMain.raceViewController.getYPos(lastBoatY, ClientMain.raceViewController.isZoomed()) + ((BoatDrawer.BOAT_HEIGHT / 2) - ((aphroWGif.getWidth() / 2) / imageScale)) * zoom;
                ClientMain.raceViewController.getGraphicsContext().drawImage(aphroWGif,aphroX , aphroY, aphroWidth, aphroHeight);


                zoom = ClientMain.raceViewController.getZoomFactor();

                if(System.currentTimeMillis() >= stopTime) {
                    ClientMain.mainWindowController.getRaceViewAnchor().setEffect(null);
                    ClientMain.mainWindowController.getRaceViewAnchor().getChildren().remove(pane);

                    stop();
                }
            }
        }.start();


    }

    /**
     * Draws the Ares W ability which places a gif of rotating fire for the ability duration around the sourceId boat.
     * @param sourceId Id of the boat that used the ability
     * @param duration duration of the ability.
     */
    private static void drawAresAbility(int sourceId, long duration){
        double imageScale = 4.9;
        double zoomFactor = ClientMain.raceViewController.getZoomFactor();
        long stopTime = System.currentTimeMillis() + duration;

        Boat boat = ClientMain.getRace().getFleet().getBoat(sourceId);
        ImageView abilityView = new ImageView(aresWGif);
        abilityView.setPreserveRatio(true);
        abilityView.setFitHeight((aphroWGif.getWidth() / imageScale) * zoomFactor);

//        double width = ClientMain.raceViewController.getScaleGeoToCartesian()*Boat.ARES_ABILITY_RADIUS_LATLONG;


        Platform.runLater(() -> ClientMain.mainWindowController.getRaceViewAnchor().getChildren().add(abilityView));
        try {
            Sound aresWsound = new Sound(aresWsoundEffect, false, false);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        new AnimationTimer() {
            double zoom = zoomFactor;
            @Override
            public void handle(long now) {
                //Grabs the zoomfactor and applies to the gif constantly
                if(isZeusActive){
                    abilityView.setVisible(false);
                }else{
                    abilityView.setVisible(true);
                }
                zoom = ClientMain.raceViewController.getZoomFactor();
                //ClientMain.mainWindowController.getRaceViewAnchor().getChildren().remove(abilityView);
                abilityView.setX(boat.getX()  - ((aresWGif.getWidth() / 2) / imageScale) * zoom);
                abilityView.setY(boat.getY()  - ((aresWGif.getHeight() / 2) / imageScale) * zoom);
                abilityView.setFitHeight((aresWGif.getWidth() / imageScale) * zoom);
                // ClientMain.mainWindowController.getRaceViewAnchor().getChildren().add(abilityView);

                if(System.currentTimeMillis() >= stopTime){
                    ClientMain.mainWindowController.getRaceViewAnchor().getChildren().remove(abilityView);
                    stop();
                }
            }
        }.start();


    }

    /**
     * Method that calls the draw ability method with parameters determined by the type of God
     * @param avatarType god type of boat that used ability
     * @param sourceId id of the client that used the ability
     */
    public static void abilityQ(int avatarType, int sourceId) {

        if(!isZeusActive){ //don't draw gif if zeus ability is active as we don't want to see the zapping gifs
            switch (GodType.values()[avatarType]) {
                case ZEUS:
                    drawAbility(ClientMain.getRace().getFleet().getBoat(sourceId), zeusQGif, zeusQsoundEffect);
                    break;
                case POSEIDON:
                    drawAbility(ClientMain.getRace().getFleet().getBoat(sourceId), posQGif, poseidonQsoundEffect);
                    break;
                case APHRODITE:
                    drawAbility(ClientMain.getRace().getFleet().getBoat(sourceId), aphroQGif, aphroditeQsoundEffect);
                    break;
                case HADES:
                    drawAbility(ClientMain.getRace().getFleet().getBoat(sourceId), hadesQGif, hadesQsoundEffect);
                    break;
                case ARES:
                    drawAbility(ClientMain.getRace().getFleet().getBoat(sourceId), aresQGif, aresQSoundEffect);
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * Method that calls the draw ability method with parameters determined by the type of God
     * @param avatarType god type of boat that used ability
     * @param sourceId id of the client that used the ability
     */
    public static void abilityW(int avatarType, int sourceId, Race race){
        //TODO call the render methods for the W abilities
        switch (GodType.values()[avatarType]) {
            case ZEUS:
                drawZeusAbility(2000, sourceId); //2 seconds, will put into manifest later.
                break;
            case POSEIDON:
                drawPoseidonAbility(sourceId);
                break;
            case APHRODITE:
                drawAphroditeAbility(sourceId == ClientMain.getAssignedSourceID(), race, 3000);
                break;
            case HADES:
                drawHadesAbility(sourceId == ClientMain.getAssignedSourceID());
                break;
            case ARES:
                drawAresAbility(sourceId,Boat.ARES_ABILITY_DURATION);
                break;
            default:
                break;
        }
    }

}
