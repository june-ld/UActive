package com.junelabs.june.uactive;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by June on 3/11/2016.
 */
public class ActiveChallenge {
    private int challengeID;
    private String chName;
    private String eventDate;
    private String createdDate;
    private String location;
    private String hostName;
    private boolean ifOrgChallenge;
    private String details;
    private int expVal;
    private int upVotes;
    private int checkinCode;
    private String orgName;
    private String endTime;

    private String parseCode;

    public ActiveChallenge(int challengeID, String chName, String eventDate, String createdDate, String location, String hostName, boolean ifOrgChallenge, String details, int expVal, int upvotes, int checkinCode, String orgName, String endTime){
        this.challengeID = challengeID;
        this.chName = chName;
        this.eventDate = eventDate;
        this.createdDate = createdDate;
        this.location = location;
        this.hostName = hostName;
        this.ifOrgChallenge = ifOrgChallenge;
        this.details = details;
        this.expVal = expVal;
        this.upVotes = upvotes;
        this.checkinCode = checkinCode;
        this.orgName = orgName;
        this.endTime = endTime;

        if(ifOrgChallenge)
            parseCode = String.format("%d,%s,%s,%s,%s,%s,1,%s,%d,%d,%d,%s", challengeID, chName, eventDate, createdDate, location, hostName, details, expVal, upvotes, checkinCode, orgName);
        else
            parseCode = String.format("%d,%s,%s,%s,%s,%s,0,%s,%d,%d,%d,%s", challengeID, chName, eventDate, createdDate, location, hostName, details, expVal, upvotes, checkinCode, orgName);
    }

    public ActiveChallenge(String parseString){
        String[] tok = parseString.split(",");

        this.challengeID = Integer.valueOf(tok[0]);
        this.chName = tok[1];
        this.eventDate = tok[2];
        this.createdDate = tok[3];
        this.location = tok[4];
        this.hostName = tok[5];
        if(Integer.valueOf(tok[6]) == 0)
            this.ifOrgChallenge = false;
        else
            this.ifOrgChallenge = true;
        this.details = tok[7];
        this.expVal = Integer.valueOf(tok[8]);
        this.upVotes = Integer.valueOf(tok[9]);
        this.checkinCode = Integer.valueOf(tok[10]);
        this.orgName = tok[11];
        this.endTime = tok[12];
        this.parseCode = parseString;

    }


    public int getChallengeID() {
        return challengeID;
    }

    public String getChName() {
        return chName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHostName() {
        return hostName;
    }

    public boolean isIfOrgChallenge() {
        return ifOrgChallenge;
    }

    public void setIfOrgChallenge(boolean ifOrgChallenge) {
        this.ifOrgChallenge = ifOrgChallenge;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getExpVal() {
        return expVal;
    }

    public void setExpVal(int expVal) {
        this.expVal = expVal;
    }

    public int getUpVotes() {
        return upVotes;
    }

    public void setUpVotes(int upVotes) {
        this.upVotes = upVotes;
    }

    public int getCheckinCode() {
        return checkinCode;
    }

    public void setCheckinCode(int checkinCode) {
        this.checkinCode = checkinCode;
    }

    public String getParseCode() {
        return parseCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Calendar getCalEventTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm", Locale.ENGLISH);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse(this.eventDate));
            return cal;
        } catch (ParseException e) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR,1);
            return cal;
        }
    }

    public Calendar getCalEndTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm", Locale.ENGLISH);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse(this.endTime));
            return cal;
        } catch (ParseException e) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR,1);
            return cal;
        }
    }



    public String getDateFormat(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE',' MMM d '\nfrom' HH:mm aa ", Locale.ENGLISH);
        SimpleDateFormat endTime = new SimpleDateFormat("'to' HH:mm aa", Locale.ENGLISH);
        return dateFormat.format(getCalEventTime().getTime()) + endTime.format(getCalEndTime().getTime());
    }

    // -1 over, 1 today, 2 later this week, 3 later this year
    public int getDateOrder(){
        Calendar day = Calendar.getInstance();
        Calendar eventTime = getCalEventTime();

        if(eventTime.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                eventTime.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR))
            return 1;

        day.add(Calendar.WEEK_OF_YEAR, 1);
        if(eventTime.before(day)){
            return 2;
        }
        else
            return 3;

    }
}
