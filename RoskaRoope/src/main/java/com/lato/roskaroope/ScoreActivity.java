package com.lato.roskaroope;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by jaakko on 6/6/13.
 */
public class ScoreActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
    }

    public void onContinueButtonClicked(View v) {
        finish();
    }

    public void onQuitButtonClicked(View v) {
        finish();
    }
}