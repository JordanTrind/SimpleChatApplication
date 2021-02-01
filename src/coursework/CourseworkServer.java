package coursework;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CourseworkServer {

    private static int clientID; // ID for each member
    private final ArrayList<ClientThread> arrayOfThreads; // array of client threads
    private final SimpleDateFormat dateFormat;
    private final int port;
    private boolean state; // state of the server (on/off)

    //<editor-fold defaultstate="collapsed" desc="Server constructor">
// server constructor that will receive the port for connection in the console
    public CourseworkServer(int port) {
        this.port = port;
        dateFormat = new SimpleDateFormat("HH:mm:ss"); // for recording
        arrayOfThreads = new ArrayList<>();  // new array of threads
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="main Method">
    public static void main(String[] args) {
        int portNumber = 7777;
        try { // to output IP of the machine used
            InetAddress IPAddress = InetAddress.getLocalHost();
            System.out.println("The default IP Address for this server is: " + "[" + IPAddress + "]");
        } catch (UnknownHostException uhe) {
            System.out.println("Cannot identify local host address" + uhe);
        }
        // start server with given port 7777
        CourseworkServer server = new CourseworkServer(portNumber);
        server.serverStart();
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="serverStart Method">
    public void serverStart() { // key logic for running server
        state = true;
        try {
            ServerSocket serverSocket = new ServerSocket(port); // new server with port
            serverMessages("The webchat is now online and is waiting for its members on port: " + "[" + port + "]"); // opening message
            while (state) { // waiting for connection
                Socket socket = serverSocket.accept(); // accept connection
                if (!state) { // if server stopped - break from the loop and continue down
                    break;
                }
                ClientThread clientThreadInstance = new ClientThread(socket);  // new thread of it
                arrayOfThreads.add(clientThreadInstance); // save it in the arraylist
                clientThreadInstance.start(); // run the client code
            }
            try { // to close the server
                serverSocket.close(); // close the server
                for (int i = 0; i < arrayOfThreads.size(); ++i) {
                    ClientThread clientThreadInstance = arrayOfThreads.get(i); // make a thread out of all members
                    try {
                        // close both streams and the socket
                        clientThreadInstance.input.close();
                        clientThreadInstance.output.close();
                        clientThreadInstance.socket.close();
                    } catch (IOException ioe) {
                        System.out.println("Cannot close the input/output streams with exception: " + ioe);
                    }
                }
            } catch (Exception e) {
                serverMessages("The chat had to be closed due to unforseen exception: " + e);
            }
        } catch (IOException ioe) {
            String message = dateFormat.format(new Date()) + " The chat was closed due to exception: " + ioe + "\n";
            serverMessages(message);
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="serverMessage Method">
    void serverMessages(String message) { // method to broadcast the server messages
        String fullmessage = dateFormat.format(new Date()) + " > " + message;
        System.out.println(fullmessage);
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="broadcast Method">
    private synchronized void broadcastMessage(String message) { // to send each message to all clients
        String messageFinal = dateFormat.format(new Date()) + " " + message + "\n"; // actual message sent + the date
        System.out.print(messageFinal);
        for (int i = arrayOfThreads.size(); --i >= 0;) {// reverse loop to check if we need to remove a disconnected user
            ClientThread clientThreadInstance = arrayOfThreads.get(i);
            if (!clientThreadInstance.writeMessage(messageFinal)) { // try to "ping" the user - if he can't get the message - delete him from the list of users
                arrayOfThreads.remove(i);
                serverMessages("The user [" + clientThreadInstance.username + "]" + " is disconnected and has been removed from the user list.");
            }
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="remove Method">
    synchronized void remove(int ID) { // to remove a user from the userlist (arraylist)
        for (int i = 0; i < arrayOfThreads.size(); ++i) { // scan the array list until we found the Id
            ClientThread clientThreadInstance = arrayOfThreads.get(i);
            if (clientThreadInstance.id == ID) {
                arrayOfThreads.remove(i);
                return;
            }
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="ClientThread Class">
    class ClientThread extends Thread { // Thread class for each client 

        ObjectInputStream input;
        ObjectOutputStream output;
        Socket socket;
        String date;
        String username;
        int id;
        ConsoleMessage consoleMessage;
        Boolean coordinatorRole = false;

        //<editor-fold defaultstate="collapsed" desc="ClientThread constructor">
        ClientThread(Socket socket) { // Constructor for the class
            id = clientID++;
            this.socket = socket;
            try {
                // creating both streams of data (input/output)
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                username = (String) input.readObject(); // read the username
                for (int i = 0; i < arrayOfThreads.size(); ++i) { // to check if username is unique
                    if (arrayOfThreads.get(i).username.equals(username)) {
                        username = (username + id);
                        writeMessage("Username automatically changed as not unique, new username is: " + "[" + username + "]");
                        serverMessages(username + " has had username changed.");
                    }
                }
                serverMessages(username + " has just connected to the chat.");
            } catch (IOException ioe) {
                serverMessages("Exception creating new data streams: " + ioe);
                return;
            } catch (ClassNotFoundException cnfe) {
                serverMessages("The class can't be found: " + cnfe);
            }
            date = new Date().toString() + "\n";
        }
//</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="run Method">
        @Override
        public void run() { // key logic for Thread class
            boolean state = true;
            while (state) { // while loop to run while boolean = true (server is on)
                try { // try to read the message which is an object of string
                    consoleMessage = (ConsoleMessage) input.readObject();
                } catch (IOException ioe) {
                    serverMessages(username + "Exception reading data streams: " + ioe);
                    break;
                } catch (ClassNotFoundException cnfe) {
                    System.out.println("The class wasn't found: " + cnfe);
                    break;
                }
                if (arrayOfThreads.get(0).coordinatorRole == false) {
                    arrayOfThreads.get(0).coordinatorRole = true;
                    serverMessages(arrayOfThreads.get(0).username + " set as new coordinator with id: " + arrayOfThreads.get(0).id);
                }
                String message = consoleMessage.getMessage();
                // Switch on the type of message receive
                switch (consoleMessage.getType()) {
                    case ConsoleMessage.stringMessage:
                        broadcastMessage(username + ": " + message);
                        break;
                    case ConsoleMessage.commands:
                        writeMessage("LIST OF ALL COMMANDS:");
                        writeMessage("1. 'commandlist' - to check the list of commands ");
                        writeMessage("2. 'logout' - to disconnect from the chat ");
                        writeMessage("3. 'online' - to check who is currently online in the server ");
                        writeMessage("4. 'coordinator' - to check who is the coordinator");
                        break;
                    case ConsoleMessage.logout:
                        serverMessages(username + " disconnected from the chat.");
                        state = false;
                        break;
                    case ConsoleMessage.online:
                        if (coordinatorRole == true) {
                            writeMessage("List of all users currently online at " + "[" + dateFormat.format(new Date()) + "]" + "\n");
                            for (int i = 0; i < arrayOfThreads.size(); ++i) { // loop through the array of online members
                                ClientThread clientThreadInstance = arrayOfThreads.get(i);
                                writeMessage((i + 1) + ". " + clientThreadInstance.username + " - " + "logged in at: " + clientThreadInstance.date);
                            }
                        } else {
                            writeMessage("You must be the coordinator to use this command, current coordinator is " + arrayOfThreads.get(0).username);
                        }
                        break;
                    case ConsoleMessage.coordinator:
                        if (arrayOfThreads.get(0).coordinatorRole == true) {
                            writeMessage("The Current Coordinator is: " + arrayOfThreads.get(0).username);
                        } else {
                            serverMessages("Error no current coordinator selected!");
                        }
                        break;
                }
            }
            remove(id);
            //<editor-fold defaultstate="collapsed" desc="coordinator statement">
            try {
                if (coordinatorRole == true) {
                    coordinatorRole = false;
                    arrayOfThreads.get(0).coordinatorRole = true;
                    serverMessages(arrayOfThreads.get(0).username + " set as new coordinator with id: " + arrayOfThreads.get(0).id);
                }
            } catch (Exception e) {
                System.out.println("There are no users currently online to assign a coordinator");
            }
//</editor-fold>
            clientDisconnect();
        }
//</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="clientDisconnect Method">
        private void clientDisconnect() { // closing both streams and socket
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

        //<editor-fold defaultstate="collapsed" desc="writeMessage Method">
        private boolean writeMessage(String message) { // to check if user is still online or disconnected
            if (!socket.isConnected()) {
                clientDisconnect();
                return false;
            }
            try { // try sending message to the outputstream
                output.writeObject(message);
            } catch (IOException ioe) { // if impossible - display error message
                serverMessages("There is an error trying to communicate with " + username);
                serverMessages(ioe.toString());
            }
            return true;
        }
//</editor-fold>
    }
//</editor-fold>
}
