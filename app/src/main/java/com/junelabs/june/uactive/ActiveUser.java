package com.junelabs.june.uactive;

/**
 * Created by June on 3/11/2016.
 */
public class ActiveUser {
    public static int USERNAME = 10;
    public static int PASSWORD = 20;
    public static int EMAILADDR = 30;
    public static int SECURITYQUES = 40;
    public static int CURREXP = 50;

    private int userID;
    private String name;
    private String password;
    private String emailAddr;
    private String securityQuestionAns;
    private int currExp;

    public ActiveUser(int userID, String name, String password, String emailAddr, String securityQuestionAns, int currExp){
        this.userID = userID;
        this.name = name;
        this.password = password;
        this.emailAddr = emailAddr;
        this.securityQuestionAns = securityQuestionAns;
        this.currExp = currExp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public String getSecurityQuestionAns() {
        return securityQuestionAns;
    }

    public void setSecurityQuestionAns(String securityQuestionAns) {
        this.securityQuestionAns = securityQuestionAns;
    }

    public int getCurrExp() {
        return currExp;
    }

    public void setCurrExp(int currExp) {
        this.currExp = currExp;
    }

    public static ActiveUser createUserFromString(String parseString){
        String[] response = parseString.split(",");
        return new ActiveUser(Integer.valueOf(response[0]), response[1], response[2], response[3], response[4], Integer.valueOf(response[5]));
    }
}
