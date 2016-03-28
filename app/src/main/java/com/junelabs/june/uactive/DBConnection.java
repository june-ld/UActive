package com.junelabs.june.uactive;

import android.app.ProgressDialog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by June on 3/11/2016.
 */
public class DBConnection {
    private Socket connection;
    private boolean connSuccessful = true;


    public static final int ADD_SUCCESSFUL = 1;
    public static final int ADD_EXISTS = 2;
    public static final int ADD_FAILED = 3;


    public DBConnection(){

        try {
            connection = new Socket("10.0.2.2", 4444);
            connSuccessful = true;
        } catch (IOException e) {
            connSuccessful = false;
            e.printStackTrace();
        }
    }

    public static void enableProgressDialog( ProgressDialog dialog){
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void disableProgressDialog( ProgressDialog dialog){
        dialog.cancel();
    }


    public boolean getConnectionStatus() {
        return connSuccessful;
    }

    public ActiveUser getActiveUserServ(String username, String password){

        ActiveUser retrievedUser;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            String message = String.format("getUser,%s,%s\r\n", username, password);
            out.write(message.getBytes());

            String received = in.readLine();
            if(received.isEmpty())
                return null;
            if(received.matches("NOTFOUND"))
                return null;


            String[] response = received.split(",");

            retrievedUser = new ActiveUser(Integer.valueOf(response[0]), response[1], response[2], response[3], response[4], Integer.valueOf(response[5]));


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return retrievedUser;
    }

    public String addUser(String username, String email, String password, String secAns){
        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // send addUser request to server
            String message = String.format("addUser,%s,%s,%s,%s,0\r\n", username, password, email, secAns);
            out.write(message.getBytes());

            // wait for response
            String received = in.readLine();

            // process possible failures
            if(received.isEmpty())
                return "ADD_FAILED";
            if(received.matches("USER_EXISTS"))
                return "ADD_EXISTS";
            if(received.matches("ADD_FAILED"))
                return "ADD_FAILED";

            // return successful if server returns successful
            return received;


        } catch (IOException e) {
            e.printStackTrace();
            return "ADD_FAILED";
        }
    }

    public String updateUserEXP(int userID, int currentEXP){
        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // send addUser request to server
            String message = String.format("updateUserEXP,%d,%d\r\n", userID, currentEXP );
            out.write(message.getBytes());

            // wait for response
            String received = in.readLine();

            // process possible failures
            if(received.isEmpty())
                return "UPDATE_FAILED";

            // return successful if server returns successful
            return received;

        } catch (IOException e) {
            e.printStackTrace();
            return "UPDATE_FAILED";
        }
    }

    public ArrayList<ActiveChallenge> getAllChallenges(){
        ArrayList<ActiveChallenge> challenges = new ArrayList<>();

        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // write command to server
            out.write("getAllChallenges,0\r\n".getBytes());

            // get number of challenges; if 0, return null
            String received = in.readLine();
            int size = Integer.valueOf(received);
            if(size == 0)
                return challenges;

            // read each line and create challenge to add to Arraylist
            for(int i = 0; i < size; i++){
                while(true){
                    if(in.ready())
                        break;
                }
                received = in.readLine();
                if(received == null)
                    break;
                challenges.add(new ActiveChallenge(received));
            }

            // return Arraylist
            return challenges;


        } catch (IOException e) {
            e.printStackTrace();
            return challenges;
        }
    }

    public String addChallenge(String command){
        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // send addUser request to server
            out.write(command.getBytes());

            // wait for response
            String received = in.readLine();

            // process possible failures
            if(received.isEmpty())
                return "ADD_FAILED";
            if(received.matches("ADD_FAILED"))
                return "ADD_FAILED";

            // return successful if server returns successful
            return received;


        } catch (IOException e) {
            e.printStackTrace();
            return "ADD_FAILED";
        }
    }

    public ArrayList<Comment> getComments(int challengeID){
        ArrayList<Comment> comments = new ArrayList<>();

        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // write command to server
            String command = String.format("getComments,%d\r\n", challengeID);
            out.write(command.getBytes());

            // get number of challenges; if 0, return null
            String received = in.readLine();
            int size = Integer.valueOf(received);
            if(size == 0)
                return comments;

            // read each line and create challenge to add to Arraylist
            for(int i = 0; i < size; i++){
                while(true){
                    if(in.ready())
                        break;
                }
                received = in.readLine();
                if(received == null)
                    break;
                comments.add(new Comment(received));
            }

            // return Arraylist
            return comments;


        } catch (IOException e) {
            e.printStackTrace();
            return comments;
        }
    }

    public String addComment(String parseCode){
        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // send addUser request to server
            String command = String.format("addComment,%s\r\n",parseCode);
            out.write(command.getBytes());

            // wait for response
            String received = in.readLine();

            // process possible failures
            if(received.isEmpty())
                return "ADD_FAILED";
            if(received.matches("ADD_FAILED"))
                return "ADD_FAILED";

            // return successful if server returns successful
            return received;


        } catch (IOException e) {
            e.printStackTrace();
            return "ADD_FAILED";
        }
    }

    public String checkIn(int userID, int challengeID){
        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // send addUser request to server
            String command = String.format("checkIn,%d,%d\r\n", userID, challengeID);
            out.write(command.getBytes());

            // wait for response
            String received = in.readLine();

            // process possible failures
            if(received.isEmpty())
                return "ADD_FAILED";
            if(received.matches("ADD_FAILED"))
                return "ADD_FAILED";

            // return successful if server returns successful
            return received;


        } catch (IOException e) {
            e.printStackTrace();
            return "ADD_FAILED";
        }
    }

    public int checkInStatus(int userID, int challengeID){
        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // send addUser request to server
            String command = String.format("chInStatus,%d,%d\r\n", userID, challengeID);
            out.write(command.getBytes());

            // wait for response
            String received = in.readLine();

            // process possible failures
            if(received.isEmpty())
                return -1;
            if(received.matches("FAILED"))
                return -1;

            // return successful if server returns successful
            return Integer.valueOf(received);

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int checkInCount(int challengeID){
        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // send addUser request to server
            String command = String.format("chCount,%d\r\n", challengeID);
            out.write(command.getBytes());

            // wait for response
            String received = in.readLine();

            // process possible failures
            if(received == null)
                return -1;
            if(received.isEmpty())
                return -1;
            if(received.matches("FAILED"))
                return -1;

            // return successful if server returns successful
            return Integer.valueOf(received);

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // -1: op failed, 0: already exists, 1: added to list
    public int addNewOrg(String orgName, int userID){
        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // send addUser request to server
            String command = String.format("adOrg,%s,%d\r\n",orgName,userID);
            out.write(command.getBytes());

            // wait for response
            String received = in.readLine();

            // process possible failures
            if(received == null)
                return -1;
            if(received.isEmpty())
                return -1;
            if(received.matches("AE"))
                return 0;

            // return successful if server returns successful
            if(received.matches("SUCCESS"))
                return 1;
            else
                return -1;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // -1: failed, 0: user already exists, 1: org does not exist, 2: successful
    public int addUserToOrg(String code, int userID){
        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // send addUser request to server
            String command = String.format("adU2Org,%s,%d\r\n",code,userID);
            out.write(command.getBytes());

            // wait for response
            String received = in.readLine();

            // process possible failures
            if(received == null)
                return -1;
            if(received.isEmpty())
                return -1;
            if(received.matches("AE"))
                return 0;
            if(received.matches("DNE"))
                return 1;

            // return successful if server returns successful
            if(received.matches("SUCCESS"))
                return 2;
            else
                return -1;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public ArrayList<Organization> getUserOrgList(int userID){
        ArrayList<Organization> orgs = new ArrayList<>();

        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // write command to server
            String command = String.format("UOList,%d\r\n", userID);
            out.write(command.getBytes());

            // get number of challenges; if 0, return null
            String received = in.readLine();
            int size = Integer.valueOf(received);
            if(size == 0)
                return orgs;

            // read each line and create challenge to add to Arraylist
            for(int i = 0; i < size; i++){
                while(true){
                    if(in.ready())
                        break;
                }
                received = in.readLine();
                if(received == null)
                    break;
                orgs.add(new Organization(received));
            }

            // return Arraylist
            return orgs;


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public ArrayList<String> getHistory(int userID){
        ArrayList<String> hist = new ArrayList<>();

        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // write command to server
            String command = String.format("hist,%d\r\n", userID);
            out.write(command.getBytes());

            // get number of challenges; if 0, return null
            String received = in.readLine();
            int size = Integer.valueOf(received);
            if(size == 0)
                return hist;

            // read each line and create challenge to add to Arraylist
            for(int i = 0; i < size; i++){
                while(true){
                    if(in.ready())
                        break;
                }
                received = in.readLine();
                if(received == null)
                    break;
                hist.add(received);
            }

            // return Arraylist
            return hist;


        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 0: failed, 1: success
    public int removeChallenge(int challengeID){
        try {
            // get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            // send addUser request to server
            String command = String.format("rmCH,%d\r\n",challengeID);
            out.write(command.getBytes());

            // wait for response
            String received = in.readLine();

            // process possible failures
            if(received == null)
                return 0;
            if(received.isEmpty())
                return 0;

            // return successful if server returns successful
            if(received.matches("SUCCESS"))
                return 1;
            else
                return 0;

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
