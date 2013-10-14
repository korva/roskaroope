package com.lato.roskaroope;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaakko on 6/6/13.
 */
public class CameraActivity extends Activity implements Camera.AutoFocusCallback, Camera.PictureCallback {

    private static final String TAG = "CaptureActivity";

    CameraPreview mPreview;

    public static Bitmap sharedCameraImage = null;
    public static List<Bitmap> sharedThumbnails = new ArrayList<Bitmap>();

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        // Create new preview, add it to layout and start viewfinder
        FrameLayout layout = ((FrameLayout) findViewById(R.id.framelayout));
        mPreview = new CameraPreview(this);
        layout.addView(mPreview);

        ImageButton button = (ImageButton)findViewById(R.id.captureButton);
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                ImageButton but = (ImageButton)v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        but.setImageDrawable(getResources().getDrawable(R.drawable.button_capture_pressed));
                        but.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        but.setImageDrawable(getResources().getDrawable(R.drawable.button_capture));
                        but.invalidate();
                        break;
                    }
                }
                return false;
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mPreview.releaseCamera();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        mPreview.acquireCamera();
        mPreview.startPreview();
        ImageButton captureButton = (ImageButton)findViewById(R.id.captureButton);
        captureButton.setVisibility(View.VISIBLE);
        captureButton.setEnabled(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



    public void onCameraButtonClicked(View v) {
        Log.d(TAG, "onCameraButtonClicked");

        if(mPreview != null) {
            ImageButton button = (ImageButton) v;
            button.setEnabled(false);
            mPreview.mCamera.autoFocus(this);
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

        if(success) {
            Log.d(TAG, "Autofocus succeeded");
            mPreview.mCamera.takePicture(null, null, this);
        }
        else {
            Log.d(TAG, "Autofocus failed");
            ImageButton captureButton = (ImageButton)findViewById(R.id.captureButton);
            captureButton.setVisibility(View.VISIBLE);
            captureButton.setEnabled(true);
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(TAG, "onPictureTaken");

        new SaveImageTask(this).execute(data);

    }

    private class SaveImageTask extends AsyncTask<byte[], Void, Bitmap> {

        private ProgressDialog mProgressDialog;
        private Context mContext;

        SaveImageTask(Context context) {
            super();
            mContext = context;
        }
        @Override
        protected void onPreExecute() {
            // show progress dialog
            mProgressDialog = ProgressDialog.show(mContext, "Poimitaan roskaa...", "");
        }

        @Override
        protected void onPostExecute(Bitmap image) {


            if(image != null) {
                sharedCameraImage = image;



                Intent intent = new Intent(CameraActivity.this, TrashMapActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);

            } else {

            }

            mProgressDialog.dismiss();
        }


        @Override
        protected Bitmap doInBackground(byte[]... params) {

            // rotate and resize the picture
            byte[] data = params[0];
            Bitmap sourceBitmap;
            sourceBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
            Log.d(TAG, "Got bitmap from camera. w: " + rotatedBitmap.getWidth() + " h: " + rotatedBitmap.getHeight());

            // Create thumbnail
            Bitmap thumb = Bitmap.createScaledBitmap(rotatedBitmap, 250, 400, false);
            sharedThumbnails.add(thumb);

            return rotatedBitmap;

        }
    }

}