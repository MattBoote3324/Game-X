package seng302;

import javafx.scene.layout.Pane;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dki27 on 30/08/17.
 */
public class SelectionWheelTest {
    Pane emptyPane = new Pane();
    SelectionWheel sw = new SelectionWheel(emptyPane, 300, 300, 6, null);

    //Test was here but Ci runner has issues running graphical things

    //https://docs.google.com/spreadsheets/d/1_Z8NVCtHGMdOZ_LngfqaCljZmfUvg4mLwiK_L0eP6MM/edit#gid=1812899115

//    @Test
//    public void initialiseTest(){
//        Assert.assertNotNull(sw.getSelected());
//    }

    //Manual Test in google doc to make sure selection increments on scroll wheel
}
