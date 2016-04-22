package com.example.chrissebesta.nyccyclemap;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.chrissebesta.nyccyclemap.data.CycleContract;
import com.example.chrissebesta.nyccyclemap.data.CycleDbHelper;

public class MainActivity extends AppCompatActivity {
    public final String LOG_TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button refreshButton = (Button) findViewById(R.id.refreshbutton);
        //final FetchCycleDataTask fetch = new FetchCycleDataTask();

        assert refreshButton != null;
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fetch Json data from NYC Data
                final FetchCycleDataTask fetch = new FetchCycleDataTask();
                fetch.execute();

            }
        });

        CycleDbHelper helper = new CycleDbHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Log.d("BUILDTABLE", helper.getTableAsString(db, CycleContract.CycleEntry.TABLE_NAME));



    }
}
