package com.junelabs.june.uactive;

import android.app.ProgressDialog;
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
import android.widget.Toast;

public class AddOrg extends AppCompatActivity {

    String orgName;
    DataWriter writer;
    ActiveUser currentUser;

    boolean isOnline = false;
    boolean isAddExisting = true;

    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_org);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        writer = new DataWriter(AddOrg.this);
        currentUser = writer.getCurrActiveUser();
        mDialog = new ProgressDialog(AddOrg.this);
    }

    public void onSubmit(View view) {
        if(isOnline){
            return;
        }

        EditText text;

        if(!isAddExisting){
            text = (EditText)findViewById(R.id.orgNameField);
            orgName = "";
            orgName = text.getText().toString().trim();

            if(orgName.isEmpty()){
                Toast.makeText(AddOrg.this, "Enter a name", Toast.LENGTH_SHORT).show();
                return;
            }

            DBConnection.enableProgressDialog(mDialog);
            new addOrgTask().execute();
        }
        else{
            text = (EditText)findViewById(R.id.orgCodeField);
            orgName = "";
            orgName = text.getText().toString().trim();

            if(orgName.isEmpty()){
                Toast.makeText(AddOrg.this, "Enter a code", Toast.LENGTH_SHORT).show();
                return;
            }

            DBConnection.enableProgressDialog(mDialog);
            new addToExistingOrgTask().execute();
        }


    }

    public void changeType(View view) {
        Button button;
        if(view.getId() == R.id.addToggle){
            if(!isAddExisting){
                isAddExisting = true;
                button = (Button)view;
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                button.setTextColor(getResources().getColor(R.color.red));

                button = (Button)findViewById(R.id.createToggle);
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                button.setTextColor(getResources().getColor(R.color.black));

                findViewById(R.id.addExistingOrgContainer).setVisibility(View.VISIBLE);
                findViewById(R.id.addNewOrgContainer).setVisibility(View.GONE);
            }
        }
        else
            if(isAddExisting){
                isAddExisting = false;
                button = (Button)view;
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                button.setTextColor(getResources().getColor(R.color.red));

                button = (Button)findViewById(R.id.addToggle);
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                button.setTextColor(getResources().getColor(R.color.black));

                findViewById(R.id.addExistingOrgContainer).setVisibility(View.GONE);
                findViewById(R.id.addNewOrgContainer).setVisibility(View.VISIBLE);
            }
    }

    private class addOrgTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;
        int status;

        @Override
        protected String doInBackground(String... params) {

            isOnline = true;
            connection = new DBConnection();

            if(!connection.getConnectionStatus())
                return "noConnection";

            status = connection.addNewOrg(orgName, currentUser.getUserID());

            if(status == -1)
                return "FAILED";
            if(status == 0)
                return "AE";


            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            isOnline = false;
            if(result.matches("noConnection"))
                showNoConnection();
            else if(result.matches("FAILED"))
                showFailed();
            else if (result.matches("AE"))
                showAlreadyExists(0);
            else{
                showSuccess(0);
            }

            disableProgressDialog();
        }
    }

    private class addToExistingOrgTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;
        int status;

        @Override
        protected String doInBackground(String... params) {

            isOnline = true;
            connection = new DBConnection();

            if(!connection.getConnectionStatus())
                return "noConnection";

            status = connection.addUserToOrg(orgName,currentUser.getUserID());

            if(status == -1)
                return "FAILED";
            if(status == 0)
                return "AE";
            if(status == 1)
                return "DNE";


            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            isOnline = false;
            if(result.matches("noConnection"))
                showNoConnection();
            else if (result.matches("FAILED"))
                showFailed();
            else if (result.matches("AE"))
                showAlreadyExists(1);
            else if (result.matches("DNE"))
                showDoesNotExist();
            else{
                showSuccess(1);
            }

            disableProgressDialog();
        }
    }

    private void disableProgressDialog(){
        DBConnection.disableProgressDialog(mDialog);
    }

    private void showDoesNotExist() {
        Toast.makeText(AddOrg.this, "Incorrect Code", Toast.LENGTH_SHORT).show();
    }

    private void showAlreadyExists(int type) {
        if(type == 0)
            Toast.makeText(AddOrg.this, "Name already exists", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(AddOrg.this, "User already added", Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(int type) {
        if(type == 0)
            Toast.makeText(AddOrg.this, "Request submitted successfully", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(AddOrg.this, "User added successfully", Toast.LENGTH_SHORT).show();
    }

    private void showFailed() {
        Toast.makeText(AddOrg.this, "Server Error. Try again later", Toast.LENGTH_SHORT).show();
    }

    private void showNoConnection() {
        Toast.makeText(AddOrg.this, "Server Error. Try again later", Toast.LENGTH_SHORT).show();
    }

}
