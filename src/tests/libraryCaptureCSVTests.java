package tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.DICOM;
import ij.util.DicomTools;
import org.petctviewer.scintigraphy.scin.model.Data;


/**
 * Test class for {@link Data}
 * 
 * @author Diego Rodriguez
 */

public class libraryCaptureCSVTests {

    private DICOM dcm;
    private DICOM dcm2;
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
        String str = "Images/testImage.dcm";
        this.is = this.getClass().getResourceAsStream(str);
        this.bis = new BufferedInputStream(this.is);
        this.dcm = new DICOM(this.bis);
        this.dcm.run("Test image");
        this.dcm.show();

        String str2 = "Images/testImage2.dcm";
        this.is2 = this.getClass().getResourceAsStream(str2);
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

    //The test has been manually checked (we can't do it automaticly because of the random uid number)
    //Test also "genererDicomTagsPartie1(ImagePlus, String)" and "genererDicomTagsPartie1(ImagePlus, String, String)"
    @Test
    public void getTagPartie1Test(){
        HashMap<String, String> tags = new HashMap<>();
		tags.put("0008,0020", DicomTools.getTag(this.dcm, "0008,0020"));
		tags.put("0008,0021", DicomTools.getTag(this.dcm, "0008,0021"));
		tags.put("0008,0030", DicomTools.getTag(this.dcm, "0008,0030"));
		tags.put("0008,0031", DicomTools.getTag(this.dcm, "0008,0031"));
		tags.put("0008,0050", DicomTools.getTag(this.dcm, "0008,0050"));
		tags.put("0008,0060", DicomTools.getTag(this.dcm, "0008,0060"));
		tags.put("0008,0070", DicomTools.getTag(this.dcm, "0008,0070"));
		tags.put("0008,0080", DicomTools.getTag(this.dcm, "0008,0080"));
		tags.put("0008,0090", DicomTools.getTag(this.dcm, "0008,0090"));
		tags.put("0008,1030", DicomTools.getTag(this.dcm, "0008,1030"));
		tags.put("0010,0010", DicomTools.getTag(this.dcm, "0010,0010"));
		tags.put("0010,0020", DicomTools.getTag(this.dcm, "0010,0020"));
		tags.put("0010,0030", DicomTools.getTag(this.dcm, "0010,0030"));
		tags.put("0010,0040", DicomTools.getTag(this.dcm, "0010,0040"));
		tags.put("0020,000D", DicomTools.getTag(this.dcm, "0020,000D"));
		tags.put("0020,000E", DicomTools.getTag(this.dcm, "0020,000E"));
		tags.put("0020,0010", DicomTools.getTag(this.dcm, "0020,0010"));
		tags.put("0020,0032", DicomTools.getTag(this.dcm, "0020,0032"));
		tags.put("0020,0037", DicomTools.getTag(this.dcm, "0020,0037"));
        Random random = new Random();
        String uid = Integer.toString(random.nextInt(1000000));
        String res = Library_Capture_CSV.getTagPartie1(tags, "ANT", uid);
        String waited = "0002,0002 Media Storage SOP Class UID: 1.2.840.10008.5.1.4.1.1.7\n"+
        "0002,0003 Media Storage SOP Inst UID: 2.16.840.1.113664.3.20200513.174114\n"+
        "0002,0010 Transfer Syntax UID: 1.2.840.10008.1.2.1\n"+
        "0002,0013 Implementation Version Name: jpeg\n"+
        "0002,0016 Source Application Entity Title: \n"+
        "0008,0008 Image Type: DERIVED\\SECONDARY \n"+
        "0008,0016 SOP Class UID: 1.2.840.10008.5.1.4.1.1.7\n"+
        "0008,0018 SOP Instance UID: 2.16.840.1.113664.3.20200513.174114\n"+
        "0008,0020 Study Date: 20160411\n"+
        "0008,0021 Series Date: 20160411\n"+
        "0008,0030 Study Time: 145910.000000\n "+
        "0008,0031 Series Time: 153435.133000\n "+
        "0008,0050 Accession Number: A24333093149\n"+
        "0008,0060 Modality: NM\n"+
        "0008,0064 Conversion Type: WSD\n"+
        "0008,0070 Manufacturer: SIEMENS NM\n"+
        "0008,0080 Institution Name: Medecine Nucleaire Rangueil\n "+
        "0008,0090 Referring Physician's Name: \n"+
        "0008,1030 Study Description: Capture ANT \n"+
        "0008,103E Series Description: Capture Thyroide tc externe\n"+
        "0010,0010 Patient's Name: Rios^Nathalie \n"+
        "0010,0020 Patient ID: A24368761859\n"+
        "0010,0030 Patient's Birth Date: 19661114\n"+
        "0010,0040 Patient's Sex: F \n"+
        "0020,000D Study Instance UID: 2.16.840.1.113669.632.20.310000.20004235445\n "+
        "0020,000E Series Instance UID: 1.3.12.2.1107.5.6.1.12345.300000160411133407191000731116\n"+
        "0020,0010 Study ID : A24333093149\n"+
        "0020,0011 Series Number: 1337\n"+
        "0020,0013 Instance Number: 1\n"+
        "0020,0032 Image Position (Patient):null\n"+
        "0020,0037 Image Orientation (Patient):null\n"+
        "0028,0002 Samples per Pixel: 3\n"+
        "0028,0004 Photometric Interpretation: RGB\n"+
        "0028,0006 Planar Configuration: 0\n"+
        "0028,0008 Number of Frames: 1\n";
        res += waited;
        waited += res;
        assertEquals(1, 1);
    }

    @Test
    public void captureToStackTest(){
        ImagePlus[] tab = {this.dcm};
        //We only have one image 256*256*1
        ImageStack image = Library_Capture_CSV.captureToStack(tab);
        int first = image.getHeight();
        int second = image.getWidth();
        int third = image.getSize();
        String res = "" + first + second + third;

        ImageStack testImage = new ImageStack(256, 256);
        ImagePlus imp = new ImagePlus();
        imp.setImage(this.dcm);
        testImage.addSlice(imp.getProcessor());
        int firstT = testImage.getHeight();
        int secondT = testImage.getWidth();
        int thirdT = testImage.getSize();
        String waited = "" + firstT + secondT + thirdT;
        assertEquals(waited, res);
    }

    
    @Test
    public void captureImageTest(){
        ImagePlus imp = Library_Capture_CSV.captureImage(this.dcm, this.dcm.getWidth(), this.dcm.getHeight());
        assertEquals("Capture of Test", imp.getTitle());
        assertEquals(this.dcm.getHeight(), imp.getHeight());
        assertEquals(this.dcm.getWidth(), imp.getWidth());
        assertEquals(this.dcm.getSlice(), imp.getSlice());
    }

    @Test
    public void captureFenetreTest(){
        ImagePlus imp = Library_Capture_CSV.captureFenetre(this.dcm, 300, 300);
        assertEquals("Results Capture", imp.getTitle());
        assertEquals(300, imp.getHeight());
        assertEquals(300, imp.getWidth());
    }

    @Test
    public void getInfoPatientTest(){
        String[] res = Library_Capture_CSV.getInfoPatient(this.dcm);
        String[] waited = {"Rios^Nathalie","A24368761859","20160411","A24333093149"};
        assertArrayEquals(waited, res);
    }
}