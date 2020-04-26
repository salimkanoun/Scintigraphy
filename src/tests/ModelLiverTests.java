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

//Run test ??
	public static void main(String[] args){
		System.out.println("Debut des tests");
	}
	ModelLiver model;
	
	@BeforeEach
	public void setUp() throws Exception {
		this.model = new ModelLiver(new ImageSelection[0], "LiverTest");
	}

	@AfterEach
	public void tearDown() throws Exception {
	this.model = null;
	}

    @Test
    public void calculateResultsTest(){
        Assertions.assertEquals(1,0);
	}
}


/*

class CalculatorTests {

	@ParameterizedTest(name = "{0} + {1} = {2}")
	@CsvSource({
			"0,    1,   1",
			"1,    2,   3",
			"49,  51, 100",
			"1,  100, 101"
	})
	void add(int first, int second, int expectedResult) {
		Calculator calculator = new Calculator();
		assertEquals(expectedResult, calculator.add(first, second),
				() -> first + " + " + second + " should equal " + expectedResult);
	}
}
*/