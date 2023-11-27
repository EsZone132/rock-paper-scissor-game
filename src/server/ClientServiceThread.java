//Code written by:
//George Maximos (40206332) &
//Ethan Santhiyapillai (40212845)

package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientServiceThread extends Thread {

    //the ClientServiceThread class extends the Thread class and has the following parameters
    public String clientname; //client name
    public String opponent; //opponent name if in a game
    public String choice; //choice in game
    public Socket connectionSocket; //client connection socket
    ArrayList<ClientServiceThread> Clients; //list of all clients connected to the server

    //constructor function
    public ClientServiceThread(String clientname, Socket connectionSocket, ArrayList<ClientServiceThread> Clients) {

        this.clientname = clientname; //used to identify clients
        this.connectionSocket = connectionSocket;
        this.Clients = Clients;

        opponent = ""; //initialize to empty until match is formed
        choice = ""; //initialize to empty until match is formed

    }

    //thread's run function
    public void run() {

        try {

            //create a buffer reader and connect it to the client's connection socket
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            ;
            String clientSentence;
            DataOutputStream outToClient;

            //always read messages from client
            while (true) {

                clientSentence = inFromClient.readLine();
                //System.out.println("Server from: " + clientname + " -> " + clientSentence); //used for debugging

                //check the start of the message
                if (clientSentence.startsWith("-RemUser")) { //Remove Client

                    for (int i = 0; i < Clients.size(); i++) {

                        if (Clients.get(i).clientname.equals(clientname)) {

                            Clients.remove(i); //removed client from list of clients
                            i--; //to avoid skipping a compare

                        } else {

                            if (Clients.get(i).opponent.equals(clientname)) {
                                Clients.get(i).opponent = ""; //opponent left
                                Clients.get(i).choice = ""; //clear choice also

                                String availableclient = Clients.get(i).clientname; //client now becomes available

                                for (int j = 0; j < Clients.size(); j++) { //add available players to all lists again

                                    if (!(Clients.get(j).clientname.equals(availableclient) || Clients.get(j).clientname.equals(clientname))) {
                                        outToClient = new DataOutputStream(Clients.get(j).connectionSocket.getOutputStream());
                                        outToClient.writeBytes("-AddUser;" + availableclient + "\n");
                                    }

                                }

                            }

                            outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-RemUser;true;" + clientname + "\n"); //send to all other users to remove client from each drop down menu

                        }
                    }


                } else if (clientSentence.startsWith("-Request")) { //request from clientname to opponent

                    String[] msg = clientSentence.split(";");
                    //System.out.println("Server: " + clientSentence); //used for debugging

                    for (int i = 0; i < Clients.size(); i++) {

                        if (Clients.get(i).clientname.equals(msg[1])) {

                            if (Clients.get(i).opponent.equals("")) { //check that opponent is not already in a game

                                //System.out.println("Current op: " + Clients.get(i).opponent); //used for debugging
                                opponent = msg[1]; //set client opponent
                                Clients.get(i).opponent = clientname; //set opponent for other client too

                                outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                                outToClient.writeBytes("-Request;" + clientname + "\n"); //send game request to opponent

                                for (int j = 0; j < Clients.size(); j++) { //remove unavailable clients from all lists

                                    outToClient = new DataOutputStream(Clients.get(j).connectionSocket.getOutputStream());
                                    outToClient.writeBytes("-RemUser;false;" + clientname + "\n");
                                    outToClient.writeBytes("-RemUser;false;" + opponent + "\n");

                                }

                            } else { //client already has an opponent (should never happen)

                                for (int j = 0; j < Clients.size(); j++) {

                                    if (Clients.get(j).clientname.equals(clientname)) { //end game so player is not stuck waiting for request

                                        outToClient = new DataOutputStream(Clients.get(j).connectionSocket.getOutputStream());
                                        outToClient.writeBytes("-EndGame;" + msg[1] + "\n");

                                    }
                                }

                            }

                            break;

                        }

                    }

                } else if (clientSentence.startsWith("-StartGame")) { //both players ready start game by sending to both

                    String[] msg = clientSentence.split(";");
                    //System.out.println("Server: " + clientSentence); //used for debugging

                    for (int i = 0; i < Clients.size(); i++) {
                        if (Clients.get(i).clientname.equals(msg[1]) || Clients.get(i).clientname.equals(clientname)) {

                            outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-StartGame\n"); //tell both who they are playing against to start

                        }
                    }

                } else if (clientSentence.startsWith("-Choice")) { //game choice was made by player

                    String[] msg = clientSentence.split(";");
                    choice = msg[1];

                    //check if opponent made a choice
                    for (int i = 0; i < Clients.size(); i++) {

                        if (Clients.get(i).clientname.equals(opponent)) {

                            if (!Clients.get(i).choice.equals("")) { //compare choices if both chose, otherwise wait (do nothing)

                                String choices = choice + " x " + Clients.get(i).choice;
                                String results;

                                switch (choices) { //switch choices for all possibilities (win/lose/draw)

                                    case "Rock x Paper":
                                        results = "lose;win";
                                        break;
                                    case "Paper x Rock":
                                        results = "win;lose";
                                        break;
                                    case "Rock x Scissors":
                                        results = "win;lose";
                                        break;
                                    case "Scissors x Rock":
                                        results = "lose;win";
                                        break;
                                    case "Scissors x Paper":
                                        results = "win;lose";
                                        break;
                                    case "Paper x Scissors":
                                        results = "lose;win";
                                        break;
                                    case "Rock x Rock":
                                        results = "draw;draw";
                                        break;
                                    case "Paper x Paper":
                                        results = "draw;draw";
                                        break;
                                    case "Scissors x Scissors":
                                        results = "draw;draw";
                                        break;
                                    default:
                                        results = "error;error";

                                }

                                if (results.equals("win;lose")) { //tell players who won depending on results

                                    outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                                    outToClient.writeBytes("-Results;" + choices + "... " + clientname + " wins\n");

                                    for (int j = 0; j < Clients.size(); j++) {
                                        if (Clients.get(j).clientname.equals(clientname)) {
                                            outToClient = new DataOutputStream(Clients.get(j).connectionSocket.getOutputStream());
                                            outToClient.writeBytes("-Results;" + choices + "... " + "You win\n");
                                        }
                                    }

                                } else if (results.equals("lose;win")) { //tell players who won depending on results

                                    outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                                    outToClient.writeBytes("-Results;" + choices + "... " + "You win\n");

                                    for (int j = 0; j < Clients.size(); j++) {
                                        if (Clients.get(j).clientname.equals(clientname)) {
                                            outToClient = new DataOutputStream(Clients.get(j).connectionSocket.getOutputStream());
                                            outToClient.writeBytes("-Results;" + choices + "... " + opponent + " wins\n");
                                        }
                                    }

                                } else if (results.equals("draw;draw")) { //encourage players to play again in case of draw

                                    outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                                    outToClient.writeBytes("-Results;" + choices + "... draw, Play again!\n");

                                    for (int j = 0; j < Clients.size(); j++) {
                                        if (Clients.get(j).clientname.equals(clientname)) {
                                            outToClient = new DataOutputStream(Clients.get(j).connectionSocket.getOutputStream());
                                            outToClient.writeBytes("-Results;" + choices + "... draw, Play again!\n");
                                        }
                                    }

                                } else { //should never happen, only if a bug occurs allow players to try again

                                    outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                                    outToClient.writeBytes("-Results;" + "An error occured, please try again\n");

                                    for (int j = 0; j < Clients.size(); j++) {
                                        if (Clients.get(j).clientname.equals(clientname)) {
                                            outToClient = new DataOutputStream(Clients.get(j).connectionSocket.getOutputStream());
                                            outToClient.writeBytes("-Results;" + "An error occured, please try again\n");
                                        }
                                    }

                                }

                                //reset choices of both players to start a new game
                                choice = "";
                                Clients.get(i).choice = "";

                            }

                        }

                    }

                } else if (clientSentence.startsWith("-EndGame") && !opponent.equals("")) { //end game by telling opponent client stopped
                    //checking that opponent is not empty because clicks sometimes generate two EndGame messages

                    //System.out.println("Server: " + clientSentence); //used for debugging

                    for (int i = 0; i < Clients.size(); i++) { //add available players to all lists again

                        outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                        if (Clients.get(i).clientname.equals(clientname)) {
                            outToClient.writeBytes("-AddUser;" + opponent + "\n");
                            //System.out.println("clientname: " + "-AddUser;" + opponent + "\n"); //used for debugging
                        } else if (Clients.get(i).clientname.equals(opponent)) {
                            outToClient.writeBytes("-AddUser;" + clientname + "\n");
                            //System.out.println("opponent: " + "-AddUser;" + clientname + "\n"); //used for debugging
                        } else {
                            outToClient.writeBytes("-AddUser;" + clientname + "\n");
                            outToClient.writeBytes("-AddUser;" + opponent + "\n");
                            //System.out.println("None: " + "-AddUser;"  + clientname + ";" + opponent + "\n"); //used for debugging
                        }

                    }

                    for (int i = 0; i < Clients.size(); i++) {

                        if (Clients.get(i).clientname.equals(opponent)) {

                            outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-EndGame\n"); //send to opponent that game ended

                            //clear opponents and choices to allow new games to form and start
                            Clients.get(i).opponent = "";
                            Clients.get(i).choice = "";
                            opponent = "";
                            choice = "";
                            break;

                        }

                    }

                }
            }

        } catch (Exception ex) {

        }

    }

}
