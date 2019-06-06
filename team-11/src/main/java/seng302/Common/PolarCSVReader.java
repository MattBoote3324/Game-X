package seng302.Common;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rph65 on 23/05/17
 */
public class PolarCSVReader {
    private HashMap<Double, HashMap<Double, Double>> polarMap = new HashMap();
    private HashMap<Double, HashMap<Double, Double>> upwindSpeed = new HashMap();
    private HashMap<Double, HashMap<Double, Double>> downwindSpeed = new HashMap();
    private HashMap<Double, Double> upwindAngle = new HashMap<>();
    private HashMap<Double, Double> downwindAngle = new HashMap<>();

    /**
     * @param fileName the location of the file that we are going to use to load in the polars.
     *  Constructor method that takes the fileName and reads in the Polar.csv file. It then parses
     *  the data and puts it in the polarMap.
     */

    public PolarCSVReader(String fileName) {
        BufferedReader br;
        String line;
        String[] columns;
        List<String[]> fileText = new ArrayList<>();
//        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        InputStream is = getClass().getResourceAsStream(fileName);

        try{
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                columns = (line.split(","));
                fileText.add(columns);
            }



            for(int row = 1; row <= 8; row++){
                HashMap<Double, Double> columnMap = new HashMap<>();
                HashMap<Double, Double> upwindColumnMap = new HashMap<>();
                HashMap<Double, Double> downwindColumnMap = new HashMap<>();
                for(int column = 1; column < fileText.get(row).length; column+= 2){
                    columnMap.put(Double.parseDouble(fileText.get(row)[column]), Double.parseDouble(fileText.get(row)[column + 1]));
                    if(column == 5){
                        upwindColumnMap.put(Double.parseDouble(fileText.get(row)[column]), Double.parseDouble(fileText.get(row)[column + 1]));
                        upwindAngle.put(Double.parseDouble(fileText.get(row)[0]), Double.parseDouble(fileText.get(row)[column]));
                        upwindSpeed.put(Double.parseDouble(fileText.get(row)[0]), upwindColumnMap);
                    }else if(column == 17){
                        downwindColumnMap.put(Double.parseDouble(fileText.get(row)[column]), Double.parseDouble(fileText.get(row)[column + 1]));
                        downwindAngle.put(Double.parseDouble(fileText.get(row)[0]), Double.parseDouble(fileText.get(row)[column]));
                        downwindSpeed.put(Double.parseDouble(fileText.get(row)[0]), downwindColumnMap);
                    }
                }
                polarMap.put(Double.parseDouble(fileText.get(row)[0]), columnMap);
            }

        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Double, HashMap<Double, Double>> getPolarMap() {
        return polarMap;
    }

    public HashMap<Double, Double> getUpwindAngle() {
        return upwindAngle;
    }

    public HashMap<Double, Double> getDownwindAngle() { return downwindAngle; }

    public HashMap<Double, HashMap<Double, Double>> getUpwindSpeed() {
        return upwindSpeed;
    }

    public HashMap<Double, HashMap<Double, Double>> getDownwindSpeed() { return downwindSpeed; }
}
