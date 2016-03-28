package com.junelabs.june.uactive;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class HomeScreen extends AppCompatActivity {

    private ActiveUser aUser;
    private DataWriter writer;
    private ArrayList<ActiveChallenge> challenges;
    private ArrayList<Organization> orgs;
    private boolean isOnline = false;

    private static int CHALLENGE_ID = 0;

    private boolean netMode = true;
    private boolean timeToSave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_drawer);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                if(!netMode){
                    Toast.makeText(HomeScreen.this, "Must be online", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isOnline)
                    return;
                if(aUser.getName().matches("guest")){
                    Toast.makeText(HomeScreen.this, "Cannot create a challenge as guest", Toast.LENGTH_SHORT).show();
                    return;
                }


                Intent intent = new Intent(HomeScreen.this, CreateChallenge.class);
                startActivityForResult(intent, 1);
            }
        });


        writer = new DataWriter(HomeScreen.this);
        aUser = writer.getCurrActiveUser();
        challenges = writer.getChallenges();
        orgs = writer.getOrganizations();
        netMode = writer.getNetMode();

        Toast.makeText(HomeScreen.this, "Hello: " + aUser.getName() + " CurrEXP: " + String.valueOf(aUser.getCurrExp()), Toast.LENGTH_SHORT).show();

        if(netMode)
            new getChallengesTask().execute();
        else {
            showChallenges();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                challenges.add(new ActiveChallenge(data.getStringExtra("challengeFields")));
                showChallenges();
            }
        }
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                aUser = writer.getCurrActiveUser();
                Toast.makeText(HomeScreen.this, "CurrEXP: " + String.valueOf(aUser.getCurrExp()), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class getChallengesTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;

        @Override
        protected String doInBackground(String... params) {
            isOnline = true;
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            // check if there's been an update to challenges
            ArrayList<ActiveChallenge> temp = connection.getAllChallenges();
            if(challenges.size() != temp.size())
                timeToSave = true;

            challenges = temp;

            if(challenges.size() == 0)
                return "noChallenges";

            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.matches("noConnection"))
                showNoConnection();
            else if(result.matches("noChallenges"))
                showNoChallenges();
            else{
                // TODO: save challenges
                showChallenges();
                new getOrgsTask().execute();
            }
        }
    }

    private void showChallenges() {
        LinearLayout container = (LinearLayout)findViewById(R.id.challengeContainer);
        LinearLayout orgContainer = (LinearLayout)findViewById(R.id.challengeOrgContainer);
        container.removeAllViews();
        orgContainer.removeAllViews();

        for(int i = 0; i < challenges.size(); i++){
            LinearLayout temp = new LinearLayout(HomeScreen.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 10);
            temp.setLayoutParams(lp);
            temp.setOrientation(LinearLayout.VERTICAL);
            temp.setBackgroundColor(Color.parseColor("#E10E49"));
            temp.setId(CHALLENGE_ID + i);
            temp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(HomeScreen.this, "Challenge ID: " + String.valueOf(v.getId() - CHALLENGE_ID), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HomeScreen.this, ChallengeDetails.class);
                    intent.putExtra("challengeFields", challenges.get(v.getId() - CHALLENGE_ID).getParseCode());
                    startActivityForResult(intent, 2);
                }
            });

            TextView tempVar = new TextView(HomeScreen.this);
            tempVar.setText(challenges.get(i).getChName());
            tempVar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            tempVar.setTextColor(Color.parseColor("#FFFFFF"));
            tempVar.setPadding(10, 0, 10, 0);
            temp.addView(tempVar);

            tempVar = new TextView(HomeScreen.this);
            tempVar.setText("Event Date: " + challenges.get(i).getEventDate());
            tempVar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tempVar.setTextColor(Color.parseColor("#FFFFFF"));
            tempVar.setPadding(10, 0, 10, 0);
            temp.addView(tempVar);

            tempVar = new TextView(HomeScreen.this);
            tempVar.setText("Location: " + challenges.get(i).getLocation());
            tempVar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tempVar.setTextColor(Color.parseColor("#FFFFFF"));
            tempVar.setPadding(10, 0, 10, 0);
            temp.addView(tempVar);

            if(challenges.get(i).isIfOrgChallenge())
                orgContainer.addView(temp);
            else
                container.addView(temp);
        }

        if(isOnline)
            isOnline = false;
        if(timeToSave) {
            Toast.makeText(HomeScreen.this, "Updating challenges", Toast.LENGTH_SHORT).show();
            writer.saveChallenges(challenges);
        }

    }

    private void showNoChallenges() {
        Toast.makeText(HomeScreen.this, "No challenges to show", Toast.LENGTH_SHORT).show();
        challenges = new ArrayList<>();
        isOnline = false;
    }

    public void showNoConnection(){
        Toast.makeText(HomeScreen.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
        challenges = new ArrayList<>();
        isOnline = false;
    }

    private class getOrgsTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;

        @Override
        protected String doInBackground(String... params) {
            isOnline = true;
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            orgs = connection.getUserOrgList(aUser.getUserID());

            if(challenges == null)
                return "noOrgs";

            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            isOnline = false;
            if(result.matches("noConnection"))
                showNoConnection();
            else if(result.matches("noOrgs"))
                orgs = new ArrayList<>();
            else{
                if(orgs.size() != writer.getOrgSize())
                    writer.saveOrganizations(orgs);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if ( id == android.R.id.home){
            Toast.makeText(HomeScreen.this, "Home Clicked", Toast.LENGTH_SHORT).show();
            //Intent intent = new Intent(HomeScreen.this, navigationPane.class);
            //startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
