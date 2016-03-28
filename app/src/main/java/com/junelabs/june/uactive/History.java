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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class History extends AppCompatActivity {

    DataWriter writer;
    ActiveUser currentUser;

    boolean isOnline = false;
    ArrayList<String> history;
    LinearLayout layout;

    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        writer = new DataWriter(History.this);
        currentUser = writer.getCurrActiveUser();
        mDialog = new ProgressDialog(History.this);

        DBConnection.enableProgressDialog(mDialog);
        new getHistoryTask().execute();
    }

    public void showHistory(){
        layout = (LinearLayout)findViewById(R.id.showHistoryContainer);

        for(int i = 0; i < history.size(); i++){
            TextView view = new TextView(History.this);

            String[] temp = history.get(i).split(",");
            String text;

            if(Integer.valueOf(temp[1]) == 1)
                text = String.format("--%s by %s", temp[0],temp[3]);
            else
                text = String.format("--%s by %s", temp[0],temp[2]);

            view.setText(text);
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            layout.addView(view);
        }
    }

    private class getHistoryTask extends AsyncTask<String, Integer, String> {
        DBConnection connection;

        @Override
        protected String doInBackground(String... params) {
            isOnline = true;
            connection = new DBConnection();
            if(!connection.getConnectionStatus())
                return "noConnection";

            history = connection.getHistory(currentUser.getUserID());

            if(history.size() == 0)
                return "noHistory";

            return "Complete";
        }

        @Override
        protected void onPostExecute(String result) {
            disableDialog();
            isOnline = false;
            if(result.matches("noConnection"))
                showNoConnection();
            else if(result.matches("noHistory"))
                showNoHistory();
            else{
                showHistory();
            }
        }
    }

    private void disableDialog(){
        mDialog.cancel();
    }

    private void showNoConnection() {
        Toast.makeText(History.this, "Server Error, Try again later", Toast.LENGTH_SHORT).show();
    }

    private void showNoHistory() {
        layout = (LinearLayout)findViewById(R.id.showHistoryContainer);
        TextView view = new TextView(History.this);
        view.setText("No history, go do something!");
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        layout.addView(view);
    }
}
