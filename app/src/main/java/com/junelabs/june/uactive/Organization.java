package com.junelabs.june.uactive;

/**
 * Created by June on 3/15/2016.
 */
public class Organization {
    private int orgID;
    private String orgName;
    private int passCode;

    private String parseCode;

    public static int VIEW_ID = 300;


    public Organization(int orgID, String orgName, int passCode) {
        this.orgID = orgID;
        this.orgName = orgName;
        this.passCode = passCode;

        parseCode = String.format("%d,%s,%d", orgID, orgName, passCode);
    }

    public Organization(String parseCode) {
        String[] fields = parseCode.split(",");

        this.orgID = Integer.valueOf(fields[0]);
        this.orgName = fields[1];
        this.passCode = Integer.valueOf(fields[2]);
        this.parseCode = parseCode;
    }

    public int getOrgID() {
        return orgID;
    }

    public String getOrgName() {
        return orgName;
    }

    public int getPassCode() {
        return passCode;
    }

    public String getParseCode() {
        return parseCode;
    }
}
