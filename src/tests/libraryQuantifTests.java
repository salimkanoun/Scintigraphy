package tests;

import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import ij.plugin.DICOM;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Libreary_Quantif}
 * 
 * @author Diego Rodriguez
 */

public class libraryQuantifTests {

    private DICOM dcm;
    private String str = "Images/testImage.dcm";
    private InputStream is;
    private BufferedInputStream bis;

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
        this.dcm.run("open");
<<<<<<< HEAD
=======
        System.out.println(this.dcm.getInfoProperty());
        System.out.println(this.dcm.getHeight());
        this.dcm.show();
>>>>>>> dfee0b844fc018d1981a12cfdf5c63a21fb2d1cd
    }

    @AfterEach
    public void tearDown() {
        this.dcm = null;
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

    @Test
    public void testGetAvgCounts() throws IOException {
        //double res = Library_Quantif.getAvgCounts(this.dcm);
        double res = this.dcm.getHeight();
        assertEquals(100, res);


    }

    @Test
    public void testGetPixelNumber(){

    }

    @Test
    public void testGetCountCorrectedBackground(){

    }

    @Test
    public void testCalculer_countCorrected(){
        Isotope is = Isotope.INDIUM_111;
        double res = Library_Quantif.calculer_countCorrected(10, 10000, is);
        assertEquals(10000.000286034412, res);
    }

    @Test
    public void testApplyDecayFraction(){
        Isotope is = Isotope.INDIUM_111;
        double res = Library_Quantif.applyDecayFraction(10, 10000, is);
        assertEquals(9999.999713965595,res);
    }

    @Test
    public void testCalculateDeltaTime(){
        Date d1 = new Date(1588864900000l);
        Date d2 = new Date(1588864960000l);
        
        double res = Library_Quantif.calculateDeltaTime(d1, d2);
        assertEquals(1, res);
    }
}