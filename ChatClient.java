package chatserver;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.out;
import javax.swing.text.DefaultCaret;

//Chat client class
public class ChatClient extends JFrame implements ActionListener {

    String uname;
    String room;
    PrintWriter pw;
    BufferedReader br;
    JTextArea taMessages;
    JTextField tfInput;
    JButton btnSend, btnExit, btnRoom;
    Socket client;

    //Create client
    public ChatClient(String uname, String servername) throws Exception {
        super(uname);
        this.uname = uname;
        client = new Socket(servername, 1518);
        br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        pw = new PrintWriter(client.getOutputStream(), true);
        pw.println("ENTER " + uname);
        // send name
        buildInterface();
        new MessagesThread().start();
    }

    //UI
    public void buildInterface() {
        btnSend = new JButton("Send");
        btnExit = new JButton("Exit");
        btnRoom = new JButton("Room: 0");
        taMessages = new JTextArea();
        taMessages.setRows(10);
        taMessages.setColumns(50);
        taMessages.setEditable(false);
        //automatic scrolling
        DefaultCaret caret = (DefaultCaret) taMessages.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        tfInput = new JTextField(50);
        JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp, "Center");
        JPanel bp = new JPanel(new FlowLayout());
        bp.add(tfInput);
        bp.add(btnSend);
        bp.add(btnRoom);
        bp.add(btnExit);
        add(bp, "South");
        btnSend.addActionListener(this);
        btnRoom.addActionListener(this);
        btnExit.addActionListener(this);
        setSize(500, 300);
        setVisible(true);
        pack();
    }

    //ACTIONS
    public void actionPerformed(ActionEvent evt) {
        //EXIT BUTTON
        if (evt.getSource() == btnExit) {
            pw.println("EXIT");
            System.exit(0);
        }
        //ROOM BUTTON
        if (evt.getSource() == btnRoom) {
            String room = JOptionPane.showInputDialog(null, "Enter room name :", "Roomname",
                    JOptionPane.PLAIN_MESSAGE);
            if (room != null) {
                pw.println("JOIN " + room);
            }
        } 
        //TEXT FIELD
        else {           
            String text = tfInput.getText();
            //prevents sending blank maessages
            if (text != null && text.length() > 0) {
                pw.println("TRANSMIT " + text);
                //uncomment to print to text area 
                //taMessages.append(text + "\n"); 
            }
            tfInput.setText("");
        }
    }

    //MAIN starts a client by entering their name
    public static void main(String... args) {

        // get user name
        String name = JOptionPane.showInputDialog(null, "Enter your name :", "Username",
                JOptionPane.PLAIN_MESSAGE);
        String servername = "localhost";
        try {
            new ChatClient(name, servername);
        } catch (Exception ex) {
            out.println("Error --> " + ex.getMessage());
        }

    }

    //PROTOCOL
    class MessagesThread extends Thread {
        public void run() {
            try {
                while (true) {
                    String line = br.readLine();
                    out.println(line);
                    String[] command = line.split(" ");
                    switch (command[0]) {
                        case "NEWMESSAGE":
                            String user = command[1];
                            StringBuilder message = new StringBuilder(user + ": ");
                            for (int i = 2; i < command.length; i++) {
                                message.append(command[i]).append(" ");

                            }
                            taMessages.append(message + "\n");
                            break;
                        case "ACK":
                            switch (command[1]) {
                                case "JOIN":
                                    taMessages.append("Successfully joined room" + "\n");
                                    taMessages.append(uname + " entered room" + "\n");
                                    room = command[2];
                                    btnRoom.setText("Room: " + room);
                                    break;
                                case "ENTER":
                                    taMessages.append("Connected to server" + "\n");
                                    break;
                            }
                            break;
                        case "ENTERING":
                            taMessages.append(command[1] + " entered room" + "\n");
                            break;
                        case "EXITING":
                            taMessages.append(command[1] + " exited room" + "\n");
                            break;
                    }

                }
            } catch (Exception ex) {
                out.println("Error --> " + ex.getMessage());
            }
        }
    }
}
