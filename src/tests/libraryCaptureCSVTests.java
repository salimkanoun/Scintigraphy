package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.plugin.DICOM;
import loci.plugins.util.LibraryChecker.Library;



/**
 * Test class for {@link Data}
 * 
 * @author Diego Rodriguez
 */

public class libraryCaptureCSVTests {

    private DICOM dcm;
    private DICOM dcm2;
    private String str = "Images/testImage.dcm";
    private String str2 = "Images/testImage2.dcm";
    private InputStream is;
    private InputStream is2;
    private BufferedInputStream bis;
    private BufferedInputStream bis2;
    
    public static void main(String[] args){
        libraryCaptureCSVTests csv = new libraryCaptureCSVTests();
        csv.setUp();
    }

    @BeforeEach
    public void setUp(){
        this.is = this.getClass().getResourceAsStream(this.str);
        this.bis = new BufferedInputStream(this.is);
        this.dcm = new DICOM(this.bis);
        this.dcm.run("Test image");
        this.dcm.show();

        this.is2 = this.getClass().getResourceAsStream(this.str2);
        this.bis2 = new BufferedInputStream(this.is2);
        this.dcm2 = new DICOM(this.bis2);
        this.dcm2.run("Test image2");
        this.dcm2.show();
    }

    @AfterEach
    public void tearDown(){
        this.dcm = null;
        this.dcm2 = null;
    }

    @Test
    public void getPatientInfoTest(){
        HashMap<String, String> res = new HashMap<>();
        res = Library_Capture_CSV.getPatientInfo(this.dcm);

        HashMap<String, String> resVisible = new HashMap<>();
        resVisible.put("name", "Rios Nathalie");
        resVisible.put("date", "04/11/2016");
        resVisible.put("id", "A24368761859");
        resVisible.put("accessionNumber", "A24333093149");
        assertEquals(resVisible, res);
    }

    //TODO : problème avec l'uid ?? En enlevant l'histoire du substring, l'erreur part. L'erreur provient probablement ud code existant
    @Test
    public void getTagPartie1Test(){
        HashMap<String, String> resVisible = new HashMap<>();
        resVisible.put("name", "Rios Nathalie");
        resVisible.put("date", "04/11/2016");
        resVisible.put("id", "A24368761859");
        resVisible.put("accessionNumber", "A24333093149");
        Random random = new Random();
        String uid = Integer.toString(random.nextInt(1000000));
        String res = Library_Capture_CSV.getTagPartie1(resVisible, "Thyroide tc externe", uid);
        assertEquals(1, res);
    }
}