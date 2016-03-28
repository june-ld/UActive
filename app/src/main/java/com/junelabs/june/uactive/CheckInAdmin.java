package com.junelabs.june.uactive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class CheckInAdmin extends AppCompatActivity {

    ActiveChallenge challenge;
    boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_admin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        challenge = new ActiveChallenge(getIntent().getStringExtra("challengeFields"));

        TextView view = (TextView)findViewById(R.id.checkInTitleAdmin);
        view.setText(challenge.getChName());

    }

    public void getCodeButton(View view) {
        if(clicked)
            return;

        TextView codeHolder = (TextView)findViewById(R.id.codeHolder);
        codeHolder.setText(String.valueOf(challenge.getCheckinCode()));
        findViewById(R.id.codeVisbilityContainer).setVisibility(View.VISIBLE);
    }

    public void onGoBack(View view) {
        Intent returnIntent = getIntent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

}
