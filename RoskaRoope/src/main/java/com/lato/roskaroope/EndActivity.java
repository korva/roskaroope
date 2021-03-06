package com.lato.roskaroope;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;

/**
 * Created by jaakko on 10/14/13.
 */
public class EndActivity extends Activity {

    private ImageAdapter mAdapter = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "8dc145e2");
        setContentView(R.layout.activity_end);

        mAdapter = new ImageAdapter(this, CameraActivity.sharedThumbnails);
        GridView grid = (GridView) findViewById(R.id.gridview);
        grid.setAdapter(mAdapter);

        TextView score = (TextView) findViewById(R.id.scoreText);
        score.setText("Peli päättyi!\n" +
                "Sait yhteensä " + MainActivity.sharedTotalScore + " pistettä");
    }

    protected void onDestroy() {
        super.onDestroy();
        BugSenseHandler.closeSession(this);
    }

    public void onContinueButtonClicked(View v) {
        CameraActivity.sharedThumbnails.clear();

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}