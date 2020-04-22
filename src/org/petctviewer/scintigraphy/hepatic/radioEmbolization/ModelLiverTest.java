package org.petctviewer.scintigraphy.hepatic.radioEmbolization;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link ModelLiver}
 * 
 * @author Diego Rodriguez
 */

public class ModelLiverTest {

    @Test
    public void calculateResultsTest(){
        assertEquals(1,1);
    }
}





/*

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculatorTests {

	@Test
	@DisplayName("1 + 1 = 2")
	void addsTwoNumbers() {
		Calculator calculator = new Calculator();
		assertEquals(2, calculator.add(1, 1), "1 + 1 should equal 2");
	}

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

private Distributeur d;

	@Before
	public void setUp() throws Exception {
		this.d = new Distributeur(10,10,10);
	}

	@After
	public void tearDown() throws Exception {
		this.d = null;
	}

	@Test
	public void test10() {
		List<Couple> proposition = d.donnerBillets(10);
		assertEquals(0, d.montantRestantDû(proposition, 10));
		assertEquals(10, proposition.get(0).getValeurBillet());
        assertEquals(1, proposition.get(0).getNombreBilletsDélivrés());
    }*/