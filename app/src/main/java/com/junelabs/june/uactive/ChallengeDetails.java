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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChallengeDetails extends AppCompatActivity {

    ActiveChallenge challenge;
    ActiveUser currentUser;
    DataWriter writer;

    ArrayList<Comment> comments;
    ArrayList<Organization> orgs;
    boolean isOnline = false;
    boolean isAdmin = false;
    int checkInCount = 0;

    boolean netMode = true;

    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        challenge = new ActiveChallenge(getIntent().getStringExtra("challengeFields"));
        writer = new DataWriter(ChallengeDetails.this);
        currentUser = writer.getCurrActiveUser();
        orgs = writer.getOrganizations();
        mDialog = new ProgressDialog(ChallengeDetails.this);

        // check to see if user is admin
        if(challenge.getHostName().matches(currentUser.getName()))
            isAdmin = true;
        for(int i = 0; i < orgs.size(); i++){
            if(challenge.getOrgName().matches(orgs.get(i).getOrgName())) {
                isAdmin = true;
                break;
            }
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (netMode && isAdmin) {
                    DBConnection.enableProgressDialog(mDialog);
                    new removeChallengeTask().execute();
                }
                else if(netMode)
                    Toast.makeText(ChallengeDetails.this, "Cannot delete: Not Admin", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(ChallengeDetails.this, "Must be online", Toast.LENGTH_SHORT).show();
            }
        });

        TextView temp = (TextView)findViewById(R.id.detailTitle);
        temp.setText(challenge.getChName());
        temp = (TextView)findViewById(R.id.detailTime);
        temp.setText(challenge.getEventDate());
        temp = (TextView)findViewById(R.id.detailLocation);
        temp.setText(challenge.getLocation());
        temp = (TextView)findViewById(R.id.detailDescription);
        temp.setText(challenge.getDetails());
        temp = (TextView)findViewById(R.id.detailHost);
        if(!challenge.isIfOrgChallenge())
            temp.setText("Created by - " + challenge.getHostName());
        else
            temp.setText("Created by - " + challenge.getOrgName());


        netMode = writer.getNetMode();

        if(netMode) {
            DBConnection.enableProgressDialog(mDialog);
            new getCommentsTask().execute();
            new getChCountTask().execute();
        }

        /*
        temp = (TextView)findViewById(R.id.checkInCount);
        temp.setText(String.valueOf(currChallenge.getUsersCheckedIn().size()));


        LinearLayout comContainer = (LinearLayout)findViewById(R.id.commentContainer);
        for(int i = 0; i < challenges.get(challengeIndex).getComments().size(); i++){
            TextView comment = new TextView(challengeDetails.this);
            comment.setText(challenges.get(challengeIndex).getComments().get(i));
            comment.setBackgroundColor(getResources().getColor(R.color.lightGrey));
            comContainer.addView(comment);
        }
        */
    }

    public void onButtonClick(View view) {
        if(!netMode){
            Toast.makeText(ChallengeDetails.this, "Must be online", Toast.LENGTH_SHORT).show();
            return;
        }
        if(currentUser.getUserID() == -1){
            Toast.makeText(ChallengeDetails.this, "Must be logged in to comment", Toast.LENGTH_SHORT).show();
            return;
        }



        View addCommentView = findViewById(R.id.commentInputContainer);
        if(addCommentView.getVisibility() == View.GONE)
            addCommentView.setVisibility(View.VISIBLE);
        else
            addCommentView.setVisibility(View.GONE);
    }

    public void onSubmitComment(View view) {
        if(!netMode){
            Toast.makeText(ChallengeDetails.this, "Must be online", Toast.LENGTH_SHORT).show();
            return;
        }
        if(isOnline)
            return;

        EditText commentView = (EditText)findViewById(R.id.commentInput);
        String commentText = commentView.getText().toString().trim();

        String command = String.format("%d,%s,%s", challenge.getChallengeID(), currentUser.getUserID(), commentText);
        DBConnection.enableProgressDialog(mDialog);
        new addCommentTask().execute(command);
    }

    public void onCheckIn(View view) {
        if(!netMode){
            Toast.makeText(ChallengeDetails.this, "Must be online", Toast.LENGTH_SHORT).show();
            return;
        }
        if(isOnline)
            return;
        if(currentUser.getName().matches("guest")){
            Toast.makeText(ChallengeDetails.this, "Guests cannot check in!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent;
        if(isAdmin) {
            intent = new Intent(ChallengeDetails.this, CheckInAdmin.class);
            intent.putExtra("challengeFields", challenge.getParseCode());
            startActivityForResult(intent, 2);
        }
        else {
            intent = new Intent(ChallengeDetails.this, CheckInUser.class);
            intent.putExtra("challengeFields", challenge.getParseCode());
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Intent returnIntent = getIntent();
                setResult(Activity.RESULT_OK, returnIntent);
                //isOnline = false;
                finish();
            }
        }
        if(requestCode == 2){
            if (resultCode == RESULT_OK) {
                Intent returnIntent = getIntent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                //isOnline = false;
                finish();
            }
        }
    }

    private class addCommentTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;
        String addStatus;

        @Override
        protected String doInBackground(String... params) {
            isOnline = true;
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            addStatus = connection.addComment(params[0]);

            if(addStatus == null)
                return "ADD_FAILED";
            else if(addStatus.isEmpty())
                return "ADD_FAILED";
            else {
                return "ADD_SUCCESS";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            isOnline = false;
            if(result.matches("noConnection"))
                showNoConnection();
            else if(result.matches("ADD_FAILED"))
                showAddFail();
            else {
                View addCommentView = findViewById(R.id.commentInputContainer);
                EditText text = (EditText) findViewById(R.id.commentInput);
                text.setText("");
                addCommentView.setVisibility(View.GONE);
                comments.add(new Comment(addStatus));
                showComments();
            }

            disableDialog();
        }
    }

    private void showComments() {
        if(comments.size() == 0)
            return;

        LinearLayout commentContainer = (LinearLayout)findViewById(R.id.commentContainer);
        commentContainer.removeAllViews();

        for(int i = 0; i < comments.size(); i++){
            TextView commentView = new TextView(ChallengeDetails.this);
            commentView.setPadding(10,0,10,0);
            commentView.setText(String.format("%s -%s", comments.get(i).getText(),comments.get(i).getUser()));
            commentContainer.addView(commentView);
        }
    }

    private void showAddFail() {
        Toast.makeText(ChallengeDetails.this, "Cannot add comments at this time", Toast.LENGTH_SHORT).show();
    }

    private void showNoConnection() {
        Toast.makeText(ChallengeDetails.this, "Cannot connect at this time", Toast.LENGTH_SHORT).show();
    }

    private class getCommentsTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;

        @Override
        protected String doInBackground(String... params) {
            isOnline = true;
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            comments = connection.getComments(challenge.getChallengeID());

            if(comments == null)
                return "noComments";

            int countStatus = connection.checkInCount(challenge.getChallengeID());
            if (countStatus > 0) {
                checkInCount = countStatus;
            }

            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            isOnline = false;
            if(result.matches("noConnection"))
                showNoConnection();
            else if(result.matches("noComments"))
                showNoComments();
            else{
                showComments();
            }

            disableDialog();
        }
    }

    private class getChCountTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;

        @Override
        protected String doInBackground(String... params) {
            isOnline = true;
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            int countStatus = connection.checkInCount(challenge.getChallengeID());
            if (countStatus > 0) {
                checkInCount = countStatus;
                return "Complete";
            }
            else
                return "doNothing";
        }

        @Override
        protected void onPostExecute(String result) {
            isOnline = false;
            if(result.matches("noConnection"))
                showNoConnection();
            else if (result.matches("Complete")){
                updateCount();
            }

            disableDialog();
        }
    }

    private void updateCount() {
        TextView view = (TextView)findViewById(R.id.checkInCount);
        view.setText(String.valueOf(checkInCount));
    }

    private void showNoComments() {
        Toast.makeText(ChallengeDetails.this, "No comments to show", Toast.LENGTH_SHORT).show();
    }

    private class removeChallengeTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;

        @Override
        protected String doInBackground(String... params) {
            isOnline = true;
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            int countStatus = connection.removeChallenge(challenge.getChallengeID());
            if (countStatus > 0) {
                checkInCount = countStatus;
                return "Complete";
            }
            else
                return "ERROR";
        }

        @Override
        protected void onPostExecute(String result) {
            isOnline = false;
            if(result.matches("noConnection"))
                showNoConnection();
            else if (result.matches("Complete")){
                setDelete();
            }
            else
                cannotDelete();

            disableDialog();
        }
    }

    private void disableDialog(){
        mDialog.cancel();
    }

    private void setDelete() {
        Intent returnIntent = getIntent();
        setResult(MainScreen.RESULT_DELETE, returnIntent);
        finish();
    }

    private void cannotDelete() {
        Toast.makeText(ChallengeDetails.this, "Could not delete, Try again later", Toast.LENGTH_SHORT).show();
    }
}
