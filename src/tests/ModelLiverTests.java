package tests;

import org.petctviewer.scintigraphy.hepatic.radioEmbolization.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.junit.jupiter.api.Assertions;
/**
 * Test class for {@link ModelLiver}
 * 
 * @author Diego Rodriguez
 */

public class ModelLiverTests {

	ModelLiver model;
	
	@BeforeEach
	public void setUp() {
		this.model = new ModelLiver(new ImageSelection[0], "LiverTest");
	}

	@AfterEach
	public void tearDown() {
	this.model = null;
	}

    @Test
    public void calculateResultsTest(){
        Assertions.assertEquals(1,1);
	}
}