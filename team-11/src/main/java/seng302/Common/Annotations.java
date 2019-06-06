package seng302.Common;

import seng302.Client.ClientMain;

public class Annotations {
    // Annotation offsets
    static int ANNOTATION_INITIAL_OFFSET = 15; // initial offset
    static int ANNOTATION_OFFSET_SPACE = 15; // Spacing between boat annotations

    private static double lastAveragedFrameRate;

    private static boolean speedVisible;
    private static boolean tracksVisible;
    private static boolean namesVisible;
    private static boolean vmg_sogVisible;
    private static boolean fpsVisible;
    private static boolean timeSinceMarkVisible;
    private static boolean iconVisible = true;

    private static Fleet fleet;
    private static int annotationOffset = ANNOTATION_INITIAL_OFFSET;

    /**
     * Handles all of the draw/show methods for annotations (names, speed, tracks, VMG, SOG, Time since last mark, FPS)
     * boat annotations use a Fleet object which contains a list
     * boats to set all of the annotations as a group
     *
     */
    public static void draw() {

        if (fleet != null) {

            if (speedVisible) {
                annotationOffset = fleet.showSpeed(ClientMain.raceViewController.getGraphicsContext(), annotationOffset);
            }

            if (namesVisible) {
                annotationOffset = fleet.showNames(ClientMain.raceViewController.getGraphicsContext(), annotationOffset);
            }

            if (tracksVisible) {
                fleet.drawTracks(ClientMain.raceViewController.getGraphicsContext(), System.currentTimeMillis());
            }

            if (vmg_sogVisible) {
                fleet.showVMG_SOG();
            }
            if (timeSinceMarkVisible){
                annotationOffset = fleet.showTimeSinceMark(ClientMain.raceViewController.getGraphicsContext(), annotationOffset);
            }

            if (fpsVisible){
                showFPS();
            } else {
                ClientMain.windDirectionController.setFpsText("");
            }
            if(iconVisible){
                fleet.drawGodIcons(ClientMain.raceViewController.getGraphicsContext(), annotationOffset);
            }
        }

        //reset the offset or they all fly of the canvas lol
        annotationOffset = ANNOTATION_INITIAL_OFFSET;

    }

    /**
     * Calls clear tracks on fleet which clears all the tracks for each boat
     */
    public static void clearTracks(){
        fleet.clearTracks(ClientMain.raceViewController.getGraphicsContext(), System.currentTimeMillis());
    }

    /**
     * Turns on all the boat annotations
     */
    public static void setAll(){
        namesVisible = true;
        tracksVisible = true;
        speedVisible = true;
        timeSinceMarkVisible = true;
        vmg_sogVisible = true;
        fpsVisible = true;
        iconVisible = true;
    }

    /**
     * Hides all the boat annotations
     */
    public static void setNone(){
        namesVisible = false;
        tracksVisible = false;
        speedVisible = false;
        timeSinceMarkVisible = false;
        vmg_sogVisible = false;
        fpsVisible = false;
        iconVisible = false;
    }

    /**
     * Below are individual toggles for the custom annotation setting
     */
    public static void toggleNames(){
        namesVisible = !namesVisible;
    }


    public static void toggleTracks(){
        tracksVisible = !tracksVisible;

    }
    public static void toggleGodIcon(){
        iconVisible = !iconVisible;
    }

    public static void toggleSpeed(){
        speedVisible = !speedVisible;
    }

    public static void toggleTimeSinceMark(){
        timeSinceMarkVisible = !timeSinceMarkVisible;
    }

    public static void toggleVMG_SOG(){
        vmg_sogVisible = !vmg_sogVisible;
    }

    public static void toggleFPS(){
        fpsVisible = !fpsVisible;
    }

    /**
     * This is the fleet that the Annotations class will operate on
     * there is a better way to do this.
     *
     * @param newfleet fleet to be assigned
     */
    public static void assignFleet(Fleet newfleet){
        fleet = newfleet;
    }

    /**
     * Displays latest average frame rate on the fps label on the wind direction controller
     * this should probably be moved from the wind controller.
     */
    static void showFPS() {
        // Frame rate has been updated, change FPS label if needed.
        String fpsText = String.format("%.0f FPS", lastAveragedFrameRate);
        ClientMain.windDirectionController.setFpsText(fpsText);
    }

    /**
     * updates the lastAveragedFrameRate (this is called in RaceViewController where the fps
     * is currently measured)
     * @param measuredFrameRate Framerate that should be measured
     */
    public static void fpsUpdate(double measuredFrameRate){
        lastAveragedFrameRate = measuredFrameRate;
    }
}
