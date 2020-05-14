package tests;

import ij.plugin.DICOM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

/**
 * Test class for {@link Libreary_Quantif}
 * 
 * @author Diego Rodriguez
 */

public class libraryDicomTests {

    private DICOM dcm;
    private DICOM dcm2;
    private String str = "Images/testImage.dcm";
    private String str2 = "Images/testImage2.dcm";
    private InputStream is;
    private InputStream is2;
    private BufferedInputStream bis;
    private BufferedInputStream bis2;

    public static void main(String[] args) {
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
    public void getDateAcquisitionTest() throws ParseException {
        Date res = Library_Dicom.getDateAcquisition(this.dcm);
        String dateString = "20160411153436";
        SimpleDateFormat parser = new SimpleDateFormat("yyyyMMddHHmmss");
        Date waited = parser.parse(dateString);
        assertEquals(waited, res);
    }

    @Test
    public void isSameCameraMultiFrameTest() throws ReadTagException {
        boolean res = Library_Dicom.isSameCameraMultiFrame(this.dcm);
        assertTrue(res);
    }

    @Test
    public void isAnteriorTest() throws ReadTagException {
        boolean res = Library_Dicom.isAnterior(this.dcm);
        assertTrue(res);
    }

    @Test
    public void isMultiFrameTest(){
        boolean res = Library_Dicom.isMultiFrame(this.dcm);
        assertFalse(res);
    }

    @Test
    public void splitDynamicAntPostTest(){
        ImageSelection[] ims = Library_Dicom.splitDynamicAntPost(this.dcm);
    }
}