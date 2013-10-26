package com.lato.roskaroope;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jaakko on 6/6/13.
 */
public class TrashMapActivity extends FragmentActivity implements TrashMapFragment.MapEventListener, SensorEventListener {

    private static final String TAG = "TrashMapActivity";

    private com.lato.roskaroope.LocationService mLocationService = null;
    private LocationServiceListener mLocationServiceListener = null;
    private static final int mLocationAccuracyTreshold = 100;
    private long mStartTime = 0;

    // User's last known location
    private Location mCurrentLocation = null;
    // Default location for map in case location not known
    private LatLng mDefaultLocation = new LatLng(61.497518,23.762419);
    private Location mStartingLocation = null;

    private TrashMapFragment.TrashCan mTarget = null;

    private TrashMapFragment mMapFragment = null;

    private SensorManager mSensorManager = null;
    private Sensor mCompass = null;
    // Azimuth between current location and target spot
    private double mAzimuth = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "8dc145e2");

        setContentView(R.layout.activity_map);

        mMapFragment = (TrashMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map));

        Button button = (Button)findViewById(R.id.returnButton);
        button.setVisibility(View.GONE);

        // Set up compass
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        if(mCompass != null) {
            // orientation sensor available
            mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_UI);
        }

        // Create a listener for LocationService events
        mLocationServiceListener = new LocationServiceListener() {
            public void onLocationAvailable(Location location) {
                Log.d(TAG, "Listener callback lat: " + location.getLatitude() + " lon: " + location.getLongitude() + " acc: " + location.getAccuracy());
                // only accept location if it is accurate enough
                if (location.getAccuracy() > mLocationAccuracyTreshold) return;

                mCurrentLocation = location;
                if(mStartingLocation == null) mStartingLocation = location;

                if(mMapFragment != null) mMapFragment.updateCurrentLocation(location);

            }
        };

        // Bind to location service
        bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);



        mStartTime = System.currentTimeMillis();
    }


    protected void onDestroy() {
        super.onDestroy();
        if (mConnection != null) unbindService(mConnection);
        BugSenseHandler.closeSession(this);

    }

    public void onCheatButtonClicked(View v) {
        onTargetReached(null);
    }

    public void onReturnButtonClicked(View v) {

        if(mTarget != null && mStartingLocation != null) {

            // Get time for completion
            long time = System.currentTimeMillis() - mStartTime;

            // Get distance from start to finish
            double distance = GeoUtils.distanceKm(mStartingLocation.getLatitude(), mStartingLocation.getLongitude(),
                    mTarget.location.latitude, mTarget.location.longitude);

            Log.d(TAG, "SCORE DISTANCE: " + distance);

            Intent i = new Intent(this, ScoreActivity.class);
            i.putExtra("distance", distance);
            i.putExtra("time", time);
            startActivity(i);
            finish();
        }

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            Log.d(TAG, "Location service connected");

            mLocationService = ((LocationService.LocalBinder)service).getService();

            mLocationService.setOnLocationServiceListener(mLocationServiceListener);

            if(!mLocationService.locationSettingsEnabled()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(TrashMapActivity.this);
                builder.setMessage("Aseta sijaintitiedot käyttöön asetuksista.")
                        .setCancelable(false)
                        .setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, 1);
                            }
                        })
                        .setNegativeButton("Ei", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();

            }


            // If location happens to be available immediately, update things accordingly
            if (mLocationService.locationAvailable()) {
                mCurrentLocation = mLocationService.getLocation();
                mStartingLocation = mCurrentLocation;
                if(mMapFragment != null) mMapFragment.updateCurrentLocation(mLocationService.getLocation());
            }


        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mLocationService = null;
            Log.d(TAG, "Location service disconnected");
        }
    };

    public void onTargetCompleted(TrashMapFragment.TrashCan spot) {
        mTarget = spot;
       onReturnButtonClicked(null);
    }

    public void onTargetReached(TrashMapFragment.TrashCan spot) {

        mTarget = spot;
        Button button = (Button)findViewById(R.id.returnButton);
        button.setVisibility(View.VISIBLE);
    }

    public void onTargetLost(TrashMapFragment.TrashCan spot) {

        mTarget = spot;
        Button button = (Button)findViewById(R.id.returnButton);
        button.setVisibility(View.GONE);
    }

    public void onTargetUpdated(TrashMapFragment.TrashCan spot, double distance) {
        // update game UI
        TextView text = (TextView)findViewById(R.id.nearestText);

        int dist = (int)distance;
        if(dist < 1000) {
            text.setText("Roskis " + dist + " m päässä");
        }
        else {
            text.setText("Roskis " + dist/1000 + " km " + dist%1000 + " m päässä");
        }

        if(spot == null) return;

        mTarget = spot;

        calculateAzimuth(mTarget.location.latitude, mTarget.location.longitude);

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {	}

    public void onSensorChanged(SensorEvent event) {

        // only accept magnetometer readings
        if (event.sensor.getType() != Sensor.TYPE_ORIENTATION) return;

        // no point updating compass if location is not known
        if (mCurrentLocation == null) return;

        float northAzimuth = event.values[0];
        double direction = -northAzimuth + mAzimuth;

        CompassView view = (CompassView)findViewById(R.id.compass);
        view.setEnabled(true);
        view.setAngle(direction);

        //Log.d(TAG, "from north: " + northAzimuth + " spotA: " + mAzimuth + " dir: " + direction + "acc: " + event.accuracy);



    }

    void calculateAzimuth(double targetLatitude, double targetLongitude) {

        if (mCurrentLocation == null) return;

        mAzimuth = GeoUtils.bearing(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                targetLatitude, targetLongitude);

    }

}