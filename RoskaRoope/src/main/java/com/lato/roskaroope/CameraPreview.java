package com.lato.roskaroope;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by jaakko on 6/9/13.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";

    private SurfaceHolder mHolder = null;
    public Camera mCamera = null;
    // set to true once configuration is done
    private boolean mCameraConfigured = false;

    public CameraPreview(Context context) {
        super(context);

        acquireCamera();

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged, width: " + width + ", height: " + height);

        initPreview();
        if(mCamera == null) acquireCamera();
        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Log.d(TAG, "surfaceCreated");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Log.d(TAG, "surfaceDestroyed");
        releaseCamera();
    }

    private void initPreview() {
        if (mCamera == null || mHolder == null) return;

        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            Log.d(TAG, "Could not set preview display: " + e.getMessage());
        }


        configureCamera();

    }

    private void configureCamera() {
        Camera.Parameters parameters = mCamera.getParameters();

        // query preview sizes
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        for(Camera.Size size : previewSizes) {
            //Log.d(TAG, "Size: " + size.height + " x " + size.width);
            if(size.height == 1200 && size.width == 1600) {
                parameters.setPreviewSize(size.width, size.height);
                //Log.d(TAG, "Selected this ^^");
            }
        }

        parameters.setJpegQuality(80);
        parameters.setJpegThumbnailSize(0, 0);

        // set picture size
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        for(Camera.Size size : pictureSizes) {
            Log.d(TAG, "Size: " + size.height + " x " + size.width);
            if(size.height == 1200 && size.width == 1600) {
                parameters.setPictureSize(size.width, size.height);
                Log.d(TAG, "Selected this ^^");
            }
        }

        mCamera.setParameters(parameters);
        mCameraConfigured = true;
    }

    public void acquireCamera() {
        //Log.d(TAG, "acquireCamera()");
        if(mCamera == null) {
            Log.d(TAG, "Calling Camera.open()");
            try {
                mCamera = Camera.open();
                // We support portrait mode only, rotate accordingly
                mCamera.setDisplayOrientation(90);
            }
            catch (Exception e) {
                Log.d(TAG, "Could not open camera!");
            }
        }
    }

    public void releaseCamera() {
        //Log.d(TAG, "releaseCamera()");
        if(mCamera != null) {
            //Log.d(TAG, "Calling mCamera.release()");
            mCamera.release();
            mCamera = null;
        }
    }

    public void startPreview() {
        if(mCamera != null) {
            //Log.d(TAG, "starting preview");
            if(mHolder == null) mHolder = getHolder();
            mCamera.startPreview();
        }
    }
}