package com.example.chrissebesta.nyccyclemap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {
    public final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get unique ID from intent
        int uniqueId = getIntent().getIntExtra(getString(R.string.unique_id_extra_key), 0);
        Log.d(LOG_TAG, "The unique ID is: "+uniqueId);
        TextView textView = (TextView) findViewById(R.id.detail_activity_text_view);
        if (textView != null) {
            textView.setText("" + uniqueId);
        }
    }

}
