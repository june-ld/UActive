package com.junelabs.june.uactive;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MyOrgs extends AppCompatActivity {

    private ArrayList<Organization> orgs;
    DataWriter writer;
    static int ORG_ID = 500;
    static int ORG_CHILD = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orgs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        writer = new DataWriter(MyOrgs.this);
        orgs = writer.getOrganizations();

        showOrgs();
    }

    private void showOrgs() {
        LinearLayout container = (LinearLayout)findViewById(R.id.showOrgsContainer);

        for(int i = 0; i < orgs.size(); i++){

            LinearLayout layout = new LinearLayout(MyOrgs.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout temp = (LinearLayout) v;

                    if (temp.getChildAt(1).getVisibility() == View.GONE)
                        temp.getChildAt(1).setVisibility(View.VISIBLE);
                    else
                        temp.getChildAt(1).setVisibility(View.GONE);
                }
            });

            Organization temp = orgs.get(i);

            TextView view = new TextView(MyOrgs.this);
            view.setText(temp.getOrgName());
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            view.setTextColor(getResources().getColor(R.color.red));
            layout.addView(view);

            TextView child = new TextView(MyOrgs.this);
            child.setText(String.valueOf(temp.getPassCode()));
            child.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            child.setGravity(Gravity.CENTER_HORIZONTAL);
            child.setVisibility(View.GONE);
            layout.addView(child);

            container.addView(layout);
        }
    }

}
