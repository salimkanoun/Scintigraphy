package tests;

import ij.plugin.DICOM;
import org.easymock.EasyMock;

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
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongOrientationException;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

/**
 * Test class for {@link Library_Quantif}
 * 
 * @author Diego Rodriguez
 */

public class libraryDicomTests {

    private DICOM dcm;
    private DICOM dcm2;
    private final String str = "Images/testImage.dcm";
    private final String str2 = "Images/poumonsReins.dcm";
    private InputStream is;
    private InputStream is2;
    private BufferedInputStream bis;
    private BufferedInputStream bis2;

    public static void main(String[] args) {

        libraryDicomTests dic = new libraryDicomTests();
        dic.setUp();
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
    public void isMultiFrameTest() {
        boolean res = Library_Dicom.isMultiFrame(this.dcm);
        assertFalse(res);
    }

    @Test
    public void splitDynamicAntPostTest() throws WrongOrientationException, IllegalArgumentException, ReadTagException {
        ImageState state = new ImageState(Orientation.ANT, 0, true, 0);
        ImageSelection ims = state.getImage();
        ims.setImagePlus(this.dcm);  
        ImageSelection[] imsT = Library_Dicom.splitDynamicAntPost(ims);

        assertEquals(2, imsT.length);

        boolean ant = imsT[0].getImageOrientation().equals(Orientation.ANT);
        boolean post = imsT[1].getImageOrientation().equals(Orientation.POST);

       assertTrue(ant);
       assertTrue(post);
    }
/*
    @Test
    public void sortImageAntPostTest() throws ReadTagException {
        ImagePlus imp = Library_Dicom.sortImageAntPost(this.dcm2);
        String tag = DicomTools.getTag(imp, "0011,1012");
        System.out.println(tag);
        System.out.println(DicomTools.getTag(this.dcm2, "0011,1012"));
        assertEquals(this.dcm2, imp);
    }*/

    @Test
    public void getFrameDurationTest(){
        int res = Library_Dicom.getFrameDuration(this.dcm);
        assertEquals(167961, res);
    }

    //TODO : pas testable ?
    /*@Test
    public void flipStackHorizontalTest(){
        String[] str1 = {"un"};
        String[] str2 = {"deux"};
        ImageSelection ims = new ImageSelection(this.dcm, str1, str2);
        StackProcessor sp = new StackProcessor(ims.getImagePlus().getImageStack());
        Library_Dicom.flipStackHorizontal(ims);
        StackProcessor sp2 = new StackProcessor(ims.getImagePlus().getImageStack());
        assertEquals(sp.toString(), sp2.toString());
    }*/

    @Test
    public void buildFrameDurationsTest(){
        int[] res = Library_Dicom.buildFrameDurations(this.dcm);
        int[] waited = {Library_Dicom.getFrameDuration(this.dcm)};
        assertEquals(waited[0], res[0]);
    }

    @Test
    public void projectTest(){
        String[] str1 = {"un"};
        String[] str2 = {"deux"};
        ImageSelection ims = new ImageSelection(this.dcm, str1, str2);
        ImageSelection res = Library_Dicom.project(ims, 1, 2 ,"Max Intensity");
        assertEquals("MAX_Test image", res.getImagePlus().getTitle());
    }

    @Test
    public void ensureAntPostTest() throws WrongOrientationException {

       // String[] str1 = {"un"};
       // String[] str2 = {"deux"};
        
        ImageSelection mockImageSelection = EasyMock.createMock(ImageSelection.class);
        EasyMock.expect(mockImageSelection.getImageOrientation()).andReturn(Orientation.POST_ANT);
        EasyMock.replay(mockImageSelection);
        //ImageSelection ims = new ImageSelection(this.dcm2, str1, str2);  
        ImageSelection res = Library_Dicom.ensureAntPost(mockImageSelection);
        Orientation waited = Orientation.ANT_POST;
        assertEquals(waited, res.getImageOrientation());
    }
}