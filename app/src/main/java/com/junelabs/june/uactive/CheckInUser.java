package com.junelabs.june.uactive;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CheckInUser extends AppCompatActivity {

    ActiveChallenge challenge;
    ActiveUser currentUser;
    DataWriter writer;
    boolean clicked;
    boolean isOnline = false;
    boolean checkedIn = true;

    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        writer = new DataWriter(CheckInUser.this);
        challenge = new ActiveChallenge(getIntent().getStringExtra("challengeFields"));
        currentUser = writer.getCurrActiveUser();
        mDialog = new ProgressDialog(CheckInUser.this);

        TextView view = (TextView)findViewById(R.id.checkInTitle);
        view.setText(challenge.getChName());

        DBConnection.enableProgressDialog(mDialog);
        new getChStatusTask().execute();
    }

    public void onCheckIn(View view) {
        if(clicked)
            return;
        if(isOnline)
            return;
        if(checkedIn){
            Toast.makeText(CheckInUser.this, "Already checked in!", Toast.LENGTH_SHORT).show();
            return;
        }


        EditText codeContainer = (EditText)findViewById(R.id.codeInput);
        String inputCode = codeContainer.getText().toString().trim();


        if(inputCode.isEmpty())
            Toast.makeText(CheckInUser.this, "Must enter a code", Toast.LENGTH_SHORT).show();
        else if (inputCode.matches(String.valueOf(challenge.getCheckinCode()))) {
            DBConnection.enableProgressDialog(mDialog);
            new checkInTask().execute();
        }
        else
            Toast.makeText(CheckInUser.this, "Incorrect code", Toast.LENGTH_SHORT).show();
    }

    private class updateUserTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;
        String status;

        @Override
        protected String doInBackground(String... params) {
            connection = new DBConnection();

            if(!connection.getConnectionStatus())
                return "noConnection";

            status = connection.updateUserEXP(currentUser.getUserID(), currentUser.getCurrExp() + challenge.getExpVal());
            if(status.matches("UPDATE_FAILED"))
                return "FAILED";
            if(status.matches("BAD_REQUEST"))
                return "FAILED";


            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.matches("noConnection"))
                showNoConnection();
            else if(result.matches("FAILED"))
                showFailed();
            else{
                showSuccess();
            }

            disableDialog();
        }
    }

    private void disableDialog(){
        mDialog.cancel();
    }

    private void showSuccess() {
        currentUser.setCurrExp(currentUser.getCurrExp() + challenge.getExpVal());
        TextView challengeExpShow = (TextView)findViewById(R.id.expGainView);
        challengeExpShow.setText(String.valueOf(challenge.getExpVal()) + " XP");
        writer.saveActiveUser(currentUser);
        View confirmView = findViewById(R.id.afterCheckNotif);
        confirmView.setVisibility(View.VISIBLE);
        isOnline = false;
    }

    private void showFailed() {
        Toast.makeText(CheckInUser.this, "Cannot check in now, Try again later", Toast.LENGTH_SHORT).show();
    }

    private void showNoConnection() {
        Toast.makeText(CheckInUser.this, "Cannot check in now, Try again later", Toast.LENGTH_SHORT).show();
    }



    public void setReturn(View v) {
        if(isOnline)
            return;
        Intent returnIntent = getIntent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void setFailedReturn() {
        Intent returnIntent = getIntent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        isOnline = false;
        finish();
    }

    private class getChStatusTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;

        @Override
        protected String doInBackground(String... params) {
            isOnline = true;
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            int countStatus = connection.checkInStatus(currentUser.getUserID(), challenge.getChallengeID());
            checkedIn = countStatus > 0 || countStatus < 0;

            if(checkedIn)
                return "t";
            else
                return "f";
        }

        @Override
        protected void onPostExecute(String result) {
            isOnline = false;
            if(result.matches("noConnection"))
                showNoConnection();
            else if (result.matches("t"))
                showCheckedIn();

            disableDialog();
        }
    }

    private void showCheckedIn() {
        Toast.makeText(CheckInUser.this, "Already checked in!", Toast.LENGTH_SHORT).show();
    }

    private class checkInTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;

        @Override
        protected String doInBackground(String... params) {
            isOnline = true;
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            String status = connection.checkIn(currentUser.getUserID(), challenge.getChallengeID());
            if(status.matches("ADD_FAILED"))
                return "ADD_FAILED";

            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            isOnline = false;
            if(result.matches("Complete")){

                new updateUserTask().execute();
            }
            else {
                disableDialog();
                showNoConnection();
            }


        }
    }
}
