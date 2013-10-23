package com.lato.roskaroope;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {

	private static final String TAG = "roskaroope";
	private LocationManager mLocationManager = null;
	private static final int LOCATION_INTERVAL = 1000;
	private static final float LOCATION_DISTANCE = 10f;
	
	Location mLastLocation = null;
	boolean mLocationAvailable = false;
	
	private final IBinder mBinder = new LocalBinder();
	
	ArrayList<LocationServiceListener> mListeners = new ArrayList<LocationServiceListener> ();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }
	
	@Override
    public void onCreate() {
          super.onCreate();
          Log.e(TAG, "Location service created");
          initializeLocationManager();
          try {
              mLocationManager.requestLocationUpdates(
                      LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                      mLocationListeners[1]);
          } catch (java.lang.SecurityException ex) {
              Log.i(TAG, "fail to request location update, ignore", ex);
          } catch (IllegalArgumentException ex) {
              Log.d(TAG, "network provider does not exist, " + ex.getMessage());
          }
          try {
              mLocationManager.requestLocationUpdates(
                      LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                      mLocationListeners[0]);
          } catch (java.lang.SecurityException ex) {
              Log.i(TAG, "fail to request location update, ignore", ex);
          } catch (IllegalArgumentException ex) {
              Log.d(TAG, "gps provider does not exist " + ex.getMessage());
          }
          
         
          
          

    }
   
    @Override
    public void onDestroy() {
          super.onDestroy();
          //Toast.makeText(this, "Service destroyed ...", Toast.LENGTH_LONG).show();
          Log.e(TAG, "Location service destroiyed");
          if (mLocationManager != null) {
              for (int i = 0; i < mLocationListeners.length; i++) {
                  try {
                      mLocationManager.removeUpdates(mLocationListeners[i]);
                  } catch (Exception ex) {
                      Log.i(TAG, "fail to remove location listners, ignore", ex);
                  }
              }
          }

    }
    
    public void setOnLocationServiceListener (LocationServiceListener listener)
    {
        //Log.d(TAG, "Adding listener");
    	// Store the listener object
        this.mListeners.add(listener);
    }
    
    public Location getLocation() {
    	return mLastLocation;
    }
    
    public boolean locationAvailable() {
    	return mLocationAvailable;
    }
    
    // returns false if GPS and Network providers are not enabled
    public boolean locationSettingsEnabled() {
    	if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    			&& mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
        
    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    
    
    
    private class LocationListener implements android.location.LocationListener{
        
        
        public LocationListener(String provider)
        {
            //Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }
        public void onLocationChanged(Location location)
        {
            //Log.e(TAG, "onLocationChanged: " + location);
        	mLastLocation.set(location);
        	mLocationAvailable = true;
            
            for (LocationServiceListener listener : mListeners)
            {
                listener.onLocationAvailable(location);
            }
            
        }
        
        public void onProviderDisabled(String provider)
        {
            //Log.e(TAG, "onProviderDisabled: " + provider);            
        }
        
        public void onProviderEnabled(String provider)
        {
            //Log.e(TAG, "onProviderEnabled: " + provider);
        }
        
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            //Log.e(TAG, "onStatusChanged: " + provider);
        }
    } 
    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
  

}
