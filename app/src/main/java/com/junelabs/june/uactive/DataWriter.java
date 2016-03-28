package com.junelabs.june.uactive;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by June on 3/11/2016.
 */
public class DataWriter {
    Context context;

    public DataWriter(Context context){
        this.context = context;
    }

    public void saveActiveUser(ActiveUser currentUser){
        // get shared prefences editor
        SharedPreferences.Editor editor = context.getSharedPreferences("currentUser", Context.MODE_PRIVATE).edit();

        // save fields of current user to memory
        editor.putInt("userID", currentUser.getUserID());
        editor.putString("username", currentUser.getName());
        editor.putString("password", currentUser.getPassword());
        editor.putString("email", currentUser.getEmailAddr());
        editor.putString("secAns", currentUser.getSecurityQuestionAns());
        editor.putInt("currentEXP", currentUser.getCurrExp());

        // commit to memory
        editor.apply();
    }

    public ActiveUser getCurrActiveUser(){
        // get SP
        SharedPreferences retrievedUser = context.getSharedPreferences("currentUser", Context.MODE_PRIVATE);

        // return null if pref does not exist
        if(retrievedUser == null)
            return null;

        // return current/ or last logged user
        return new ActiveUser(retrievedUser.getInt("userID", 0),
                retrievedUser.getString("username", "guest"),
                retrievedUser.getString("password", "password"),
                retrievedUser.getString("email", "invalid"),
                retrievedUser.getString("secAns", "none"),
                retrievedUser.getInt("currentEXP", 0));
    }

    public void saveCurrentChallenge(String parseString){
        // get shared prefences editor
        SharedPreferences.Editor editor = context.getSharedPreferences("challenges", Context.MODE_PRIVATE).edit();

        // save fields of current user to memory
        editor.putString("currentChallenge", parseString);

        // commit to memory
        editor.apply();
    }

    public ActiveChallenge getCurrentChallenge(){
        // get SP
        SharedPreferences retrievedChallenges = context.getSharedPreferences("challenges", Context.MODE_PRIVATE);

        // return null if pref does not exist
        if(retrievedChallenges == null)
            return null;

        // get the string from memory
        String challengeString = retrievedChallenges.getString("currentChallenge", null);

        // if null, return; else, return ActiveChallenge from string
        if(challengeString == null)
            return null;
        else
            return new ActiveChallenge(challengeString);

    }

    public void saveChallenges(String[] challenges){
        // get shared prefences editor
        SharedPreferences.Editor editor = context.getSharedPreferences("challenges", Context.MODE_PRIVATE).edit();

        // save total num of challenges
        int size = challenges.length;
        editor.putInt("size", size);

        // save fields of current user to memory
        for(int i = 0; i < size; i++){
            String num = String.valueOf(i);
            editor.putString("Challenge" + num, challenges[i]);
        }

        // commit to memory
        editor.apply();
    }

    public void saveChallenges(ArrayList<ActiveChallenge> challenges){
        String[] challengeStrings = new String[challenges.size()];
        for(int i = 0; i < challengeStrings.length; i++){
            challengeStrings[i] = challenges.get(i).getParseCode();
        }

        saveChallenges(challengeStrings);
    }

    public ArrayList<ActiveChallenge> getChallenges(){
        // get SP
        SharedPreferences retrievedChallenges = context.getSharedPreferences("challenges", Context.MODE_PRIVATE);

        // return null if pref does not exist
        if(retrievedChallenges == null)
            return new ArrayList<>();

        // get number of challenges
        int size = retrievedChallenges.getInt("size", 0);
        if(size == 0)
            return new ArrayList<>();

        // get the challenges and add to arraylist
        ArrayList<ActiveChallenge> challenges = new ArrayList<>();
        for(int i = 0; i < size; i ++) {
            String num = String.valueOf(i);
            challenges.add(new ActiveChallenge(retrievedChallenges.getString("Challenge" + num, "ERROR")));
        }

        return challenges;
    }

    public void saveOrganizations(String[] organizations){
        // get shared prefences editor
        SharedPreferences.Editor editor = context.getSharedPreferences("orgs", Context.MODE_PRIVATE).edit();

        // save total num of challenges
        int size = organizations.length;
        editor.putInt("size", size);

        // save fields of current user to memory
        for(int i = 0; i < size; i++){
            String num = String.valueOf(i);
            editor.putString("org" + num, organizations[i]);
        }

        // commit to memory
        editor.apply();
    }

    public void saveOrganizations(ArrayList<Organization> organizations){
        String[] orgStrings = new String[organizations.size()];
        for(int i = 0; i < orgStrings.length; i++){
            orgStrings[i] = organizations.get(i).getParseCode();
        }

        saveOrganizations(orgStrings);
    }

    public int getOrgSize(){
        SharedPreferences retrievedChallenges = context.getSharedPreferences("orgs", Context.MODE_PRIVATE);
        return retrievedChallenges.getInt("size", -1);
    }

    public ArrayList<Organization> getOrganizations(){

        ArrayList<Organization> orgs = new ArrayList<>();

        // get SP
        SharedPreferences retreivedOrgs = context.getSharedPreferences("orgs", Context.MODE_PRIVATE);

        // return null if pref does not exist
        if(retreivedOrgs == null)
            return orgs;

        // get number of challenges
        int size = retreivedOrgs.getInt("size", 0);
        if(size == 0)
            return orgs;

        // get the challenges and add to arraylist

        for(int i = 0; i < size; i ++) {
            String num = String.valueOf(i);
            orgs.add(new Organization(retreivedOrgs.getString("org" + num, "ERROR")));
        }

        return orgs;
    }

    public void saveNetMode(boolean NetMode){
        // get shared prefences editor
        SharedPreferences.Editor editor = context.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit();

        // save the online/offline status
        editor.putBoolean("netMode", NetMode);

        // commit to memory
        editor.apply();
    }

    public boolean getNetMode(){
        // get SP
        SharedPreferences retreivedSettings = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);

        return retreivedSettings.getBoolean("netMode", true);
    }


}
