package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petctviewer.scintigraphy.hepatic.radioEmbolization.ModelLiver;
import org.petctviewer.scintigraphy.hepatic.scintivol.Model_Scintivol;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link ModelLiver}
 * 
 * @author Diego Rodriguez
 */

public class ModelScintivolTests {

	Model_Scintivol model;
	
	@BeforeEach
	public void setUp() throws Exception {
		this.model = new Model_Scintivol(new ImageSelection[0], "ScintivolTest", new int[0], null) {
			@Override
			public void setCounts(int sliceT1, int sliceT2) {
				Map<String, Double> liver = new HashMap<>();
				Map<String, Double> heart = new HashMap<>();
				Map<String, Double> fov = new HashMap<>();
				Map<String, Double> tomo = new HashMap<>();
				this.getResults().put("Liver", liver);
				this.getResults().put("Heart", heart);
				this.getResults().put("FOV", fov);
				this.getResults().put("Tomo", tomo);

				liver.put("t1", 24310.0);
				liver.put("t2", 33431.0);

				heart.put("t1", 1990.0);
				heart.put("t2", 1115.0);
				heart.put("AUC", 22986.0);

				fov.put("t1", 38648.0);
				fov.put("t2", 42601.0);

				tomo.put("FT", 8649578.0);
				tomo.put("FFR", 4819190.0);

				this.setSize(165);
				this.setWeight(73);
			}
		};
	}

	@AfterEach
	public void tearDown() throws Exception {
		this.model = null;
	}

    @Test
    public void calculateResultsTest(){
		this.model.calculateResults();
		assertEquals(23328, Library_Quantif.round(this.model.getResults().get("Other").get("BP Activity"), 0));
		assertEquals(1.83, Library_Quantif.round(this.model.getResults().get("Other").get("SC"), 2));
		assertEquals(0.56, Library_Quantif.round(this.model.getResults().get("Other").get("Cnorm_t2"), 2));
		assertEquals(11.55, Library_Quantif.round(this.model.getResults().get("Other").get("AUC/Cnorm"), 2));
		assertEquals(20.31, Library_Quantif.round(this.model.getResults().get("Other").get("Clairance FT"), 2));
		assertEquals(11.10, Library_Quantif.round(this.model.getResults().get("Other").get("Norm Clairance FT"), 2));
		assertEquals(0.557, Library_Quantif.round(this.model.getResults().get("Other").get("FFR/FT"), 3));;
		assertEquals(11.32, Library_Quantif.round(this.model.getResults().get("Other").get("Clairance FFR"), 2));
		assertEquals(6.2, Library_Quantif.round(this.model.getResults().get("Other").get("Norm Clairance FFR"), 1));
	}
}