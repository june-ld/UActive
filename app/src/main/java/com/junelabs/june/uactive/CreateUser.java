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
import android.widget.Toast;

public class CreateUser extends AppCompatActivity {

    boolean isOnline = false;
    ActiveUser tempUser;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDialog = new ProgressDialog(CreateUser.this);
    }

    public void onSubmit(View view) {
        if(isOnline)
            return;

        EditText usernameCont = (EditText)findViewById(R.id.enterUsername);
        EditText emailCont = (EditText)findViewById(R.id.enterEmail);
        EditText passwordCont = (EditText)findViewById(R.id.enterPassword);
        EditText confirmCont = (EditText)findViewById(R.id.confirmPassword);
        EditText secAnsCont = (EditText)findViewById(R.id.secQuestionAns);
        String username, email, password, confirm, response;

        username = usernameCont.getText().toString().trim();
        email = emailCont.getText().toString().trim();
        password = passwordCont.getText().toString().trim();
        confirm = confirmCont.getText().toString().trim();
        response = secAnsCont.getText().toString().trim();

        if(username.isEmpty()) {
            Toast.makeText(CreateUser.this, "Enter a username", Toast.LENGTH_SHORT).show();
            return;
        } else if (email.isEmpty()){
            Toast.makeText(CreateUser.this, "Enter an email", Toast.LENGTH_SHORT).show();
            return;
        } else if (password.isEmpty()){
            Toast.makeText(CreateUser.this, "Enter a password", Toast.LENGTH_SHORT).show();
            return;
        } else if (!confirm.matches(password)){
            Toast.makeText(CreateUser.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        } else if (response.isEmpty()){
            Toast.makeText(CreateUser.this, "Enter response to security question", Toast.LENGTH_SHORT).show();
            return;
        }


        isOnline = true;
        DBConnection.enableProgressDialog(mDialog);
        loginCheckTask task = new loginCheckTask();
        task.execute(username, email, password, response);

    }

    private class loginCheckTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;
        ActiveUser aUser;

        @Override
        protected String doInBackground(String... params) {
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            String taskStatus = connection.addUser(params[0],params[1],params[2],params[3]);

            if (taskStatus.matches("ADD_EXISTS"))
                return "EXISTS";
            if (taskStatus.matches("ADD_FAILED"))
                return "FAILED";

            aUser = ActiveUser.createUserFromString(taskStatus);
            return "SUCCESS";

        }

        @Override
        protected void onPostExecute(String result) {
            disableDialog();
            if(result.matches("noConnection"))
                showNoConnection();
            else if (result.matches("SUCCESS"))
                addSuccessful(aUser);
            else if(result.matches("EXISTS"))
                showExists();
            else
                showFailed();
        }
    }

    private void disableDialog(){
        mDialog.cancel();
    }

    public void showNoConnection(){
        Toast.makeText(CreateUser.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
        isOnline = false;
    }

    private void showExists() {
        Toast.makeText(CreateUser.this, "Username already Exists", Toast.LENGTH_SHORT).show();
        isOnline = false;
    }

    private void showFailed() {
        Toast.makeText(CreateUser.this, "Unable to add at this time", Toast.LENGTH_SHORT).show();
        isOnline = false;
    }

    private void addSuccessful(ActiveUser user) {
        Intent returnIntent = new Intent();

        returnIntent.putExtra("userID", user.getUserID());
        returnIntent.putExtra("username", user.getName());
        returnIntent.putExtra("email", user.getEmailAddr());
        returnIntent.putExtra("pass", user.getPassword());
        returnIntent.putExtra("secAns", user.getSecurityQuestionAns());
        returnIntent.putExtra("exp", user.getCurrExp());

        setResult(Activity.RESULT_OK, returnIntent);
        isOnline = false;
        finish();
    }
}
