package coursework;

import java.io.Serializable;

/* Component class (can be reused with different command messages)
 determines the type of message that is typed in the console
 there are 4 command messages and 1 for all the other messages
 if the message is not one of these commands - it is treated as a normal message 
 */
public class ConsoleMessage implements Serializable {

    protected static final long serialVersionUID = 7777L;
    //command messages
    static final int online = 0, stringMessage = 1, logout = 2, commands = 3, coordinator = 4;
    private final int type;
    private final String message;

    ConsoleMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    int getType() {
        return type;
    }

    String getMessage() {
        return message;
    }
}
