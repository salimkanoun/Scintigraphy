package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedInputStream;

import java.io.InputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator.ImagePlusComparator;

import ij.plugin.DICOM;
import org.petctviewer.scintigraphy.scin.model.Data;


/**
 * Test class for {@link Data}
 * 
 * @author Diego Rodriguez
 */

public class chronologicalAcquisitionComparatorTests {

    private DICOM dcm;
    private DICOM dcm2;
    private final String str = "Images/testImage.dcm";
    private final String str2 = "Images/testImage2.dcm";
    private InputStream is;
    private InputStream is2;
    private BufferedInputStream bis;
    private BufferedInputStream bis2;

    final ChronologicalAcquisitionComparator chr = new ChronologicalAcquisitionComparator();
    final ImagePlusComparator impc = new ImagePlusComparator();

    
    public static void main(String[] args){
        chronologicalAcquisitionComparatorTests chr = new chronologicalAcquisitionComparatorTests();
        chr.setUp();
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
    public void compareImageSelection(){
        String[] str1 = {"un"};
        String[] str2 = {"deux"};
       ImageSelection is = new ImageSelection(this.dcm, str1, str2);
       ImageSelection is2 = new ImageSelection(this.dcm2, str1, str2);
       double res = this.chr.compare(is, is2);
       assertEquals(0, res);
    }

    @Test
    public void compareImagePlus(){
        double res = this.impc.compare(this.dcm, this.dcm2);
        assertEquals(0, res);
    }
}