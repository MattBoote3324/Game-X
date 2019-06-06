package seng302.Client;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import seng302.Common.GodType;

import java.util.*;

/**
 * Handle client key presses.
 */
class KeyPressHandler extends Observable {
    private boolean sailsOut = true;
    private boolean zoomDown;

    // Keep track of what action codes are held down. For less delayed input.
    private List<Integer> actionsToTrack = Arrays.asList(5, 6, 7, 8);
    private Map<Integer, Boolean> actionsDown = new HashMap<>();
    private Timer notifyKeyTimer;
    //for keys that we don't care if there is a delay when holding them down.

    private EventHandler<KeyEvent> genericKeyEventHandler = event -> {
        Integer action = 0;  // actions 100 or above signify custom actions (outside protocol).
        switch (event.getCode()) {
            case ESCAPE:  // Quit
                ClientMain.mainWindowController.leaveGame();
                break;
            case SPACE:  // Snap to VMG.
                action = 1;
                break;
            case SHIFT:  // Toggle sails in / out.
                if (sailsOut) {
                    sailsOut = false;
                    action = 2;  // Sails in (Going).
                } else {
                    sailsOut = true;
                    action = 3;  // Sails out (Not going).
                }
                break;
            case ENTER:  // Tack / gybe.
                action = 4;
                break;
            case Z:  // Zoom in.
                if (!zoomDown) {
                    zoomDown = true;
                    action = 100;
                }
                break;
            case X:  // Zoom out.
                if (!zoomDown) {
                    zoomDown = true;
                    action = 101;
                }
                break;
            case Q:  // Ability.
                if (!ClientMain.isCoolDownQ() && !ClientMain.getRace().getFleet().getBoat(ClientMain.getAssignedSourceID()).isFinished()){ //We only want to reanimate the cool-down period if power is available
                    ClientMain.mainWindowController.animateQButtonCoolDown();
                    ClientMain.setQCoolDown(true); //We have Q cool-downs
                    action = 102;
                }
                break;
            case W:  // Ability.
                if (!ClientMain.isCoolDownW() && !ClientMain.getRace().getFleet().getBoat(ClientMain.getAssignedSourceID()).isFinished()) { //We only want to reanimate the cool-down period if power is available
                    ClientMain.mainWindowController.animateWButtonCoolDown(GodType.values()[ClientMain.getAvatarType()].getGod().getCOOLDOWN_PERIOD());
                    ClientMain.setWCoolDown(true); //We also have a W power cool-down.
                    action = 103;
                }
                break;
            case TAB:  // Show leader board.
                ClientMain.mainWindowController.showLeaderboard();
                break;
            case F1:  // Show help.
                ClientMain.mainWindowController.helpButtonPressed();
                break;
        }

        if (action != 0) {
            setChanged();
            notifyObservers(action);
        }
    };

    private EventHandler<KeyEvent> keyDownHandler = event -> {
        Integer action = getActionFromEvent(event);
        if (actionsToTrack.contains(action)) {
            actionsDown.put(action, true);
        }
        event.consume();
    };

    private EventHandler<KeyEvent> keyUpHandler = event -> {
        Integer action = getActionFromEvent(event);

        if (actionsToTrack.contains(action)) {
            actionsDown.put(action, false);
        }
        if (action == 100 || action == 101) {
            zoomDown = false;
        }
        event.consume();
    };

    KeyPressHandler() {

    }

    /**
     * Get an action code from a given event.
     * @param event The event.
     * @return The action code.
     */
    private Integer getActionFromEvent(KeyEvent event) {
        Integer action = 0;  // actions 100 or above signify actions that do not require boat action messages.
        switch (event.getCode()) {
            case PAGE_UP:  // Head upwind.
                action = 5;
                break;
            case PAGE_DOWN:  // Head downwind.
                action = 6;
                break;
            case UP:  // Head upwind.
                action = 5;
                break;
            case DOWN:  // Head downwind.
                action = 6;
                break;
            case LEFT:
                action = 7;
                break;
            case RIGHT:
                action = 8;
                break;
            case Z:  // Zoom in.
                action = 100;
                break;
            case X:  // Zoom out.
                action = 101;
                break;
        }
        return action;
    }

    /**
     * sets up the listening for button presses
     *
     * @param scene the scene that is going to up when listening for button presses
     */
    public void setup(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, genericKeyEventHandler);
        scene.setOnKeyPressed(keyDownHandler);
        scene.setOnKeyReleased(keyUpHandler);
        for (Integer action : actionsToTrack) {
            actionsDown.put(action, false);
        }
        TimerTask notifyKeyTask = new TimerTask() {
            @Override
            public void run() {
                for (Integer action : actionsDown.keySet()) {
                    if (actionsDown.get(action)) {
                        setChanged();
                        notifyObservers(action);
                    }
                }
            }
        };

        notifyKeyTimer = new Timer();
        notifyKeyTimer.purge();
        notifyKeyTimer.scheduleAtFixedRate(notifyKeyTask, 0, 30);
    }

    public void remove(Scene scene) {
        scene.removeEventFilter(KeyEvent.KEY_PRESSED, genericKeyEventHandler);
        scene.setOnKeyPressed(null);
        scene.setOnKeyReleased(null);
        if (notifyKeyTimer != null) {
            notifyKeyTimer.cancel();
            notifyKeyTimer.purge();
        }
    }
}
