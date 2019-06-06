package seng302;

import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import seng302.Client.Controllers.JoinGameController;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;


/**
 * Nifty selection wheel that allows the user to choose from a graphical piechart
 * Created by David on 28/08/2017.
 */
public class SelectionWheel {
    private double HEIGHT;
    private double WIDTH;
    private double RADIUS;
    private double SCROLL_SPEED = 0.2;

    private ObjectProperty<Arc> clickedArc = new SimpleObjectProperty<>();
    private ObjectProperty<ImageView> clickedImage = new SimpleObjectProperty<>();

    private Canvas canvas = new Canvas();
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private Pane pane;
    private int divisions;

    private ArrayList<Arc> arcList = new ArrayList();
    private int selected = -1;
    private ArrayList<Color> arcColor = new ArrayList();
    private List<ImageView> imageList = new ArrayList<>();

    private Circle circle;
    private List images;
    private JoinGameController joinGameController;

    private double dimensionToScaleFrom;

    public SelectionWheel(Pane pane, double width, double height, int divisions, List<ImageView> imageList) {
        this.pane = pane;
        this.divisions = divisions;
        this.HEIGHT = height;
        this.WIDTH = width;
        this.imageList = imageList;

        dimensionToScaleFrom = min(WIDTH, HEIGHT);
        RADIUS = (dimensionToScaleFrom / 2.5);

        initiateListeners();
        createArcs();
        if (imageList != null) {
            createImages();
        }
        createCenterSelection();
    }

    /**
     * Sets up handlers for when the wheel is clicked or scrolled on
     */
    private void initiateListeners() {
//        clickedArc.addListener((obs, oldSelection, newSelection) -> {
//            if (newSelection != null) {
//                handleSelection(arcList.indexOf(newSelection));
//            }
//        });
        clickedImage.addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                int pos = imageList.indexOf(newSelection);
                handleSelection(pos);

            }
        });

//        mouseArc.addListener((obs, oldSelection, newSelection) -> {
//            expandArcRadius(arcList.indexOf(newSelection));
//        });

        pane.setOnScroll(event -> {
            rotateSelection(event.getDeltaY());
        });


    }

    /**
     * Increments/decrements the selection based on a mouseScroll
     * and updates graphically
     *
     * @param deltaY
     */
    private void rotateSelection(double deltaY) {
        //Adjust based on scroll up or down
        int selection = selected;
        if (deltaY < 0) {
            selection += 1;
        } else {
            selection -= 1;
        }

        //Handle the upper and lower bounds
        if (selection < 0) {
            selection = arcList.size() - 1;
        }
        if (selection >= arcList.size()) {
            selection = 0;
        }

        //Expand after changing selection
        handleSelection(selection);
    }

    /**
     * Creates a circle shape in the centre to display the selected colour
     */
    private void createCenterSelection() {
        circle = new Circle();

        circle.setCenterX(WIDTH / 2);
        circle.setCenterY(HEIGHT / 2 - HEIGHT * 0.05);
        circle.setRadius(RADIUS / 3);
        circle.setFill(Color.TRANSPARENT);

        pane.getChildren().add(circle);
    }

    /**
     * Changes the colour of the middle of the circle
     *
     * @param color the colour to set
     */
    private void updateCircle(Color color) {
        circle.setFill(color);
    }

    /**
     * Divides a circle up into the specified amount of segments and creates arc segments for each of them
     * Also sets up the listeners for each segment while creating them
     */
    private void createArcs() {
//        ArrayList<Color> colours = new ArrayList<>(Arrays.asList(Color.PURPLE, Color.TEAL, Color.SANDYBROWN, Color.DARKOLIVEGREEN, Color.ORANGERED, Color.LIGHTBLUE, Color.BLUE, Color.LIGHTBLUE, Color.BLUE, Color.LIGHTBLUE));

        double arcExtent = 360 / divisions;

        for (int i = 0; i < divisions; i++) {
            gc.setFill(Color.TRANSPARENT);
            double start = arcExtent * i;

            Arc arc = new Arc();
            arc.setCenterX(WIDTH / 2);
            arc.setCenterY(HEIGHT / 2 - HEIGHT * 0.05);
            arc.setRadiusX(RADIUS);
            arc.setRadiusY(RADIUS);
            arc.setStartAngle(90 - start);
            arc.setLength(-arcExtent);
            arc.setType(ArcType.ROUND);
            arc.setFill(Color.TRANSPARENT);

            arc.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> clickedArc.set(arc));
            arcList.add(arc);
            arcColor.add(Color.TRANSPARENT);

            pane.getChildren().add(arc);
        }
    }

    /**
     * Builds a list of image views and attaches listeners from a passed in list of images
     * Sets the image view in the middle of its corresponding arc segment
     */
    private void createImages() {
        // Image img = new Image("images/annotate.png");

        double segment = 360 / divisions;

        for (int i = 0; i < divisions; i++) {
            ImageView imgView = imageList.get(i);
//            ImageView imgView = new ImageView();
            dimensionToScaleFrom = min(WIDTH, HEIGHT);
            imgView.setFitHeight(dimensionToScaleFrom / 3.5);
            imgView.setFitWidth(dimensionToScaleFrom / 3.5);

            setImageViewCoords(i, imgView, RADIUS);

            imgView.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                clickedImage.set(imgView);
            });

            imageList.add(imgView);

            pane.getChildren().add(imgView);
        }
    }


    /**
     * updates the middle circle and expands the selected arc statement
     *
     * @param selection the integer index of the chosen segment
     */
    private void handleSelection(int selection) {
        selected = selection;
        updateCircle(arcColor.get(selection));
        expandArcRadius(selection);
        joinGameController.updateTextDetails(selection);

    }

    /**
     * Expands the selected arc radius by 30 to show that it has been selected
     *
     * @param selection the integer index of the chosen segment
     */
    private void expandArcRadius(int selection) {
        for (Arc a : arcList) {
            a.setRadiusX(RADIUS);
            a.setRadiusY(RADIUS);
        }

        final int sel = selection;

        Transition expand = new Transition() {
            {
                setCycleDuration(Duration.millis(100));
            }

            @Override
            protected void interpolate(double frac) {
                if (sel == selected) { //makes sure we havent changed the selection halfway through animation....
                    double newRadius = RADIUS + frac * 30;
                    arcList.get(sel).setRadiusX(newRadius);
                    arcList.get(sel).setRadiusY(newRadius);
                    dimensionToScaleFrom = min(WIDTH, HEIGHT);
                    double newSize = dimensionToScaleFrom / 3.5 + frac * 30;
                    imageList.get(sel).setFitHeight(newSize);
                    imageList.get(sel).setFitWidth(newSize);
                    setImageViewCoords(sel, imageList.get(sel), RADIUS + frac * 30);
                }
            }
        };
        expand.play();

        for (int i = 0; i < arcList.size(); i++) {
            if (i != selection) {
                arcList.get(i).setRadiusY(RADIUS);
                arcList.get(i).setRadiusX(RADIUS);
                dimensionToScaleFrom = min(WIDTH, HEIGHT);
                imageList.get(i).setFitHeight(dimensionToScaleFrom / 3.5);
                imageList.get(i).setFitWidth(dimensionToScaleFrom / 3.5);
                setImageViewCoords(i, imageList.get(i), RADIUS);
            }
        }
    }

    /**
     * Sets the coordinates of the imageview to be in the middle of a segment
     * based on the segments geometry and the hwight and width of the imageview
     *
     * @param i       the integer index of the chosen segment
     * @param imgView the imageview itself to be placed
     */
    private void setImageViewCoords(int i, ImageView imgView, double radius) {
        //Coords
        double cX = WIDTH / 2;
        double cY = HEIGHT / 2 - HEIGHT * 0.05;

        double theta = -(arcList.get(i).getStartAngle() + arcList.get(i).getLength() / 2);
        double rX = cX + radius * (cos(theta * Math.PI / 180));
        double rY = cY + radius * (sin(theta * Math.PI / 180));

        double cSegX = (rX + cX) / 2;
        double cSegY = (rY + cY) / 2;

        //Can be used to change the pos of the image by editing the / 2
        cSegX = cX + ((rX - cX) / 1.5);
        cSegY = cY + ((rY - cY) / 1.5);

        imgView.setX(cSegX - (imgView.getFitWidth() / 2));
        imgView.setY(cSegY - (imgView.getFitHeight() / 2));
    }

    public void setImages(List images) {
        this.images = images;
    }

    /**
     * Returns the users choice
     *
     * @return the integer index of the chosen segment
     */
    public int getSelected() {
        return selected;
    }

    public void setController(JoinGameController jc){
        joinGameController = jc;
    }

}
