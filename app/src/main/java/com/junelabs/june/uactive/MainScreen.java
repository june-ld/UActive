package com.junelabs.june.uactive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActiveUser aUser;
    private DataWriter writer;
    private ArrayList<ActiveChallenge> challenges;
    private ArrayList<Organization> orgs;
    private boolean isOnline = false;

    public static int CHALLENGE_ID = 0;
    public static int SHOW_ORGS = 100;
    public static int CREATE_ORG = 101;
    public static int SHOW_HIST = 102;

    public static int RESULT_DELETE = 200;

    private boolean netMode = true;
    private boolean timeToSave = false;

    public ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                if(!netMode){
                    Toast.makeText(MainScreen.this, "Must be online", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isOnline)
                    return;
                if(aUser.getName().matches("guest")){
                    Toast.makeText(MainScreen.this, "Cannot create a challenge as guest", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(MainScreen.this, CreateChallenge.class);
                startActivityForResult(intent, 1);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED); // used to disable peek gesture. if fix found, re-enable
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        writer = new DataWriter(MainScreen.this);
        aUser = writer.getCurrActiveUser();
        challenges = writer.getChallenges();
        orgs = writer.getOrganizations();
        netMode = writer.getNetMode();

        mDialog = new ProgressDialog(MainScreen.this);

        // set fields in navDrawer
        TextView view = (TextView)findViewById(R.id.showUsername);
        view.setText(aUser.getName());
        updateNavBar();

        Toast.makeText(MainScreen.this, "Hello: " + aUser.getName() + " CurrEXP: " + String.valueOf(aUser.getCurrExp()), Toast.LENGTH_SHORT).show();

        if(netMode) {
            DBConnection.enableProgressDialog(mDialog);
            new getChallengesTask().execute();
        }
        else {
            showChallenges();
        }
    }

    public void updateNavBar(){
        TextView view;
        String text;

        view = (TextView)findViewById(R.id.showLevel);
        int currentLevel = XPConstants.getLevel(aUser.getCurrExp());
        text = String.format("%s %s", getResources().getString(R.string.levelField), String.valueOf(currentLevel));
        view.setText(text);

        view = (TextView)findViewById(R.id.showExp);
        text = String.format("%s %s", getResources().getString(R.string.expField), String.valueOf(aUser.getCurrExp()));
        view.setText(text);

        view = (TextView)findViewById(R.id.showNextLevel);
        int toNext = XPConstants.getToNext(aUser.getCurrExp());
        text = String.format("%s %s", getResources().getString(R.string.nextField), String.valueOf(toNext));
        view.setText(text);
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
                updateNavBar();
            }
            if (resultCode == RESULT_DELETE){
                int index = data.getIntExtra("index", -1);
                if(index >= 0) {
                    challenges.remove(index);
                    showChallenges();
                }
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
            else if(result.matches("noChallenges")) {
                showNoChallenges();
                new getOrgsTask().execute();
            }
            else{
                // TODO: restart dialog
                showChallenges();
                new getOrgsTask().execute();
            }
        }
    }

    private void disableDialog(){
        DBConnection.disableProgressDialog(mDialog);
    }

    private void showChallenges() {
        LinearLayout container = (LinearLayout)findViewById(R.id.challengeContainer);
        LinearLayout todayContainer = (LinearLayout)findViewById(R.id.challengeTodayContainer);
        LinearLayout weekContainer = (LinearLayout)findViewById(R.id.laterWeekContainer);
        LinearLayout laterContainer = (LinearLayout)findViewById(R.id.laterContainer);

        container.removeAllViews();
        todayContainer.removeAllViews();
        weekContainer.removeAllViews();
        laterContainer.removeAllViews();


        for(int i = 0; i < challenges.size(); i++){
            LinearLayout temp = new LinearLayout(MainScreen.this);
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
                    Intent intent = new Intent(MainScreen.this, ChallengeDetails.class);
                    intent.putExtra("challengeFields", challenges.get(v.getId() - CHALLENGE_ID).getParseCode());
                    intent.putExtra("index", v.getId());
                    startActivityForResult(intent, 2);
                }
            });

            TextView tempVar = new TextView(MainScreen.this);
            tempVar.setText(challenges.get(i).getChName());
            tempVar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            tempVar.setTextColor(Color.parseColor("#FFFFFF"));
            tempVar.setPadding(10, 0, 10, 0);
            temp.addView(tempVar);

            tempVar = new TextView(MainScreen.this);
            tempVar.setText("Event Date: " + challenges.get(i).getDateFormat());
            tempVar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tempVar.setTextColor(Color.parseColor("#FFFFFF"));
            tempVar.setPadding(10, 0, 10, 0);
            temp.addView(tempVar);

            tempVar = new TextView(MainScreen.this);
            tempVar.setText("Location: " + challenges.get(i).getLocation());
            tempVar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tempVar.setTextColor(Color.parseColor("#FFFFFF"));
            tempVar.setPadding(10, 0, 10, 0);
            temp.addView(tempVar);

            if(challenges.get(i).isIfOrgChallenge()){
                //orgContainer.addView(temp);
                int position = challenges.get(i).getDateOrder();// -1 over, 1 today, 2 later this week, 3 later this year
                if(position == -1)
                    continue;

                else if (position == 1)
                    todayContainer.addView(temp);

                else if (position == 2)
                    weekContainer.addView(temp);

                else
                    laterContainer.addView(temp);
            }

            else
                container.addView(temp);
        }

        TextView showNone;
        if(container.getChildCount() == 0){
            showNone = new TextView(MainScreen.this);
            showNone.setText(getText(R.string.showNone));
            container.addView(showNone);
        }
        if(todayContainer.getChildCount() == 0){
            showNone = new TextView(MainScreen.this);
            showNone.setText(getText(R.string.showNone));
            todayContainer.addView(showNone);
        }
        if(weekContainer.getChildCount() == 0){
            showNone = new TextView(MainScreen.this);
            showNone.setText(getText(R.string.showNone));
            weekContainer.addView(showNone);
        }
        if(laterContainer.getChildCount() == 0){
            showNone = new TextView(MainScreen.this);
            showNone.setText(getText(R.string.showNone));
            laterContainer.addView(showNone);
        }



        if(isOnline)
            isOnline = false;
        if(timeToSave) {
            Toast.makeText(MainScreen.this, "Updating challenges", Toast.LENGTH_SHORT).show();
            writer.saveChallenges(challenges);
        }

    }

    private void showNoChallenges() {
        DBConnection.disableProgressDialog(mDialog);
        Toast.makeText(MainScreen.this, "No challenges to show", Toast.LENGTH_SHORT).show();
        challenges = new ArrayList<>();
        isOnline = false;
    }

    public void showNoConnection(){
        DBConnection.disableProgressDialog(mDialog);
        Toast.makeText(MainScreen.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
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
            else if(result.matches("noOrgs")) {
                orgs = new ArrayList<>();
                disableDialog();
            }
            else{
                if(orgs.size() != writer.getOrgSize())
                    writer.saveOrganizations(orgs);
                disableDialog();
            }
        }
    }

    private Toast toast;
    private long lastBackPressTime = 0;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (lastBackPressTime < ( System.currentTimeMillis() - 3500 )){
                toast = Toast.makeText(this, "Press back again to close this app", Toast.LENGTH_LONG);
                toast.show();
                this.lastBackPressTime = System.currentTimeMillis();
            } else {
                if (toast != null) {
                    toast.cancel();
                }
                finish();
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.nav_myOrgs) {
            intent = new Intent(MainScreen.this, MyOrgs.class);
            startActivityForResult(intent, SHOW_ORGS);
        } else if (id == R.id.nav_goTo) {

        } else if (id == R.id.nav_createOrg) {
            if(netMode){
                intent = new Intent(MainScreen.this, AddOrg.class);
                startActivityForResult(intent, CREATE_ORG);
            }
            else
                Toast.makeText(MainScreen.this, "Must be online", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_history) {
            if(netMode){
                intent = new Intent(MainScreen.this, History.class);
                startActivityForResult(intent, SHOW_HIST);
            }
            else
                Toast.makeText(MainScreen.this, "Must be online", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_manage) {
            Toast.makeText(MainScreen.this, "Not yet Implemented", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            intent = new Intent(MainScreen.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
