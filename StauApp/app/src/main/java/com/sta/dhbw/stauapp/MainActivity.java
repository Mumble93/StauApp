package com.sta.dhbw.stauapp;

import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity
{
    final LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkGps();


    }

    @Override
    protected void onResume()
    {
       checkGps();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkGps()
    {
        TextView textView = (TextView) findViewById(R.id.status);

        boolean isActive =service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isActive)
        {
            textView.setText("GPS not enabled");
        } else
        {
            textView.setText("GPS enabled");
        }
    }
}
