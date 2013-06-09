package com.lato.roskaroope;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jaakko on 6/9/13.
 */
public class TrashMapFragment extends MapFragment {

    private static final String TAG = "Leikkimaan";
    private final int SEARCH_LIMIT_DEFAULT = 40;
    private LatLng mSelectedPoint = null;
    private GoogleMap mMap = null;
    private HashMap<Marker, TrashCan> mMarkerObjectMap = new HashMap<Marker, TrashCan>();

    // Used to communicate spot selection events back to containing activity
    OnMapSpotSelectedListener mListener;

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

        double latitude = 0;
        double longitude = 0;

        Bundle args = getArguments();

        if(args != null) {
            latitude = args.getDouble("latitude");
            longitude = args.getDouble("longitude");
            if(latitude == 0 || longitude == 0) return;
            mSelectedPoint = new LatLng(latitude, longitude);
        } else {
            return;
        }

        // Force bottom menu invalidation and add this fragment's menu items
        setHasOptionsMenu(true);

        if(mSelectedPoint != null) {
            fetchSpots(mSelectedPoint, SEARCH_LIMIT_DEFAULT);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        mMap = getMap();
        mMap.setMyLocationEnabled(true);

        // When info windows is clicked, select spot
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {


            }
        });

        if(mSelectedPoint != null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mSelectedPoint, 15));
        if(mSpotList != null) populateMap(mSpotList);
        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMapSpotSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMapSpotSelectedListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Log.d(TAG, "Creating options menu for map...");


        return;
    }

    public void setSelectedSpot(ParseObject spot) {

        mSelectedPoint = new LatLng(ppoint.getLatitude(), ppoint.getLongitude());
    }

    private void fetchSpots(LatLng center, int limit) {
        if(center == null || limit == 0) return;

        ParseGeoPoint userLocation = new ParseGeoPoint(center.latitude, center.longitude);
        ParseQuery query = new ParseQuery("Playground");
        query.whereNear("location", userLocation);
        query.setLimit(limit);

        query.findInBackground(new FindCallback() {
            public void done(List<ParseObject> spotList, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + spotList.size() + " spots");
                    mSpotList = spotList;
                    if(mMap != null) populateMap(mSpotList);

                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
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
    public interface OnMapSpotSelectedListener {
        public void onMapSpotSelected(TrashCan spot);
    }

    public class TrashCan {
        public LatLng location = null;
        public String name = null;
    }

}