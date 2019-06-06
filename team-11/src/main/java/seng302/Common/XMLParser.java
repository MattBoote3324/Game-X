package seng302.Common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses an XML document into parts
 */
public class XMLParser {
    private Document document;

    // Course attributes.
    private List<CourseFeature> courseFeatures = new ArrayList<>();
    private List<Point> boundaryPoints = new ArrayList<>();
    private Map<Integer, CourseCheckPoint> courseOrder = new HashMap<>();

    private List<Boat> boatList;


    private List<String> participants;

    // Regatta attributes.
    private int regattaID;
    private String regattaName;
    private String courseName;
    private Double centralLatitude;
    private Double centralLongitude;
    private Double centralAltitude;
    private int utc;
    private Double magVar;

    /**
     * Construct an XMLParser, taking a path to an XML file.
     *
     * @param pathToXML The path to an XML file.
     *
     * @throws ParserConfigurationException In case of error.
     * @throws IOException In case of error.
     * @throws org.xml.sax.SAXException In case of error.
     */
    public XMLParser(String pathToXML) throws ParserConfigurationException, IOException, SAXException {
        InputStream inputStream = InputStream.class.getResourceAsStream(pathToXML);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(inputStream);
        document.getDocumentElement().normalize();
    }

    /**
     * Construct an XMLParser, taking an input stream of an XML.
     *
     * @param xmlInputStream The XML input stream.
     *
     * @throws ParserConfigurationException In case of error.
     * @throws IOException In case of error.
     * @throws org.xml.sax.SAXException In case of error.
     */
    public XMLParser(InputStream xmlInputStream) throws ParserConfigurationException, IOException, SAXException {
//        xmlInputStream.reset();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(xmlInputStream);
        document.getDocumentElement().normalize();
    }

    /**
     * Parse the information needed to construct a Course object.
     */
    public void parseCourse() {
        // Build courseFeatures.
        parseCourseCompoundMarks();

        // Build boundaryPoints.
        parseCourseBoundaryPoints();

        // Build courseOrder.
        parseCourseOrder();

        // Build participants.
        parseParticipants();
    }

    /**
     * Build courseFeatures from CompoundMark XML elements.
     */
    private void parseCourseCompoundMarks() {
        NodeList compoundMarks = document.getElementsByTagName("CompoundMark");
        for (int i = 0; i < compoundMarks.getLength(); i++) {
            Element compoundMark = (Element) compoundMarks.item(i);
            CourseFeature courseFeature = createCourseFeature(compoundMark);
            if (courseFeature != null) {
                courseFeatures.add(courseFeature);
            }
        }
    }

    /**
     * Create a CourseFeature from a given CompoundMark element.
     *
     * @param compoundMark The XML element containing the compound mark information
     * @return A CourseFeature based on the given compound mark information.
     */
    private CourseFeature createCourseFeature(Element compoundMark) {
        int courseFeatureID = Integer.parseInt(compoundMark.getAttributes().getNamedItem("CompoundMarkID").getTextContent());
        String name = compoundMark.getAttributes().getNamedItem("Name").getTextContent();
        NodeList markList = compoundMark.getElementsByTagName("Mark");
        if (markList.getLength() == 1) {
            // Then the Course Feature is a mark.
            int sourceID = Integer.parseInt(markList.item(0).getAttributes().getNamedItem("SourceID").getTextContent());
            double latitude = Double.parseDouble(markList.item(0).getAttributes().getNamedItem("TargetLat").getTextContent());
            double longitude = Double.parseDouble(markList.item(0).getAttributes().getNamedItem("TargetLng").getTextContent());
            return new CourseFeature(name, courseFeatureID, FeatureType.MARK, new Point(latitude, longitude, sourceID));
        } else if (markList.getLength() == 2) {
            // Then the Course Feature is either a gate, a start line, or a finish line.
            int sourceID1 = Integer.parseInt(markList.item(0).getAttributes().getNamedItem("SourceID").getTextContent());
            int sourceID2 = Integer.parseInt(markList.item(1).getAttributes().getNamedItem("SourceID").getTextContent());
            double latitude1 = Double.parseDouble(markList.item(0).getAttributes().getNamedItem("TargetLat").getTextContent());
            double latitude2 = Double.parseDouble(markList.item(1).getAttributes().getNamedItem("TargetLat").getTextContent());
            double longitude1 = Double.parseDouble(markList.item(0).getAttributes().getNamedItem("TargetLng").getTextContent());
            double longitude2 = Double.parseDouble(markList.item(1).getAttributes().getNamedItem("TargetLng").getTextContent());
            FeatureType featureType = FeatureType.GATE;
            if (name.toLowerCase().contains("start")) {
                featureType = FeatureType.START;
            } else if (name.toLowerCase().contains("finish")) {
                featureType = FeatureType.FINISH;
            }
            return new CourseFeature(name, courseFeatureID, featureType, new Point(latitude1, longitude1, sourceID1), new Point(latitude2, longitude2, sourceID2));
        }
        return null;
    }

    /**
     * Build boundaryPoints from Limit XML elements.
     */
    private void parseCourseBoundaryPoints() {
        NodeList limitList = document.getElementsByTagName("Limit");
        for (int i = 0; i < limitList.getLength(); i++) {
            Element element = (Element) limitList.item(i);
            Double latitude = Double.parseDouble(element.getAttributes().getNamedItem("Lat").getTextContent());
            Double longitude = Double.parseDouble(element.getAttributes().getNamedItem("Lon").getTextContent());
            boundaryPoints.add(new Point(latitude, longitude));
        }
    }

    /**
     * Build courseOrder from Corner XML elements.
     */
    private void parseCourseOrder() {
        NodeList cornerList = document.getElementsByTagName("Corner");
        for (int i = 0; i < cornerList.getLength(); i++) {
            Element element = (Element) cornerList.item(i);
            int seqNumber = Integer.parseInt(element.getAttributes().getNamedItem("SeqID").getTextContent());
            int courseFeatureID = Integer.parseInt(element.getAttributes().getNamedItem("CompoundMarkID").getTextContent());
            String rounding = element.getAttributes().getNamedItem("Rounding").getTextContent();
            int zoneSize = Integer.parseInt(element.getAttributes().getNamedItem("CompoundMarkID").getTextContent());
            courseOrder.put(seqNumber, new CourseCheckPoint(courseFeatureID, rounding, zoneSize));
            // This is just here for the mean time, to make the boats go past marks,
            for (CourseFeature feature : courseFeatures) {
                if (feature.getId() == seqNumber) {
                    feature.setRounding(rounding);
                }
            }
        }
    }

    /**
     * Parses regatta XML message and sets variables to their values
     */
    public void parseRegattaXML(){
        regattaName = document.getElementsByTagName("RegattaName").item(0).getChildNodes().item(0).getNodeValue();
        regattaID = Integer.parseInt( document.getElementsByTagName("RegattaID").item(0).getChildNodes().item(0).getNodeValue());
        courseName = document.getElementsByTagName("CourseName").item(0).getChildNodes().item(0).getNodeValue();
        centralLatitude = Double.parseDouble(document.getElementsByTagName("CentralLatitude").item(0).getChildNodes().item(0).getNodeValue());
        centralLongitude = Double.parseDouble(document.getElementsByTagName("CentralLongitude").item(0).getChildNodes().item(0).getNodeValue());
        centralAltitude = Double.parseDouble(document.getElementsByTagName("CentralAltitude").item(0).getChildNodes().item(0).getNodeValue());
        utc = Integer.parseInt(document.getElementsByTagName("UtcOffset").item(0).getChildNodes().item(0).getNodeValue());
        magVar = Double.parseDouble(document.getElementsByTagName("MagneticVariation").item(0).getChildNodes().item(0).getNodeValue());

    }

    /**
     * Parses the Participants from the Race XML
     */
    private void parseParticipants(){
        NodeList participantsNodeList = document.getElementsByTagName("Yacht");
        participants = new ArrayList<>();
        for (int i = 0; i < participantsNodeList.getLength(); i++) {
            Element e = (Element) participantsNodeList.item(i);
            participants.add(e.getAttributes().getNamedItem("SourceID").getTextContent());
        }
    }

    /**
     * Parse each boat in a boat list
     */
    public void parseForBoatList() {
        // Get the boat tag
        NodeList boatNodeList = document.getElementsByTagName("Boat");
        boatList = new ArrayList<>();
        // Iterate over the list
        for (int i = 0; i < boatNodeList.getLength(); i++) {
           // Node nNode = boatNodeList.item(i);
            Element e = (Element) boatNodeList.item(i);
            String name = e.getAttributes().getNamedItem("BoatName").getTextContent();
//                String abbrev = e.getAttributes().getNamedItem("ShortName").getTextContent();
            int sourceID = Integer.parseInt(e.getAttributes().getNamedItem("SourceID").getTextContent());
            Boat b = new Boat(sourceID, name, name);
            b.resetTimeSinceLastMark();
            if(e.getAttributes().getNamedItem("BoatGod") != null){
                String godType = e.getAttributes().getNamedItem("BoatGod").getTextContent();
                b.setGreekGod(GodType.values()[Integer.valueOf(godType)].getGod()); //sets the clients boat to know its greek god.
            } else {
                b.setGreekGod(GodType.values()[0].getGod());
            }
            boatList.add(b);
        }
    }
    ////////////////////////////////////////////
    // Only getters and setters from here on. //
    ////////////////////////////////////////////
    public List<CourseFeature> getCourseFeatures() {
        return courseFeatures;
    }

    public List<Point> getBoundaryPoints() {
        return boundaryPoints;
    }

    public Map<Integer, CourseCheckPoint> getCourseOrder() {
        return courseOrder;
    }

    public List<Boat> getBoatList() {
        return boatList;
    }

    public int getRegattaID() {
        return regattaID;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public String getCourseName() {
        return courseName;
    }

    public Double getCentralLatitude() {
        return centralLatitude;
    }

    public Double getCentralLongitude() {
        return centralLongitude;
    }

    public Double getCentralAltitude() {
        return centralAltitude;
    }

    public int getUtc() {
        return utc;
    }

    public Double getMagVar() {
        return magVar;
    }


}
