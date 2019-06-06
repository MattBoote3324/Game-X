package seng302.Common;

import java.io.InputStream;

/**
 * Created by rph65 on 11/05/17.
 * Regatta class contains the attributes of a Regatta.
 */
public class Regatta extends java.util.Observable {
    private int regattaID;
    private String regattaName;
    private String courseName;
    private Double centralLatitude;
    private Double centralLongitude;
    private Double centralAltitude;
    private int utc;
    private Double magVar;

    public Regatta(int regattaID, String regattaName, String courseName, Double centralLatitude, Double centralLongitude, Double centralAltitude, int utc, Double magVar) {
        this.regattaID = regattaID;
        this.regattaName = regattaName;
        this.courseName = courseName;
        this.centralLatitude = centralLatitude;
        this.centralLongitude = centralLongitude;
        this.centralAltitude = centralAltitude;
        this.utc = utc;
        this.magVar = magVar;
    }

    public Regatta(InputStream regattaXML){
        try {
            XMLParser parser = new XMLParser(regattaXML);
            parser.parseRegattaXML();
            this.regattaID = parser.getRegattaID();
            this.regattaName = parser.getRegattaName();
            this.courseName = parser.getCourseName();
            this.centralLatitude = parser.getCentralLatitude();
            this.centralLongitude = parser.getCentralLongitude();
            this.centralAltitude = parser.getCentralAltitude();
            this.utc = parser.getUtc();
            this.magVar = parser.getMagVar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Getters and Setters



    public String getRegattaName() {
        return regattaName;
    }



}
