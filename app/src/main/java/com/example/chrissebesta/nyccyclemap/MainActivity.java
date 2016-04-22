package com.example.chrissebesta.nyccyclemap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

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
                fetch.mContext = getBaseContext();
                fetch.execute();
//                CycleDbHelper helper = new CycleDbHelper(getBaseContext());
//                SQLiteDatabase db = helper.getReadableDatabase();
//                Log.d("BUILDTABLE", helper.getTableAsString(db, CycleContract.CycleEntry.TABLE_NAME));
            }
        });





    }
}
