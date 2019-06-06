package API;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by Matthew Boote on 11/04/2017.
 */
public class GoogleTimeZone {


        private String apiKey;
        private String time_zone_name;
        private String previousAddress = "";
        private float raw_offset;
        private float dls_offset;
        public GoogleTimeZone(double lat,double longitude)
        {
            HttpClient httpclient = new DefaultHttpClient();

            HttpGet httpget = new HttpGet("https://maps.googleapis.com/maps/api/timezone/xml?location=" + lat + "," + longitude + "&timestamp=1331161200&key=AIzaSyDSdU-7lPrIjNkhTp9JSwKbR7HX1rMjTBU");
            HttpResponse response = null;
            try {
                response = httpclient.execute(httpget);
            } catch (IOException e) {
                e.printStackTrace();
            }
            HttpEntity entity = response.getEntity();
            Document doc = null;


                //String content = EntityUtils.toString(entity);

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    doc = builder.parse(entity.getContent());




                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (org.xml.sax.SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName("TimeZoneResponse");

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);


                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        raw_offset = Float.parseFloat(eElement.getElementsByTagName("raw_offset").item(0).getTextContent());
                        dls_offset = Float.parseFloat(eElement.getElementsByTagName("dst_offset").item(0).getTextContent());
                        time_zone_name = eElement.getElementsByTagName("time_zone_name").item(0).getTextContent();
                    }

                }
        }


    public String getTime_zone_name() {
       return time_zone_name;
    }

    public void setTime_zone_name(String time_zone_name) {
        this.time_zone_name = time_zone_name;
    }

    public float getRaw_offset() {
        return raw_offset;
    }

    public void setRaw_offset(float raw_offset) {
        this.raw_offset = raw_offset;
    }

    public float getDls_offset() {
        return dls_offset;
    }

    public void setDls_offset(float dls_offset) {
        this.dls_offset = dls_offset;
    }
}
