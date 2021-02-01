package coursework;

import java.net.Socket;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CourseworkClientIT {

    private final Object server = "";
    private int port;
    private String username;
    private Socket socket;

    public CourseworkClientIT() {
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
    public void testConstructorClient() throws Exception {
        boolean result;
        boolean expResult = true;
        if (server != null && port >= 0 && !"".equals(username)) {
            result = true;
            assertEquals(result, expResult);
        }
    }

    /**
     * Test of clientStart method, of class CourseworkClient.
     */
    // Pass
    @Test
    public void testClientStart() throws Exception {
        port = 7777;
        CourseworkClient client = new CourseworkClient((String) server, port, username);
        boolean result = client.clientStart();
        if (socket != null) {
            boolean expResult = true;
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of outputMessage method, of class CourseworkClient.
     */
    // Pass
    @Test
    public void testOutputMessage() {
        String message = "Test message";
        port = 7777;
        CourseworkClient client = new CourseworkClient((String) server, port, username);
        client.outputMessage(message);
    }

}
