package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import ij.IJ;
import ij.ImagePlus;

public class IsotopeTest {
	
	ImagePlus imp;

	@Before
	public void setUp() throws Exception {
		imp = IJ.openImage(
				"E:\\noaTMP\\Stage Oncopole\\Shunt_Po\\Shunt_Po\\Shunt_Po Shunt_Po\\Gastric\\static\\1\\ANT00h001_DS.dcm");
	}

	@After
	public void tearDown() throws Exception {
		imp = null;
	}

	@Test
	public void testFindIsotopeInInfoProperty() {
		assertEquals(Isotope.TECHNICIUM_99, Library_Dicom.findIsotope(imp));
	}

	@Test
	public void testFindIsotopeWhenInfoIsNull() {
		ImagePlus imp = new ImagePlus();
		assertNull(Library_Dicom.findIsotope(imp));
	}

}
