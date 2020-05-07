package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.model.Data;

import ij.ImagePlus;

/**
 * Test class for {@link Data}
 * 
 * @author Diego Rodriguez
 */

public class chronologicalAcquisitionComparatorTests {
    ChronologicalAcquisitionComparator cac;
    ImagePlus imp1;
    ImagePlus imp2;

    @BeforeEach
    public void setUp() throws Exception{
        this.cac = new ChronologicalAcquisitionComparator();
        this.imp1 = new ImagePlus();
        this.imp2 = new ImagePlus();
    }

    @AfterEach
    public void tearDown() throws Exception{
        this.cac = null;
        this.imp1 = null;
        this.imp2 = null;
    }

    @Test
    public void compareImageSelection(){
       // this.imp.set*
    }
}