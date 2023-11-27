//Code written by:
//George Maximos (40206332) &
//Ethan Santhiyapillai (40212845)

package client;

import java.io.*;
import java.net.*;
import javax.swing.*;

import java.awt.Color;
import java.awt.event.*;

public class Client {

    //general class static variables to be accessible in Thread and in button listeners
    static Socket clientSocket;

    static JLabel userinfo;

    static JComboBox<String> recipient;
    static JTextField nameTextField;
    static String clientname;

    static JLabel playLabel;
    static JButton playButton;

    static Color darkgreen = new Color(0, 153, 0);
    static Color darkyellow = new Color(255, 204, 0);

    static JLabel requestLabel;
    static JButton acceptButton;
    static JButton rejectButton;

    static JButton rockButton;
    static JButton paperButton;
    static JButton scissorsButton;

    static boolean req_ans;

    static String opponent = "";

    public static void main(String[] args) throws Exception {

        //Create the GUI frame and components
        JFrame frame = new JFrame("Client Connection Page");
        frame.setLayout(null);
        frame.setBounds(100, 100, 500, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel nameLabel = new JLabel("Client Name");
        nameLabel.setBounds(20, 20, 80, 30);
        frame.getContentPane().add(nameLabel);

        nameTextField = new JTextField(); //takes name of client (used for private communication mainly)
        nameTextField.setBounds(110, 20, 150, 30);
        frame.getContentPane().add(nameTextField);

        JButton connectButton = new JButton("Connect"); //used to connect and later disconnect to server
        connectButton.setBounds(290, 18, 100, 30);
        frame.getContentPane().add(connectButton);

        playLabel = new JLabel("Play with:"); //player to play against
        playLabel.setBounds(20, 60, 100, 30);
        frame.getContentPane().add(playLabel);
        playLabel.setVisible(false);

        playButton = new JButton("Play"); //button to send game request
        playButton.setBounds(290, 60, 80, 30);
        frame.getContentPane().add(playButton);
        playButton.setVisible(false);

        userinfo = new JLabel(""); //displays all info needed to be given to the player
        userinfo.setBounds(20, 400, 500, 30);
        frame.getContentPane().add(userinfo);

        requestLabel = new JLabel(""); //for receiving requests from other players
        requestLabel.setBounds(20, 120, 450, 30);
        requestLabel.setVisible(false);
        frame.getContentPane().add(requestLabel);

        acceptButton = new JButton("Accept"); //accept game request
        acceptButton.setBounds(20, 160, 80, 30);
        acceptButton.setForeground(Color.blue);
        acceptButton.setVisible(false);
        frame.getContentPane().add(acceptButton);

        rejectButton = new JButton("Reject"); //reject game request
        rejectButton.setBounds(110, 160, 80, 30);
        rejectButton.setForeground(Color.red);
        rejectButton.setVisible(false);
        frame.getContentPane().add(rejectButton);

        //buttons for all 3 game options
        rockButton = new JButton("Rock");
        rockButton.setBounds(180, 120, 100, 80);
        rockButton.setVisible(false);
        frame.getContentPane().add(rockButton);

        paperButton = new JButton("Paper");
        paperButton.setBounds(180, 210, 100, 80);
        paperButton.setVisible(false);
        frame.getContentPane().add(paperButton);

        scissorsButton = new JButton("Scissors");
        scissorsButton.setBounds(180, 300, 100, 80);
        scissorsButton.setVisible(false);
        frame.getContentPane().add(scissorsButton);

        //Action listener when connect/disconnect button is pressed
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {

                    if (connectButton.getText().equals("Connect")) { //if pressed to Connect

                        if (nameTextField.getText().length() > 0) { //check if field contains name

                            clientname = nameTextField.getText();

                            //create a new socket to connect with the server application
                            clientSocket = new Socket("localhost", 6969);

                            //send name
                            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                            outToServer.writeBytes("-AddUser;" + clientname + "\n"); //send name to server for validation

                            //receive the reply (rejected or accepted)
                            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            String receivedcheck = inFromServer.readLine(); //this check has no prefix it is the only packet starting with Valid/Invalid
                            String[] strings = receivedcheck.split(";");

                            if (strings[0].equals("Invalid")) { //server found name in use, no client created and gui stays same

                                userinfo.setForeground(Color.red);
                                userinfo.setText("Connection rejected: The name " + clientname + " is used by another client");

                            } else if (strings[0].equals("Valid")) { //name is unique, client is created

                                StartThread(); //this Thread checks for input messages from server

                                frame.setTitle("Client: " + clientname); //frame title has clientname for ease of identification

                                recipient = new JComboBox<String>(); //create drop down menu
                                recipient.setBounds(110, 60, 150, 30);
                                recipient.setVisible(true);
                                frame.add(recipient);

                                //need to add existing players to recipient list
                                for (int i = 1; i < strings.length; i++) {
                                    recipient.addItem(strings[i]);
                                }

                                //make the GUI components visible, so the client can send start a game
                                playButton.setVisible(true);
                                playLabel.setVisible(true);

                                userinfo.setForeground(Color.blue);
                                userinfo.setText("You are Connected");

                                //change the Connect button text to Disconnect
                                connectButton.setText("Disconnect");
                                nameTextField.setEnabled(false);

                                opponent = "";

                                if (recipient.getItemCount() == 0) { //check if player alone waiting to play game

                                    //cannot start a game
                                    recipient.setEnabled(false);
                                    playButton.setEnabled(false);

                                    //player is told no one else is available
                                    userinfo.setForeground(Color.red);
                                    userinfo.setText("No opponents at the moment, please wait for players to become available...");

                                }

                            }

                        }

                    } else { //if pressed to Disconnect

                        //create an output stream and send a RemUser message to disconnect from the server
                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        outToServer.writeBytes("-RemUser\n");

                        //close the client's socket
                        clientSocket.close();

                        frame.setTitle("Client Connection Page");

                        //make the GUI components for playing and interacting with others invisible
                        playButton.setVisible(false);
                        playButton.setText("Play");
                        playLabel.setVisible(false);
                        recipient.setVisible(false);

                        requestLabel.setVisible(false);
                        acceptButton.setVisible(false);
                        rejectButton.setVisible(false);

                        rockButton.setVisible(false);
                        paperButton.setVisible(false);
                        scissorsButton.setVisible(false);

                        userinfo.setForeground(Color.red);
                        userinfo.setText("You Disconnected");

                        //change the Disconnect button text to Connect
                        connectButton.setText("Connect");
                        nameTextField.setEnabled(true);

                    }

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }
        });

        //Action listener when send button is pressed
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (playButton.getText().equals("Play")) { //button pressed to start a game

                        //create an output stream
                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

                        if (recipient.getSelectedItem() == null) { //should never occur, button disabled when no players connected

                            userinfo.setForeground(Color.red);
                            userinfo.setText("No opponent selected");

                        } else {

                            String request = "-Request;" + recipient.getSelectedItem() + "\n"; //send a request to server to get to desired player
                            outToServer.writeBytes(request);

                            opponent = (String) recipient.getSelectedItem();

                            playButton.setText("Stop");
                            recipient.setEnabled(false);

                            userinfo.setForeground(Color.blue);
                            userinfo.setText("Waiting for " + recipient.getSelectedItem() + " to accept request..."); //tell player available

                        }

                    } else if (playButton.getText().equals("Stop")) { //button pressed to end a game

                        rockButton.setVisible(false); //remove game components from gui
                        paperButton.setVisible(false);
                        scissorsButton.setVisible(false);

                        recipient.setEnabled(true); //allow player to choose a new opponent

                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        outToServer.writeBytes("-EndGame;\n"); //let opponent know that the game ended

                        playButton.setText("Play"); //allow client to start a new game

                        userinfo.setForeground(Color.red);
                        userinfo.setText("Game ended");

                    }

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }
        });

        //Disconnect on window close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                try {

                    //create an output stream and send a RemUser message to disconnect from the server
                    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    outToServer.writeBytes("-RemUser\n");

                    //close the client's socket
                    clientSocket.close();

                    System.exit(0); //exit code

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }

            }
        });

        frame.setVisible(true);

    }

    //Thread to always read messages from the server and take appropriate actions
    private static void StartThread() {

        new Thread(new Runnable() { /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
            @Override
            public void run() {

                try {

                    //create a buffer reader and connect it to the socket's input stream
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    String receivedSentence;

                    //always read received messages and append them to the textArea
                    while (true) {

                        receivedSentence = inFromServer.readLine();
                        //System.out.println("Client: " + receivedSentence); //used for debugging

                        if (receivedSentence.startsWith("-Request")) { //request to join a game

                            String[] strings = receivedSentence.split(";");
                            req_ans = false;
                            int countdown = 60; //give player 60 seconds to respond to request

                            opponent = strings[1];

                            //remove buttons to choose opponent and show request to accept or reject
                            playButton.setVisible(false);
                            recipient.setEnabled(false);
                            requestLabel.setForeground(darkgreen);
                            requestLabel.setText(opponent + " wants to play with you! (remaining time: " + countdown + ")"); //tell client who wants to play and remaining time to accept
                            requestLabel.setVisible(true);
                            acceptButton.setVisible(true);
                            rejectButton.setVisible(true);

                            acceptButton.addActionListener(new ActionListener() { //player accepts game
                                public void actionPerformed(ActionEvent e) {

                                    DataOutputStream outToServer;
                                    try {

                                        //remove request and show stop button
                                        requestLabel.setVisible(false);
                                        acceptButton.setVisible(false);
                                        rejectButton.setVisible(false);
                                        playButton.setVisible(true);
                                        playButton.setText("Stop");

                                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                                        outToServer.writeBytes("-StartGame;" + strings[1] + "\n"); //tell other player game is starting

                                        req_ans = true;

                                    } catch (IOException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }
                            });

                            rejectButton.addActionListener(new ActionListener() { //player rejects game
                                public void actionPerformed(ActionEvent e) {

                                    DataOutputStream outToServer;
                                    try {

                                        //remove request and show play button to choose new opponent
                                        requestLabel.setVisible(false);
                                        acceptButton.setVisible(false);
                                        rejectButton.setVisible(false);
                                        playButton.setVisible(true);
                                        playButton.setText("Play");

                                        recipient.setEnabled(true);

                                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                                        outToServer.writeBytes("-EndGame;\n"); //tell other player request was rejected

                                        req_ans = true;

                                    } catch (IOException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }
                            });

                            while (!req_ans) { //loops until either button pressed or time runs out

                                receivedSentence = inFromServer.readLine(); //3 things that can happen while waiting for a request
                                if (receivedSentence.startsWith("-RemUser")) {
                                    RemUser(receivedSentence); //opponent disconnects ending request
                                    break;
                                } else if (receivedSentence.startsWith("-AddUser")) {
                                    AddUser(receivedSentence); //another user connects must be added to list
                                } else if (receivedSentence.startsWith("-EndGame")) {
                                    EndGame(receivedSentence); //opponent presses stop ending request
                                    break;
                                }

                                Thread.sleep(1000); //waits the desired 1 second
                                countdown--;

                                if (countdown == 0) { //automatically reject request
                                    DataOutputStream outToServer;

                                    //remove request and show play button to choose new opponent
                                    requestLabel.setVisible(false);
                                    acceptButton.setVisible(false);
                                    rejectButton.setVisible(false);
                                    playButton.setVisible(true);
                                    playButton.setText("Play");

                                    recipient.setEnabled(true);

                                    outToServer = new DataOutputStream(clientSocket.getOutputStream());
                                    outToServer.writeBytes("-EndGame;\n"); //tell other player request was rejected

                                    req_ans = true;
                                } else if (countdown < 11) //very close to end of timer
                                    requestLabel.setForeground(Color.red);
                                else if (countdown < 31) //close to end of timer
                                    requestLabel.setForeground(darkyellow);

                                requestLabel.setText(strings[1] + " wants to play with you! (remaining time: " + countdown + ")"); //tell client who wants to play and remaining time to accept

                            }

                        } else if (receivedSentence.startsWith("-AddUser")) { //new client connected to server

                            AddUser(receivedSentence); //used in multiple places so create method

                        } else if (receivedSentence.startsWith("-StartGame")) { //game was accepted on both ends and can start

                            //disable button to choose opponent
                            recipient.setEnabled(false);

                            //show and enable game buttons
                            rockButton.setVisible(true);
                            paperButton.setVisible(true);
                            scissorsButton.setVisible(true);

                            rockButton.setEnabled(true);
                            paperButton.setEnabled(true);
                            scissorsButton.setEnabled(true);

                            userinfo.setForeground(Color.blue);
                            userinfo.setText("Game Started against " + opponent + "!");

                            //listeners for all 3 game buttons
                            rockButton.addActionListener(new ActionListener() { //rock is chosen
                                public void actionPerformed(ActionEvent e) {

                                    DataOutputStream outToServer;
                                    try {

                                        //disable all buttons until new game starts
                                        rockButton.setEnabled(false);
                                        paperButton.setEnabled(false);
                                        scissorsButton.setEnabled(false);

                                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                                        outToServer.writeBytes("-Choice;Rock\n"); //send choice to server

                                    } catch (IOException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }
                            });

                            paperButton.addActionListener(new ActionListener() { //paper is chosen
                                public void actionPerformed(ActionEvent e) {

                                    DataOutputStream outToServer;
                                    try {

                                        //disable all buttons until new game starts
                                        rockButton.setEnabled(false);
                                        paperButton.setEnabled(false);
                                        scissorsButton.setEnabled(false);

                                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                                        outToServer.writeBytes("-Choice;Paper\n"); //send choice to server

                                    } catch (IOException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }
                            });

                            scissorsButton.addActionListener(new ActionListener() { //scissors is chosen
                                public void actionPerformed(ActionEvent e) {

                                    DataOutputStream outToServer;
                                    try {

                                        //disable all buttons until new game starts
                                        rockButton.setEnabled(false);
                                        paperButton.setEnabled(false);
                                        scissorsButton.setEnabled(false);

                                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                                        outToServer.writeBytes("-Choice;Scissors\n"); //send choice to server

                                    } catch (IOException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }
                                }
                            });

                        } else if (receivedSentence.startsWith("-Result")) { //received after both players make their choice

                            String[] strings = receivedSentence.split(";");
                            userinfo.setForeground(Color.blue);
                            userinfo.setText(strings[1]); //display winner message

                            //enable buttons again to play another game
                            rockButton.setEnabled(true);
                            paperButton.setEnabled(true);
                            scissorsButton.setEnabled(true);

                        } else if (receivedSentence.startsWith("-EndGame")) { //request rejected or game stopped

                            EndGame(receivedSentence); //used in multiple places so create method

                        } else if (receivedSentence.startsWith("-RemUser")) { //user removed from recipient list

                            RemUser(receivedSentence); //used in multiple places so create method

                        } else if (receivedSentence.startsWith("-OnlyUser")) { //client is connected alone

                            //cannot start a game
                            recipient.setEnabled(false);
                            playButton.setEnabled(false);

                            //player is told no one else is connected
                            userinfo.setForeground(Color.red);
                            userinfo.setText("No opponents at the moment, please wait for players to connect...");

                        }
                    }

                } catch (Exception ex) {

                }

            }
        }).start();

    }


    public static void RemUser(String receivedSentence) {

        String[] strings = receivedSentence.split(";");
        //System.out.println(receivedSentence); //used for debugging

        for (int i = 0; i < recipient.getItemCount(); i++) {

            if (recipient.getItemAt(i).equals(strings[2])) { //find and remove user from drop down menu

                recipient.removeItemAt(i);
                break;

            }

        }

        if (strings[2].equals(opponent) && strings[1].equals("true")) { //removed player was opponent that disconnected

            //game or request must end and gui go back to allow for a new game to be set up
            playButton.setVisible(true);
            playButton.setText("Play");
            recipient.setEnabled(true);

            requestLabel.setVisible(false);
            acceptButton.setVisible(false);
            rejectButton.setVisible(false);

            rockButton.setVisible(false);
            paperButton.setVisible(false);
            scissorsButton.setVisible(false);

            rockButton.setEnabled(true);
            paperButton.setEnabled(true);
            scissorsButton.setEnabled(true);

            userinfo.setForeground(Color.red);
            userinfo.setText("Your opponent: " + opponent + " disconnected!"); //tell player opponent disconnected

            opponent = "";

            playButton.setText("Play");

        }

        if (recipient.getItemCount() == 0 && opponent.equals("")) { //check if player alone waiting to play game

            //cannot start a game
            recipient.setEnabled(false);
            playButton.setEnabled(false);

            //player is told no one else is available
            userinfo.setForeground(Color.red);
            userinfo.setText("No opponents at the moment, please wait for players to become available...");

        }

    }

    public static void AddUser(String receivedSentence) {

        String[] strings = receivedSentence.split(";");
        recipient.addItem(strings[1]); //add to player list of all clients

        //remove message saying user is alone and allow to start a game
        if (recipient.getItemCount() == 1 && opponent.equals("")) {

            userinfo.setText("");
            recipient.setEnabled(true);
            playButton.setEnabled(true);

        }

    }

    public static void EndGame(String receivedSentence) {

        //hide all buttons that are not needed anymore for communication with opponent
        rockButton.setVisible(false);
        paperButton.setVisible(false);
        scissorsButton.setVisible(false);

        rockButton.setEnabled(true);
        paperButton.setEnabled(true);
        scissorsButton.setEnabled(true);

        requestLabel.setVisible(false);
        acceptButton.setVisible(false);
        rejectButton.setVisible(false);

        playButton.setVisible(true);
        playButton.setText("Play");
        playButton.setEnabled(true);
        recipient.setEnabled(true);

        userinfo.setForeground(Color.red); //tell player opponent ended the game
        userinfo.setText(opponent + " does not want to play at the moment!");

        opponent = "";

    }

}
