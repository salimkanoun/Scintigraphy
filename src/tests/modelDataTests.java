package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.model.Data;

/**
 * Test class for {@link Data}
 * 
 * @author Diego Rodriguez
 */

public class modelDataTests {

    Data data;
    ImageState state = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, 0);

    @BeforeEach
    public void setUp() throws Exception{
        this.data = new Data(state, 100);
    }

    @AfterEach
    public void tearDown() throws Exception{
        this.data = null;
    }

    @Test
    public void testGetMinutes(){
        assertEquals(100, this.data.getMinutes());
    }

    @Test
    public void testSetMinutes(){
        this.data.setTime(150);
        assertEquals(150, this.data.getMinutes());
    }
}