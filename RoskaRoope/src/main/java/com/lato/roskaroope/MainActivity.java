package com.lato.roskaroope;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.opengl.Visibility;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class MainActivity extends Activity {

    public static long sharedTotalScore = 0;
    private com.lato.roskaroope.LocationService mLocationService = null;
    private LocationServiceListener mLocationServiceListener = null;
    private static final int mLocationAccuracyTreshold = 100;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "8dc145e2");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        this.startService(new Intent(this, LocationService.class));

        // Create a listener for LocationService events
        mLocationServiceListener = new LocationServiceListener() {
            public void onLocationAvailable(Location location) {
                Log.d(TAG, "Listener callback lat: " + location.getLatitude() + " lon: " + location.getLongitude() + " acc: " + location.getAccuracy());
                // only accept location if it is accurate enough
                if (location.getAccuracy() > mLocationAccuracyTreshold) return;

                Button button = (Button)findViewById(R.id.button);
                button.setText("Pelaa!");
                button.setEnabled(true);

                TextView text = (TextView)findViewById(R.id.locationText);
                text.setVisibility(View.GONE);

            }
        };

        // Bind to location service
        bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);


    }

    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, LocationService.class));
        BugSenseHandler.closeSession(this);
        if (mConnection != null) unbindService(mConnection);
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

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            Log.d(TAG, "Location service connected");

            mLocationService = ((LocationService.LocalBinder)service).getService();

            mLocationService.setOnLocationServiceListener(mLocationServiceListener);

            if(!mLocationService.locationSettingsEnabled()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                Button button = (Button)findViewById(R.id.button);
                button.setText("Pelaa!");
                button.setEnabled(true);

                TextView text = (TextView)findViewById(R.id.locationText);
                text.setVisibility(View.GONE);
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
    
}
