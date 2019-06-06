package seng302;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import seng302.Client.Controllers.CourseController;
import seng302.Common.Course;
import seng302.Common.XMLParser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class CourseControllerTest {

    private CourseController courseController;
    private Course course;

    @Before
    public void setup() throws IOException, SAXException, ParserConfigurationException {
        XMLParser xmlParser = new XMLParser("/xml/TestRace.xml");
        xmlParser.parseCourse();
        course = new Course(xmlParser.getCourseFeatures(), xmlParser.getBoundaryPoints(), xmlParser.getCourseOrder());
        courseController = new CourseController(course, 200.0, 200.0);
        courseController.addCartesianToCourseFeatures();
    }

    @Test
    public void testCourseDimensions() {
        Assert.assertEquals(0.0, courseController.getLonXDifference(), 0.00001);
        Assert.assertEquals(12.0, courseController.getLatYDifference(), 0.00001);
    }

    @Test
    public void testScaleGeoToCartesian() {
        Assert.assertEquals(16.5, courseController.getScaleGeoToCartesian(), 0.001);
    }

    @Test
    public void testCourseCartesian() {
        Assert.assertEquals(17.5, course.getCourseFeatureById(1).getPoint1().getX(), 0.001);
        Assert.assertEquals(182.5, course.getCourseFeatureById(1).getPoint1().getY(), 0.001);
        Assert.assertEquals(34.0, course.getCourseFeatureById(1).getPoint2().getX(), 0.001);
        Assert.assertEquals(182.5, course.getCourseFeatureById(1).getPoint2().getY(), 0.001);

        Assert.assertEquals(100, course.getCourseFeatureById(2).getPoint1().getX(), 0.001);
        Assert.assertEquals(100, course.getCourseFeatureById(2).getPoint1().getY(), 0.001);

        Assert.assertEquals(182.5, course.getCourseFeatureById(3).getPoint1().getX(), 0.001);
        Assert.assertEquals(17.5, course.getCourseFeatureById(3).getPoint1().getY(), 0.001);
        Assert.assertEquals(182.5, course.getCourseFeatureById(3).getPoint2().getX(), 0.001);
        Assert.assertEquals(34.0, course.getCourseFeatureById(3).getPoint2().getY(), 0.001);
    }
}