package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.petctviewer.scintigraphy.scin.library.Library_Debug;

/**
 * Test class for {@link Data}
 * 
 * @author Diego Rodriguez
 */

public class libraryDebugTests {
    

    public static void main(String[] args){
        libraryDebugTests ld = new libraryDebugTests();
        ld.setUp();
    }

    @BeforeEach
    public void setUp(){
    }

    @AfterEach
    public void tearDown(){

    }

    @Test
    public void separatorTest(){
        String res = Library_Debug.separator(2,2);
        String waited = ".oOo\n.oOo";
        assertEquals(waited, res);

        String res2 = Library_Debug.separator(4,5);
        String waited2 = ".oOo\n.oOo\n.oOo\n.oOo\n.oOo";
        assertEquals(waited2, res2);
    }

    @Test
    public void separator60Test(){
        String res = Library_Debug.separator();
        String waited = ".oOo.oOo.oOo.oOo.oOo.oOo.oOo.oOo.oOo.oOo.oOo.oOo.oOo.oOo.oOo";
        assertEquals(waited, res);
    }

    @Test
    public void replaceNullTest(){
        String res = Library_Debug.replaceNull("");
        String waited = "N/A";
        assertEquals(waited, res);
    }

    @Test
    public void preventNullTest(){
        String s = null;
        String res = Library_Debug.preventNull(s);
        String waited = "";
        assertEquals(waited, res);

        String res2 = Library_Debug.preventNull("test");
        String waited2 = "test";
        assertEquals(waited2, res2);
    }
}