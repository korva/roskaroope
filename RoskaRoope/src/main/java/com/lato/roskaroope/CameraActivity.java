package com.lato.roskaroope;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import com.bugsense.trace.BugSenseHandler;

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
        BugSenseHandler.initAndStartSession(this, "8dc145e2");
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
        BugSenseHandler.closeSession(this);
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

    private class SaveImageTask extends AsyncTask<byte[], Void, Boolean> {

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
        protected void onPostExecute(Boolean success) {


            if(success) {

                Intent intent = new Intent(CameraActivity.this, TrashMapActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
                builder.setMessage("Kuvan ottamisessa tapahtui virhe.")
                        .setCancelable(true)
                        .setPositiveButton("Perhana", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
            }

            mProgressDialog.dismiss();
        }


        @Override
        protected Boolean doInBackground(byte[]... params) {

            // rotate and resize the picture
            byte[] data = params[0];
            Log.d(TAG, "Image bytes length: " + data.length);
            Bitmap sourceBitmap;
            try {
              sourceBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            } catch (OutOfMemoryError oom) {
                return false;
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = null;
            try {
                rotatedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
            } catch (OutOfMemoryError oom) {
                return false;
            }
            sourceBitmap.recycle();
            sharedCameraImage = rotatedBitmap;
            Log.d(TAG, "Got bitmap from camera. w: " + rotatedBitmap.getWidth() + " h: " + rotatedBitmap.getHeight());

            // Create thumbnail
            Bitmap thumb = Bitmap.createScaledBitmap(rotatedBitmap, 250, 400, false);
            sharedThumbnails.add(thumb);

            return true;

        }
    }

}