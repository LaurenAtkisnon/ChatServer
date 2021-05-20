package chatserver;

import java.io.*;
import java.util.*;
import java.net.*;
import static java.lang.System.out;

public class ChatServer {

    //Set Default Room String
    String DEFAULT_ROOM = "0";
    Vector<HandleClient> clients = new Vector<HandleClient>();

    //Start server and accept clients
    public void process() {
        try {
            ServerSocket server = new ServerSocket(1518, 10);
            out.println("ChatServer started...");

            while (true) {
                Socket client = server.accept();
                HandleClient c = new HandleClient(client);
                clients.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //MAIN runs the server and accepts clients
    public static void main(String... args) throws Exception {
        new ChatServer().process();
    }

    //Sends messages to clients
    public void broadcast(HandleClient client, String message) {
        for (HandleClient c : clients) {
            //uncomment if you want a client to not recieve their own message
//            if (c.equals(client)) {
//                continue;
//            } 
            if (c.room.equals(client.room)) {
                c.sendMessage("NEWMESSAGE " + client.name + " " + message);
            }
        }
    }

    //Creates clients with name and room
    class HandleClient extends Thread {

        String name;
        String room;
        BufferedReader input;
        PrintWriter output;

        public HandleClient(Socket client) throws Exception {
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new PrintWriter(client.getOutputStream(), true);
            String[] command = input.readLine().split(" ");

            // handles ENTER <name> or <name>
            if (command[0].equals("ENTER")) {
                name = command[1];
            } else {
                name = command[0];
            }

            start();
            sendMessage("ACK ENTER " + name);
            enterRoom(DEFAULT_ROOM);
        }

        //Server message to client
        public void sendMessage(String msg) {
            output.println(msg);
        }

        //client enters a room
        public void enterRoom(String room) {
            out.println(name + " entered chatroom " + room);
            sendMessage("ACK JOIN " + room);
            this.room = room;
            for (HandleClient c : clients) {
                if (c.room.equals(room)) {
                    c.sendMessage("ENTERING " + name);
                }
            }
        }

        //client exits a room
        public void exitRoom() {
            out.println(name + " left chatroom");
            for (HandleClient c : clients) {
                if (c.room.equals(room)) {
                    c.sendMessage("EXITING " + name);
                }
            }
        }

        //Protocol 
        public void run() {
            try {
                while (true) {

                    String[] command = input.readLine().split(" ");

                    switch (command[0]) {
                        case "EXIT":
                            exitRoom();
                            clients.remove(this);
                            out.println(name + " has been succesfully removed");

                            break;
                        case "JOIN":
                            String newRoom = command[1];
                            exitRoom();
                            enterRoom(newRoom);
                            break;
                        case "TRANSMIT":
                            StringBuilder message = new StringBuilder();
                            for (int i = 1; i < command.length; i++) {
                                message.append(command[i]).append(" ");
                            }
                            broadcast(this, message.toString());
                            break;
                    }

                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
