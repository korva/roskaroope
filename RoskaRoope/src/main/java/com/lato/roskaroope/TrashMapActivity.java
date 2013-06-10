package com.lato.roskaroope;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import com.google.android.gms.maps.model.LatLng;
import com.lato.roskaroope.TrashMapFragment.OnTargetReachedListener;

/**
 * Created by jaakko on 6/6/13.
 */
public class TrashMapActivity extends Activity implements OnTargetReachedListener {

    com.lato.roskaroope.LocationService mLocationService = null;
    LocationServiceListener mLocationServiceListener = null;
    private static final int mLocationAccuracyTreshold = 2000;

    // User's last known location
    private Location mCurrentLocation = null;
    // Default location for map in case location not known
    private LatLng mDefaultLocation = new LatLng(61.497518,23.762419);

    private TrashMapFragment mMapFragment = null;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a listener for LocationService events
        mLocationServiceListener = new LocationServiceListener() {
            public void onLocationAvailable(Location location) {
                //Log.d(TAG, "Listener callback lat: " + location.getLatitude() + " lon: " + location.getLongitude() + " acc: " + location.getAccuracy());
                // only accept location if it is accurate enough
                if (location.getAccuracy() > mLocationAccuracyTreshold) return;

                mCurrentLocation = location;

                if(mMapFragment != null) mMapFragment.updateCurrentLocation(location);

            }
        };

        // Bind to location service
        bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);


    }

    private void showMapFragment() {
        FragmentManager fragMgr = getFragmentManager();
        FragmentTransaction ft = fragMgr.beginTransaction();

        // Check if the fragment is already initialized
        if (mMapFragment == null) {
            // If not, instantiate and add it to the activity
            if(mCurrentLocation == null) {
                mMapFragment = TrashMapFragment.newInstance(mDefaultLocation.latitude, mDefaultLocation.longitude);
            } else {
                mMapFragment = TrashMapFragment.newInstance(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            }
            ft.add(android.R.id.content, mMapFragment, "map");
        } else {
            // If it exists, simply attach it in order to show it
            ft.show(mMapFragment);
        }

        ft.commit();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

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
                if(mMapFragment != null) mMapFragment.updateCurrentLocation(mLocationService.getLocation());
            }


        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mLocationService = null;

        }
    };

    public interface LocationServiceListener
    {
        public void onLocationAvailable(Location location);
    }

    public void onTargetReached(TrashMapFragment.TrashCan spot) {
        // A spot has been selected from search fragment.
        // Switch to main view and show it
        Intent i = new Intent(this, ScoreActivity.class);
        startActivity(i);
        finish();

    }

}