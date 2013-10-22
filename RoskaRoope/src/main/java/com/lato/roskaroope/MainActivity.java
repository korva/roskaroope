package com.lato.roskaroope;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import com.bugsense.trace.BugSenseHandler;

public class MainActivity extends Activity {

    public static long sharedTotalScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "8dc145e2");
        setContentView(R.layout.activity_main);

        // start location service
        this.startService(new Intent(this, LocationService.class));


    }

    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, LocationService.class));
        BugSenseHandler.closeSession(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onPlayButtonClicked(View v) {
       Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);
    }
    
}
