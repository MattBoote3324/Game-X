package seng302.Common;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Course is a domain model class that stores details about the course of a race
 */
public class Course {
    private List<CourseFeature> courseFeatures;
    private List<Point> boundaryPoints;
    private Map<Integer, CourseCheckPoint> courseOrder;
    private int featureAmount; // The amount of features a boat must pass (including start and finish).
    private String courseName;

    public Course(List<CourseFeature> courseFeatures, List<Point> boundaryPoints, Map<Integer, CourseCheckPoint> courseOrder) {
        this.courseFeatures = courseFeatures;
        this.boundaryPoints = boundaryPoints;
        this.courseOrder = courseOrder;
        this.featureAmount = courseOrder.size();
    }

    /**
     * Get a feature point by its Associated ID
     * @param id ID associated with feature point
     * @return courseFeature object
     */
    public CourseFeature getCourseFeatureById(int id) {
        for (CourseFeature courseFeature : courseFeatures) {
            if (courseFeature.getId() == id) {
                return courseFeature;

            }
        }
        return null;
    }

    /**
     * Get a course from a given path to a course XML file.
     *
     * @param xmlPath The path to a course XML file.
     * @return A fully initialised course object.
     */
    public static Course getCourseFromXmlPath(String xmlPath) {
        Course course = null;
        try {
            XMLParser xmlParser = new XMLParser(xmlPath);
            xmlParser.parseCourse();
            course = new Course(xmlParser.getCourseFeatures(), xmlParser.getBoundaryPoints(), xmlParser.getCourseOrder());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            System.err.println("Error initialising XMLParser from path: " + xmlPath);
            e.printStackTrace();
        }
        return course;
    }

    ////////////////////////////////////////////
    // Only getters and setters from here on. //
    ////////////////////////////////////////////
    public List<CourseFeature> getCourseFeatures() {
        return courseFeatures;
    }

    public Map<Integer, CourseCheckPoint> getCourseOrder() {
        return courseOrder;
    }

    public List<Point> getBoundaryPoints() {
        return boundaryPoints;
    }

    public double getCourseLatBasedOnPoints(){

        return getBoundaryPoints().get(0).getLatitude();
    }
    public double getCourseLongBasedOnPoints(){

        return getBoundaryPoints().get(0).getLongitude();
    }
    public int getFeatureAmount() {
        return featureAmount;
    }

    public String getName() {
        return courseName;
    }
}
