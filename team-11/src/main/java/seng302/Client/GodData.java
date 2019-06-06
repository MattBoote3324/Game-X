package seng302.Client;

/**
 * Created by mbo57 on 11/09/17.
 */
public class GodData {
    private String image_location;
    private String name;
    private String desc;

    public GodData(String name,String desc, String image_location){
        this.name = name;
        this.desc = desc;
        this.image_location = image_location;
    }
    public String getImage_location() {
        return image_location;
    }


    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
