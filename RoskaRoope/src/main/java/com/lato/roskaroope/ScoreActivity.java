package com.lato.roskaroope;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by jaakko on 6/6/13.
 */
public class ScoreActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        ImageView bg = (ImageView) findViewById(R.id.backgroundImage);
        bg.setImageBitmap(CameraActivity.sharedCameraImage);
    }

    public void onContinueButtonClicked(View v) {
        finish();
    }

    public void onQuitButtonClicked(View v) {
        finish();
    }
}