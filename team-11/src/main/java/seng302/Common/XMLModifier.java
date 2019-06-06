package seng302.Common;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Created by tho63 on 8/08/17
 */
public class XMLModifier {
    private static Document boatDoc;
    //Elements of the boat config XML shared between generating and adding
    private static Element boats;
    private Element rootElement;
    private static StringWriter stringWriter;

    public XMLModifier() {
        generateBoatConfigXML();
    }

    /**
     * Generates the XML string of the boats.
     **/
    private void generateBoatConfigXML() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = docFactory.newDocumentBuilder();

            boatDoc = documentBuilder.newDocument();
            rootElement = boatDoc.createElement("BoatConfig");
            boatDoc.appendChild(rootElement);

            boats = boatDoc.createElement("Boats");
            rootElement.appendChild(boats);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new boat to the boat config XML
     *
     * @param newBoat The new boat being added to the XML
     */
    public synchronized void addBoat(Boat newBoat) {
        Element boat = boatDoc.createElement("Boat");

        Attr type = boatDoc.createAttribute("Type");
        type.setValue("Yacht");
        boat.setAttributeNode(type);

        Attr sourceID = boatDoc.createAttribute("SourceID");
        sourceID.setValue(String.valueOf(newBoat.getSourceID()));
        boat.setAttributeNode(sourceID);

        Attr stoweName = boatDoc.createAttribute("StoweName");
        stoweName.setValue(newBoat.getAbbreviatedName());
        boat.setAttributeNode(stoweName);

//                Attr shortName = boatDoc.createAttribute("ShortName");
//                shortName.setValue(newBoat.getMedia());
//                boat.setAttributeNode(shortName);

        Attr boatName = boatDoc.createAttribute("BoatName");
        boatName.setValue(newBoat.getName());
        boat.setAttributeNode(boatName);

        Attr boatGod = boatDoc.createAttribute("BoatGod");
        if (newBoat.getGreekGod() == null){
            newBoat.setGreekGod(new GodZeus());
            boatGod.setValue("0");
        } else{
            boatGod.setValue(Integer.toString(newBoat.getGreekGod().getGodType().getType()));
        }


        boat.setAttributeNode(boatGod);

        boats.appendChild(boat);

    }

    /**
     * Writes the XML into a string
     *
     * @return A String of the XML Structure
     */
    public String getStringWriter() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(boatDoc);
            StreamResult result = new StreamResult(stringWriter = new StringWriter());
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return stringWriter.toString();
    }


    /**
     * Remove a boat from the XML Document structure, matches on the boats source id
     *
     * @param boat Boat that is to be removed from the XML structure
     */
    public void removeBoat(Boat boat) {
        NodeList targetList = rootElement.getElementsByTagName("Boat");
        // Search thru the nodes to find the one we want
        for (int i = 0; i < targetList.getLength(); i++) {
            int xmlSourceID = Integer.parseInt(targetList.item(i).getAttributes().getNamedItem("SourceID").getNodeValue());
            if (xmlSourceID == boat.getSourceID()) {
                // Get that nodes parent
                Node parent = targetList.item(i).getParentNode();
                // Remove the target node
                parent.removeChild(targetList.item(i));
                break;
            }
        }
    }
}



