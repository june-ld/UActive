package com.junelabs.june.uactive;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class CreateChallenge extends AppCompatActivity {

    boolean perChallenge = true;
    boolean orgChallenge = false;

    boolean isOnline = false;

    ActiveUser currentUser;
    ArrayList<Organization> orgs;
    DataWriter writer;

    int orgIndex  = -1;
    ProgressDialog mDialog;

    public static final int DURATION_1 = 1;
    public static final int DURATION_2 = 2;
    public static final int DURATION_3 = 3;
    public static final int DURATION_4 = 4;
    public static final int DURATION_5 = 5;

    private int duration = 0;

    Calendar startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        writer = new DataWriter(CreateChallenge.this);
        currentUser = writer.getCurrActiveUser();
        mDialog = new ProgressDialog(CreateChallenge.this);

        orgs = writer.getOrganizations();
        showAvailableOrgs();


    }

    public void selectType(View view) {
        View toggle = findViewById(R.id.AdminIdContainer);
        Button perbutton = (Button)findViewById(R.id.pSelect);
        Button orgbutton = (Button)findViewById(R.id.orgSelect);

        switch (view.getId()){
            case R.id.pSelect:
                perChallenge = true;
                orgChallenge = false;

                toggle.setVisibility(View.GONE);
                perbutton.setBackgroundColor(getResources().getColor(R.color.red));
                perbutton.setTextColor(getResources().getColor(R.color.white));
                orgbutton.setBackgroundColor(getResources().getColor(R.color.white));
                orgbutton.setTextColor(Color.parseColor("#000000"));
                break;

            case R.id.orgSelect:
                perChallenge = false;
                orgChallenge = true;


                toggle.setVisibility(View.VISIBLE);
                orgbutton.setBackgroundColor(getResources().getColor(R.color.red));
                orgbutton.setTextColor(getResources().getColor(R.color.white));
                perbutton.setBackgroundColor(getResources().getColor(R.color.white));
                perbutton.setTextColor(Color.parseColor("#000000"));
                break;

        }
    }

    public void showAvailableOrgs(){
        final LinearLayout availableOrgs = (LinearLayout)findViewById(R.id.AdminIdContainer);
        availableOrgs.removeAllViews();

        for(int i = 0; i < orgs.size(); i++){
            Button temp = new Button(CreateChallenge.this);
            temp.setText(orgs.get(i).getOrgName());
            temp.setId(Organization.VIEW_ID + i);

            temp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orgIndex = v.getId() - Organization.VIEW_ID;
                    for (int i = 0; i < availableOrgs.getChildCount(); i++) {
                        Button button;
                        if (i != orgIndex) {
                            button = (Button) availableOrgs.getChildAt(i);
                            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                            button.setTextColor(getResources().getColor(R.color.black));
                        } else {
                            button = (Button) availableOrgs.getChildAt(i);
                            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                            button.setTextColor(getResources().getColor(R.color.red));
                        }
                    }
                }
            });

            availableOrgs.addView(temp);
        }

    }

    // TODO: if a single quote is used, be sure to add a second single quote
    public void onButtonClick(View view) {
        if(isOnline)
            return;

        EditText holder1 = (EditText)findViewById(R.id.enterTitle);

        String title = holder1.getText().toString().trim();
        if(title.isEmpty()){
            Toast.makeText(CreateChallenge.this, "Enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if(orgChallenge){
            if(orgIndex < 0){
                Toast.makeText(CreateChallenge.this, "Select an organization!", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        holder1 = (EditText)findViewById(R.id.enterLocation);
        String location = holder1.getText().toString().trim();
        if(location.isEmpty()){
            Toast.makeText(CreateChallenge.this, "Enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        holder1 = (EditText)findViewById(R.id.enterMonth);
        String month = holder1.getText().toString().trim();

        holder1 = (EditText)findViewById(R.id.enterDay);
        String day = holder1.getText().toString().trim();

        holder1 = (EditText)findViewById(R.id.enterYear);
        String year = holder1.getText().toString().trim();

        holder1 = (EditText)findViewById(R.id.enterHour);
        String hour = holder1.getText().toString().trim();

        holder1 = (EditText)findViewById(R.id.enterMinute);
        String minute = holder1.getText().toString().trim();

        if(month.isEmpty() || day.isEmpty() || year.isEmpty()){
            Toast.makeText(CreateChallenge.this, "Enter a valid date", Toast.LENGTH_SHORT).show();
            return;
        }

        if(hour.isEmpty() || minute.isEmpty()){
            Toast.makeText(CreateChallenge.this, "Enter a valid time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Integer.valueOf(month) < 10)
            month = "0" + month;
        if(Integer.valueOf(day) < 10)
            day = "0" + day;
        if(Integer.valueOf(hour) < 10)
            hour = "0" + hour;

        String date = String.format("%s-%s-%s %s:%s", month, day, year, hour, minute);
        if(!isValidTime(date)){
            Toast.makeText(CreateChallenge.this, "Not a valid date", Toast.LENGTH_SHORT).show();
            return;
        }

        String endTime = getEndingTime();

        holder1 = (EditText)findViewById(R.id.enterDescription);
        String description = holder1.getText().toString().trim();
        if(description.isEmpty())
            description = "No details given";

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm", Locale.ENGLISH);
        String created = dateFormat.format(Calendar.getInstance().getTime());

        Random rand = new Random();
        int code = rand.nextInt((999999 - 100000) + 1) + 100000;

        String command;
        if(!orgChallenge)
            command = String.format("addChallenge,%s,%s,%s,%s,%d,%d,%s,%d,%d,%d,%d,%s\n", title, date, created, location, currentUser.getUserID(), 0, description, 50, 0, code, 0, endTime);
        else{
            command = String.format("addChallenge,%s,%s,%s,%s,%d,%d,%s,%d,%d,%d,%d,%s\n", title, date, created, location, 0, 1, description, 50, 0, code, orgs.get(orgIndex).getOrgID(),endTime);
        }

        DBConnection.enableProgressDialog(mDialog);
        new addChallengeTask().execute(command);
    }

    public void setDuration(View view) {
        Button time1 = (Button)findViewById(R.id.time1);
        Button time2 = (Button)findViewById(R.id.time2);
        Button time3 = (Button)findViewById(R.id.time3);
        Button time4 = (Button)findViewById(R.id.time4);
        Button time5 = (Button)findViewById(R.id.time5);


        switch (view.getId()){
            case R.id.time1:
                duration = DURATION_1;
                time1.setTextColor(getResources().getColor(R.color.red));
                time2.setTextColor(getResources().getColor(R.color.black));
                time3.setTextColor(getResources().getColor(R.color.black));
                time4.setTextColor(getResources().getColor(R.color.black));
                time5.setTextColor(getResources().getColor(R.color.black));
                break;

            case R.id.time2:
                duration = DURATION_2;
                time2.setTextColor(getResources().getColor(R.color.red));
                time1.setTextColor(getResources().getColor(R.color.black));
                time3.setTextColor(getResources().getColor(R.color.black));
                time4.setTextColor(getResources().getColor(R.color.black));
                time5.setTextColor(getResources().getColor(R.color.black));
                break;

            case R.id.time3:
                duration = DURATION_3;
                time3.setTextColor(getResources().getColor(R.color.red));
                time2.setTextColor(getResources().getColor(R.color.black));
                time1.setTextColor(getResources().getColor(R.color.black));
                time4.setTextColor(getResources().getColor(R.color.black));
                time5.setTextColor(getResources().getColor(R.color.black));
                break;

            case R.id.time4:
                duration = DURATION_4;
                time4.setTextColor(getResources().getColor(R.color.red));
                time2.setTextColor(getResources().getColor(R.color.black));
                time3.setTextColor(getResources().getColor(R.color.black));
                time1.setTextColor(getResources().getColor(R.color.black));
                time5.setTextColor(getResources().getColor(R.color.black));
                break;

            case R.id.time5:
                duration = DURATION_5;
                time5.setTextColor(getResources().getColor(R.color.red));
                time2.setTextColor(getResources().getColor(R.color.black));
                time3.setTextColor(getResources().getColor(R.color.black));
                time4.setTextColor(getResources().getColor(R.color.black));
                time1.setTextColor(getResources().getColor(R.color.black));
                break;

            default:
                break;
        }
    }

    private boolean isValidTime(String dateString){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();

        try {
            cal.setTime(dateFormat.parse(dateString));
        } catch (ParseException e) {
            return false;
        }

        startTime = cal;
        return !cal.before(Calendar.getInstance());

    }

    private String getEndingTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm", Locale.ENGLISH);
        Calendar temp = this.startTime;
        switch (duration){

            case DURATION_1:
                temp.add(Calendar.MINUTE, 30);
                return dateFormat.format(temp.getTime());

            case DURATION_2:
                temp.add(Calendar.HOUR_OF_DAY, 1);
                return dateFormat.format(temp.getTime());

            case DURATION_3:
                temp.add(Calendar.HOUR_OF_DAY, 1);
                temp.add(Calendar.MINUTE, 30);
                return dateFormat.format(temp.getTime());

            case DURATION_4:
                temp.add(Calendar.HOUR_OF_DAY, 2);
                return dateFormat.format(temp.getTime());

            case DURATION_5:
                temp.add(Calendar.HOUR_OF_DAY, 2);
                return dateFormat.format(temp.getTime());

            default:
                return dateFormat.format(temp.getTime());
        }
    }

    private class addChallengeTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;
        String addStatus;

        @Override
        protected String doInBackground(String... params) {
            isOnline = true;
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            addStatus = connection.addChallenge(params[0]);

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
            disableDialog();
            if(result.matches("noConnection"))
                showNoConnection();
            else if(result.matches("ADD_FAILED"))
                showAddFail();
            else
                setReturn(addStatus);

        }
    }

    private void disableDialog(){
        mDialog.cancel();
    }

    public void showNoConnection(){
        Toast.makeText(CreateChallenge.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
        isOnline = false;
    }

    public void showAddFail(){
        Toast.makeText(CreateChallenge.this, "Unable to create challenge at this time", Toast.LENGTH_SHORT).show();
        isOnline = false;
    }

    public void setReturn( String challenge ){
        Intent returnIntent = new Intent();

        returnIntent.putExtra("challengeFields", challenge);
        setResult(Activity.RESULT_OK, returnIntent);
        isOnline = false;
        finish();
    }





}
