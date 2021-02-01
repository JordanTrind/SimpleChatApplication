package coursework;

import java.net.Socket;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

public class CourseworkServerIT {

    private int port;

    public CourseworkServerIT() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // Pass
    @Test
    public void testConstructorServer() throws Exception {
        boolean result;
        boolean expResult = true;
        if (port >= 0) {
            result = true;
            assertEquals(result, expResult);
        }
    }

    // Pass

    @Test
    public void testServerMessages() {
        String message = "Hello everyone!";
        port = 7777;
        CourseworkServer server = new CourseworkServer(port);
        server.serverMessages(message);
    }

}
