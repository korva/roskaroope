package com.lato.roskaroope;

import android.app.Activity;
import android.content.res.AssetManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jaakko on 6/9/13.
 */
public class TrashMapFragment extends SupportMapFragment {

    private static final String TAG = "Leikkimaan";
    private final int SEARCH_LIMIT_DEFAULT = 40;
    private TrashCan mTarget = null;
    private GoogleMap mMap = null;
    private HashMap<Marker, TrashCan> mMarkerObjectMap = new HashMap<Marker, TrashCan>();
    private ArrayList<TrashCan> mSpotList = new ArrayList<TrashCan>();
    private LatLng mCurrentLocation = null;

    // Used to communicate spot selection events back to containing activity
    MapEventListener mListener;

    static TrashMapFragment newInstance(double latitude, double longitude) {
        TrashMapFragment f = new TrashMapFragment();

        Bundle args = new Bundle();
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate (map)");

        // Force bottom menu invalidation and add this fragment's menu items
        setHasOptionsMenu(true);

        fetchSpots(null, SEARCH_LIMIT_DEFAULT);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "mapFragment onCreateView");
        View root = super.onCreateView(inflater, container, savedInstanceState);
        mMap = getMap();
        mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // check if we are close enough
                TrashCan can = mMarkerObjectMap.get(marker);
                if(can != null & mCurrentLocation != null) {
                    double dist = GeoUtils.distanceKm(mCurrentLocation.latitude, mCurrentLocation.longitude, can.location.latitude, can.location.longitude);
                    if(dist*1000 < 30) {
                        marker.setSnippet("Klikkaa ja palauta roskat!");
                    } else {
                        marker.setSnippet("Tämä roskis on liian kaukana...");
                    }
                    mListener.onTargetUpdated(can, dist*1000);
                }
                return false;
            }
        });

        // When info windows is clicked, select spot
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                TrashCan can = mMarkerObjectMap.get(marker);
                if(can != null) {
                    mListener.onTargetCompleted(can);
                }

            }
        });


        if(mSpotList != null) populateMap(mSpotList);
        if(mTarget != null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mTarget.location, 15));
        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MapEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMapSpotSelectedListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Log.d(TAG, "Creating options menu for map...");


        return;
    }

    public void updateCurrentLocation(Location location) {

        Log.d(TAG, "TrashMapFragment updateCurrentLocation");

        mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        // check location to selected spot
        // if under treshold, fire event to trigger score activity
        if (mTarget == null) {
            // find the nearest trash can
            double nearest = Double.MAX_VALUE;
            for(TrashCan can : mSpotList) {
                double dist = GeoUtils.distanceKm(location.getLatitude(), location.getLongitude(), can.location.latitude, can.location.longitude);
                if(dist < nearest) {
                    mTarget = can;
                    nearest = dist;
                }
            }

            if(mTarget == null) return;

            Log.d(TAG, "Nearest trash can was " + mTarget.name + ", " + nearest*1000 + " m away.");

            if(mMap != null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mTarget.location, 15));

        }
        double distance = GeoUtils.distanceKm(location.getLatitude(), location.getLongitude(), mTarget.location.latitude, mTarget.location.longitude)*1000;
        Log.d(TAG, "Distance to target trash can is " + distance + " m");

        if(distance < 30) {
            mListener.onTargetReached(mTarget);
        } else {
            mListener.onTargetUpdated(mTarget, (int)distance);
        }

    }

    public void setSelectedSpot(TrashCan spot) {

        mTarget = spot;
    }

    private void fetchSpots(LatLng center, int limit) {
        //if(center == null || limit == 0) return;

        mSpotList.clear();

        AssetManager assetManager = this.getActivity().getAssets();
        InputStream stream = null;

        try {
            stream = assetManager.open("tampere.txt");
            initializeDatabase(stream);
        } catch (IOException e) {
             Log.d(TAG,"Opening file failed");
            return;
        }

    }


    private void populateMap(List<TrashCan> items) {

        if(mMap == null) return;

        Marker mark = null;

        for(int i = 0 ; i < items.size() ; i++) {

            mark = mMap.addMarker(new MarkerOptions()
                    .position(items.get(i).location)
                    .title(items.get(i).name));
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.playground_favorite)));

            mMarkerObjectMap.put(mark, items.get(i));

        }

    }

    private void clearMap() {
        if(mMap != null) mMap.clear();
        if(mMarkerObjectMap != null) mMarkerObjectMap.clear();
    }

    // Container Activity must implement this interface
    public interface MapEventListener {
        public void onTargetUpdated(TrashCan spot, double distance);
        public void onTargetReached(TrashCan spot);
        public void onTargetCompleted(TrashCan spot);
    }

    public boolean initializeDatabase(InputStream stream) {
         Log.d(TAG,"Starting db population.");

        if (stream == null) return false;

        long start = System.nanoTime();


        // use BufferedReader for reading lines
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = "";
        int count = 0;

        try {
            while ((line = reader.readLine()) != null)   {
                count++;
                boolean ok = processLine(line, "Roskis " + count);
                if (!ok) Log.d(TAG, "Populating line failed: " + line);

                //if(count > 300) break;

            }

        } catch (IOException e1) {
             Log.d(TAG,"Reading line failed");
        }

        long end = System.nanoTime() - start;
         Log.d(TAG, "DB init took " + end/1000000 + " ms");


        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {}
        }

        return true;
    }

    // split up line and bind values to ih
    private boolean processLine(String line, String title) {


        if (line.length() < 18) {
             Log.d(TAG, "Too short line: " + line);
            return false; // too little input info
        }

        String delimiter = ";";
        String[] temp = line.split(delimiter);

        Double latitude = new Double(temp[0]);
        Double longitude = new Double(temp[1]);

        if(latitude == 0 || longitude == 0) {
             Log.d(TAG, "Coordinates invalid: " + line);
            return false;
        }

        mSpotList.add(new TrashCan(latitude, longitude, title));

         //Log.d(TAG, "Adding spot: " + temp[0].trim());
        return true;

    }

    public class TrashCan {

        public TrashCan(double latitude, double longitude, String newName) {
            location = new LatLng(latitude, longitude);
            name = newName;
        }
        public LatLng location = null;
        public String name = null;
    }

}