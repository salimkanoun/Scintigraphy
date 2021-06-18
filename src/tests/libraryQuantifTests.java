package tests;

import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import ij.plugin.DICOM;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Library_Quantif}
 * 
 * @author Diego Rodriguez
 */

public class libraryQuantifTests {

    private DICOM dcm;
    private DICOM dcm2;
    private final String str = "Images/testImage.dcm";
    private final String str2 = "Images/testImage2.dcm";
    private InputStream is;
    private InputStream is2;
    private BufferedInputStream bis;
    private BufferedInputStream bis2;


    public static void main(String[] args){
        System.out.println("ici");
        libraryQuantifTests lib = new libraryQuantifTests();
        lib.setUp();
    }

    @BeforeEach
    public void setUp() {
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
    public void tearDown() {
        this.dcm = null;
        this.dcm2 = null;
    }

    @Test
    public void testRound() {
        double res1 = Library_Quantif.round(112.4d, 0);
        assertEquals(112, res1);

        double res2 = Library_Quantif.round(112.5d, 0);
        assertEquals(113, res2);

        double res3 = Library_Quantif.round(112.6d, 0);
        assertEquals(113, res3);
    }

    @Test
    public void testMoyGeom() {
        double res1 = Library_Quantif.moyGeom(2d, 2d);
        assertEquals(2, res1);

        double res2 = Library_Quantif.moyGeom(-4d, 2d);
        assertEquals(0, res2);
    }

    //This test is obvious but usefull to test if the image is well inserted
    @Test
    public void testGetHeight(){
        double res = this.dcm.getHeight();
        assertEquals(256, res);
    }

    @Test
    public void testGetCounts(){
        double res = Library_Quantif.getCounts(this.dcm);
        assertEquals(199862, res);
    }
    @Test
    public void testGetAvgCounts() {
        double res = Library_Quantif.getAvgCounts(this.dcm);
        res = (double) Math.round(res * 100) / 100;
        assertEquals(3.050, res);
    }

    @Test
    public void testGetPixelNumber(){
        double res = Library_Quantif.getPixelNumber(this.dcm);
        double nbPixels = this.dcm.getHeight() * this.dcm.getHeight();
        assertEquals(nbPixels, res);
    }

    //TODO : mock
    @Test
    public void testGetCountCorrectedBackground(){
      //  double res = Library_Quantif.getCountCorrectedBackground(this.dcm, roi, background)
    }

    @Test
    public void testCalculer_countCorrected(){
        Isotope is = Isotope.INDIUM_111;
        double res = Library_Quantif.calculer_countCorrected(10, 10000, is);
        assertEquals(10000.000286034412, res);
    }

    //TODO : same image
    @Test
    public void testCalculer_countCorrectedImages(){
        Isotope is = Isotope.INDIUM_111;
        double res = Library_Quantif.calculer_countCorrected(this.dcm, this.dcm2, is);
        assertEquals(199862, res);
    }

    @Test
    public void testApplyDecayFraction(){
        Isotope is = Isotope.INDIUM_111;
        double res = Library_Quantif.applyDecayFraction(10, 10000, is);
        assertEquals(9999.999713965595,res);
    }

    //TODO : same image
    @Test
    public void testApplyDecayFractionImage(){
        Isotope is = Isotope.INDIUM_111;
        double res = Library_Quantif.applyDecayFraction(this.dcm, this.dcm2, is);
        assertEquals(199862, res);
    }

    @Test
    public void testCalculateDeltaTime(){
        Date d1 = new Date(1588864900000L);
        Date d2 = new Date(1588864960000L);
        
        double res = Library_Quantif.calculateDeltaTime(d1, d2);
        assertEquals(1, res);
    }
}