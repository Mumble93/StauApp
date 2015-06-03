package com.sta.dhbw.stauapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sta.dhbw.jambeaconrestclient.TrafficJam;

import java.util.List;

public class JamMapActivity extends Activity
{
    private static final String TAG = JamMapActivity.class.getSimpleName();
    private GoogleMap googleMap;
    private LatLng currentZoomedLocation;
    private List<TrafficJam> trafficJamList;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jam_map_layout);
        Intent intent = getIntent();

        if (intent != null)
        {
            Log.d(TAG, "Got intent");
            currentZoomedLocation = intent.getParcelableExtra("location");
        }

        initMap(currentZoomedLocation);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        trafficJamList = JamListActivity.transportList;
        setMarkers();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        trafficJamList = JamListActivity.transportList;
        initMap(currentZoomedLocation);
        setMarkers();
    }

    private void initMap(LatLng zoomedLocation)
    {
        if (null == googleMap)
        {
            FragmentManager fragmentManager = getFragmentManager();
            MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
            googleMap = mapFragment.getMap();
            if (googleMap == null)
            {
                Toast.makeText(this, "Could not get Map", Toast.LENGTH_SHORT).show();
            }
        }
        if (null != zoomedLocation)
        {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(zoomedLocation).zoom(10).build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }

    private void setMarkers()
    {
        if (trafficJamList != null && !trafficJamList.isEmpty())
        {

            for (TrafficJam jam : trafficJamList)
            {
                double latitude = jam.getLocation().getLatitude();
                double longitude = jam.getLocation().getLongitude();
                setMarker(new LatLng(latitude, longitude));
            }
        }
    }

    private void setMarker(LatLng location)
    {
        MarkerOptions marker = new MarkerOptions().position(location);
        googleMap.addMarker(marker);
    }
}

