package coursework;

import java.net.*;
import java.io.*;
import java.util.*;

public class CourseworkClient {

    // read/write from the socket
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Socket socket;
    private final String server;
    private final String username;
    private final int port;

    CourseworkClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    //<editor-fold defaultstate="collapsed" desc="main Method">
    public static void main(String[] args) throws IOException {
        int portNumber = 7777;
        try {
            InetAddress IPAddress = InetAddress.getLocalHost();
            System.out.println("The default IP Address for this server is: " + "[" + IPAddress + "]");
        } catch (UnknownHostException uhe) {
            System.out.println("Cannot identify localhost address" + uhe);
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter your username: ");
        String[] consoleValues = bufferedReader.readLine().split(" ");
        String IPAddress = null;
        CourseworkClient client = new CourseworkClient(IPAddress, portNumber, consoleValues[0]);
        if (!client.clientStart()) {
            return;
        }
        System.out.println("To view the command list for this network type: 'commandlist' ");
        Scanner scanner = new Scanner(System.in); // wait for messages from user
        while (true) { // loop to determine the messages coming from each user
            System.out.print("> ");
            String message = scanner.nextLine();
            if (message.equalsIgnoreCase("logout")) {
                client.sendMessageToServer(new ConsoleMessage(ConsoleMessage.logout, ""));
                break;
            } else if (message.equalsIgnoreCase("online")) {
                client.sendMessageToServer(new ConsoleMessage(ConsoleMessage.online, ""));
            } else if (message.equalsIgnoreCase("commandlist")) {
                client.sendMessageToServer(new ConsoleMessage(ConsoleMessage.commands, ""));
            } else if (message.equalsIgnoreCase("coordinator")) {
                client.sendMessageToServer(new ConsoleMessage(ConsoleMessage.coordinator, ""));
            } else {
                client.sendMessageToServer(new ConsoleMessage(ConsoleMessage.stringMessage, message));
            }
        }
        client.clientDisconnect(); // to disconnect when the break statement is triggered (when a client wants to logout)
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="clientStart Method">
    public boolean clientStart() throws UnknownHostException {
        try {
            socket = new Socket(server, port);
        } catch (Exception e) {
            outputMessage("There is an error connectiong to server: " + e);
            return false;
        }
        String message = "The connection is accepted on port: " + "[" + socket.getPort() + "]";
        outputMessage(message);

        try { //create both I/O streams
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ioe) {
            outputMessage("Exception in creating new Input/Output streams: " + ioe);
            return false;
        }
        //<editor-fold defaultstate="collapsed" desc="Login process">
        // for sending the initial username to the server
        new CourseworkServerThreadListener().start();
        try {
            output.writeObject(username);
        } catch (IOException ioe) {
            outputMessage("Exception while logging in: " + ioe);
            clientDisconnect();
            return false; // cannot login
        }
//</editor-fold>
        return true; // successfull
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="sendMessageToServer Method">
    void sendMessageToServer(ConsoleMessage message) {
        try {
            output.writeObject(message);
        } catch (IOException ioe) {
            outputMessage("Exception occured when writing to the server: " + ioe);
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="outputMessage Method">
    void outputMessage(String message) { // to output a message in the console
        System.out.println(message);
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="clientDisconnect Method">
    private void clientDisconnect() { // method to close both streams and disconnect in something occurs
        try {
            if (input != null) {
                input.close();
            }
        } catch (Exception e) {
            System.out.println("Input stream cannot be closed due to exception: " + e);
        }
        try {
            if (output != null) {
                output.close();
            }
        } catch (Exception e) {
            System.out.println("Output stream cannot be closed due to exception: " + e);
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            System.out.println("Socket cannot be closed due to exception: " + e);
        }
    }
//</editor-fold>

    class CourseworkServerThreadListener extends Thread { // thread class to listen to the server for messages and output it to console

        @Override
        public void run() {
            while (true) {
                try {
                    String message = (String) input.readObject();
                    System.out.println(message);
                    System.out.print("> ");
                } catch (IOException ioe) {
                    outputMessage("You have logged out of the server successfully with the exception: " + ioe);
                    break;
                } catch (ClassNotFoundException cnfe) {
                    System.out.println("The class wasn't found: " + cnfe);
                }
            }
        }
    }
}
