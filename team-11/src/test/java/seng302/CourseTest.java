package seng302;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import seng302.Common.Course;
import seng302.Common.XMLParser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

/**
 * Test Course.
 */
public class CourseTest {

    private Course course;

    @Before
    public void setup() {
        // Initialize course.
        String pathToXml = "/xml/Race.xml";
        try {
            XMLParser xmlParser = new XMLParser(pathToXml);
            xmlParser.parseCourse();
            course = new Course(xmlParser.getCourseFeatures(), xmlParser.getBoundaryPoints(), xmlParser.getCourseOrder());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void temporaryTest() {
        int i = course.getCourseFeatures().size();
        assertEquals(i == i, true);
    }
}
