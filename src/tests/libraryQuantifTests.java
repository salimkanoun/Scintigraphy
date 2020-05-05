package tests;

import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Libreary_Quantif}
 * 
 * @author Diego Rodriguez
 */

public class libraryQuantifTests {

    private Opener open = new Opener();
    private ImagePlus imp;

    @BeforeEach
    public void setUp(){
      //  String myPicture = ImageIO.read(getClass().getResource("testImage.dcm"));
       // this.imp = myPicture;

        this.imp = IJ.openImage("testImage.dcm");
       // this.imp = this.open.openCachedImage(myPicture);
    
    }

    @AfterEach
    public void tearDown(){
        this.imp = null;
    }


    @Test
    public void testRound(){
        double res1 = Library_Quantif.round(112.4d, 0);
        assertEquals(112, res1);

        double res2 = Library_Quantif.round(112.5d,0);
        assertEquals(113, res2);

        double res3 = Library_Quantif.round(112.6d,0);
        assertEquals(113, res3);
    }

    @Test
    public void testMoyGeom(){
        double res1 = Library_Quantif.moyGeom(2d, 2d);
        assertEquals(2, res1);

        double res2 = Library_Quantif.moyGeom(-4d, 2d);
        assertEquals(0, res2);
    }

    @Test
    public void testGetAvgCounts(){
        double res = Library_Quantif.getAvgCounts(this.imp);

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