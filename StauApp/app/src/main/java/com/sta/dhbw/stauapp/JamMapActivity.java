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

public class JamMapActivity extends Activity
{
    private static final String TAG = JamMapActivity.class.getSimpleName();
    private GoogleMap googleMap;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jam_map_layout);
        Intent intent = getIntent();
        LatLng latLng = null;

        if (intent != null)
        {
            Log.d(TAG, "Got intent with parcel");
            latLng = intent.getParcelableExtra("location");
        }

        initMap();

        if (null != latLng)
        {
            // create marker
            MarkerOptions marker = new MarkerOptions().position(latLng);

            // adding marker
            googleMap.addMarker(marker);

            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        initMap();
    }

    private void initMap()
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
    }
}
