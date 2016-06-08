package com.wordpress.chrissebesta.nyccyclemap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by Chris Sebesta
 * A simple text view activity to show the string of text with HTML links for the relevant documents
 * Shows a link to the source code, a link to the NYC Open Data,
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
