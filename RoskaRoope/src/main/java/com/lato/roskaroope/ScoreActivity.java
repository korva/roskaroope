package com.lato.roskaroope;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;

/**
 * Created by jaakko on 6/6/13.
 */
public class ScoreActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "8dc145e2");
        setContentView(R.layout.activity_score);

        ImageView bg = (ImageView) findViewById(R.id.backgroundImage);
        bg.setImageBitmap(CameraActivity.sharedCameraImage);

        Intent i = getIntent();
        long time = i.getLongExtra("time", 0);
        long timeScore = 0;
        double distanceDouble = i.getDoubleExtra("distance", 0)*1000;
        int distance = (int) distanceDouble;
        int distanceScore = 0;

        if(time > 0) {
            timeScore = 5000 - ((time/1000)*(5000/300));
            if(timeScore < 0) timeScore = 0;
        }

        if(distance > 0) {
            if(distance > 1000) distanceScore = 5000;
            else distanceScore = 5000 - (5000 - 5*distance);

        }

        long totalScore = timeScore + distanceScore;
        MainActivity.sharedTotalScore += totalScore;

        TextView score = (TextView) findViewById(R.id.scoreText);
        score.setText("Aika: " + time/1000 + " s\n" +
                "   -> " + timeScore + " p\n" +
                "Matka: " + distance + " m\n" +
                "   -> " + distanceScore + " p\n" +
                "Yhteensä " + totalScore + " pistettä");
    }

    public void onContinueButtonClicked(View v) {
        finish();
    }

    public void onQuitButtonClicked(View v) {
        Intent i = new Intent(this, EndActivity.class);
        startActivity(i);
    }
}