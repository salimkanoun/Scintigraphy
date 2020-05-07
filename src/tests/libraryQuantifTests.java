package tests;

import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.plugin.DICOM;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    @BeforeEach
    public void setUp() {
        this.is = this.getClass().getResourceAsStream(this.str);
        this.bis = new BufferedInputStream(this.is);
        this.dcm = new DICOM(this.bis);
        this.dcm.show();
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

    }
}