package com.junelabs.june.uactive;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    EditText userNameField;
    EditText passwordField;
    boolean isOnline = false;
    DataWriter writer;

    boolean netMode = true;

    static final int NEW_USER_CREATE = 1;

    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        writer = new DataWriter(Login.this);
        ActiveUser lastUser = writer.getCurrActiveUser();

        userNameField = (EditText)findViewById(R.id.usernameField);
        passwordField = (EditText)findViewById(R.id.passwordField);

        if(lastUser != null && !lastUser.getName().matches("guest")){
            userNameField.setText(lastUser.getName());
            passwordField.setText(lastUser.getPassword());
        }

        progress = new ProgressDialog(Login.this);
        progress.setMessage("Loading...");
        progress.setCancelable(false);

    }

    public void onSubmit(View view) {
        // if there is an attempt to connect do nothing
        if(isOnline)
            return;

        // get username and password
        String userName = userNameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // check if they are empty
        if(userName.isEmpty() || password.isEmpty()){
            Toast.makeText(Login.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!netMode) {
            ActiveUser lastUser = writer.getCurrActiveUser();
            if(lastUser == null){
                Toast.makeText(Login.this, "Must be online", Toast.LENGTH_SHORT).show();
                return;
            }
            if(userName.matches(lastUser.getName()) && password.matches(lastUser.getPassword()))
                userFound(lastUser);
            return;
        }

        // set connecting status and execute async task
        isOnline = true;
        progress.show();
        new loginCheckTask().execute(userName, password);
    }

    public void onNewUser(View view) {
        if(!netMode) {
            Toast.makeText(Login.this, "Must be online", Toast.LENGTH_SHORT).show();
            return;
        }

        // start activity to add new user to server
        Intent intent = new Intent(Login.this, CreateUser.class);
        startActivityForResult(intent, NEW_USER_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == NEW_USER_CREATE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // construct string from fields
                String userData = String.format("%d,%s,%s,%s,%s,%d", data.getIntExtra("userID", 0),
                        data.getStringExtra("username"), data.getStringExtra("pass"), data.getStringExtra("email"),
                        data.getStringExtra("secAns"), data.getIntExtra("exp", 0));

                // save data to shared preferences
                ActiveUser user = ActiveUser.createUserFromString(userData);
                writer.saveActiveUser(user);

                // save online/offline status
                if(writer.getNetMode() != netMode)
                    writer.saveNetMode(netMode);


                Intent intent = new Intent(Login.this, MainScreen.class);
                startActivity(intent);
                finish();
            }
        }
    }

    public void enterAsGuest(View view) {
        ActiveUser guest = new ActiveUser(-1, "guest", "pass", "email@fake.com", "none", 0);

        if(!writer.getCurrActiveUser().getName().matches(guest.getName()))
            writer.saveActiveUser(guest);

        // save online/offline status
        if(writer.getNetMode() != netMode)
            writer.saveNetMode(netMode);
        Intent intent = new Intent(Login.this, MainScreen.class);
        startActivity(intent);
        finish();
    }

    public void toggleOnline(View view) {
        Button button = (Button)view;
        if(netMode){
            button.setText("Go Online");
            netMode = false;
        }
        else {
            button.setText("Go Offline");
            netMode = true;
        }
    }

    private class loginCheckTask extends AsyncTask<String, Integer, String> {
        ActiveUser aUser;
        DBConnection connection;

        @Override
        protected String doInBackground(String... params) {
            progress.show();
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            aUser = connection.getActiveUserServ(params[0], params[1]);

            if(aUser == null)
                return "notFound";

            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.matches("noConnection"))
                showNoConnection();
            else if(result.matches("notFound"))
                showNotFound();
            else
                userFound(aUser);

        }
    }

    public void showNoConnection(){
        progress.cancel();
        Toast.makeText(Login.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
        isOnline = false;
    }

    private void showNotFound() {
        progress.cancel();
        Toast.makeText(Login.this, "User not Found", Toast.LENGTH_SHORT).show();
        isOnline = false;
    }

    private void userFound(ActiveUser user){
        progress.cancel();
        // save to memory
        if(!writer.getCurrActiveUser().getName().matches(user.getName()))
            writer.saveActiveUser(user);

        // save online/offline status
        if(writer.getNetMode() != netMode)
            writer.saveNetMode(netMode);

        // start homescreen activity, end this
        Intent intent = new Intent(Login.this, MainScreen.class);
        isOnline = false;
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
}
