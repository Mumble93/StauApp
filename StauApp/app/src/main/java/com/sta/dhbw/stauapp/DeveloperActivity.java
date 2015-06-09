package com.sta.dhbw.stauapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sta.dhbw.jambeaconrestclient.IHeartbeatCallback;
import com.sta.dhbw.jambeaconrestclient.ITrafficJamCallback;
import com.sta.dhbw.jambeaconrestclient.IUserCallback;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.jambeaconrestclient.TrafficJam;
import com.sta.dhbw.stauapp.settings.PrefFields;
import com.sta.dhbw.stauapp.settings.SettingsActivity;

import java.util.Date;
import java.util.List;

/**
 * Activity that displays various developer options. Should not be visible to normal user.
 */
public class DeveloperActivity extends Activity implements IHeartbeatCallback, ITrafficJamCallback, IUserCallback
{
    private static final String TAG = DeveloperActivity.class.getSimpleName();

    private JamBeaconRestClient restClient;

    private SharedPreferences sharedPreferences;

    private Button sendTestJamButton, reRegisterButton;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_developer);

        restClient = new JamBeaconRestClient();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        sendTestJamButton = (Button) findViewById(R.id.send_test_jam);
        reRegisterButton = (Button) findViewById(R.id.re_register_btn);


        sendTestJamButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Location location = new Location(LocationManager.GPS_PROVIDER);
                //Karlsbad - Pforzheim
                location.setLatitude(48.912311);
                location.setLongitude(8.621221);
                TrafficJam jam = new TrafficJam(location, new Date().getTime());
                String requestId = sharedPreferences.getString(PrefFields.PROPERTY_X_REQUEST_ID, "");
                restClient.postTrafficJam(jam, requestId, DeveloperActivity.this);
            }
        });

        reRegisterButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                String gcmRegId = sharedPreferences.getString(PrefFields.PROPERTY_REG_ID, "");
                if (gcmRegId == null || gcmRegId.isEmpty())
                {
                    Toast.makeText(v.getContext(), "No GCM Registration Id found", Toast.LENGTH_SHORT).show();
                } else
                {

                    restClient.registerUser(gcmRegId, DeveloperActivity.this);

                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onCheckComplete(boolean success)
    {

    }

    @Override
    public void onGetTrafficJamComplete(TrafficJam trafficJam)
    {

    }

    @Override
    public void onGetJamListComplete(List<TrafficJam> trafficJamList)
    {

    }

    @Override
    public void onTrafficJamUpdateComplete(TrafficJam updatedJam)
    {

    }

    @Override
    public void onTrafficJamPostComplete(TrafficJam jam)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (jam != null)
        {
            builder.setMessage("Returned Traffic Jam with Id " + jam.getId().toString());
            builder.setMessage("Success");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
        } else
        {
            builder.setMessage("Failed to get Response. Check Log.");
            builder.setTitle("Failure");
            builder.setNegativeButton(getString(R.string.dialog_close), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
        }

        builder.create().show();
    }

    @Override
    public void onRegisterComplete(String xRequestId)
    {
        if (xRequestId != null && !xRequestId.isEmpty())
        {
            Toast.makeText(this, "Got Request Id " + xRequestId, Toast.LENGTH_LONG).show();
            sharedPreferences.edit().putString(PrefFields.PROPERTY_X_REQUEST_ID, xRequestId).apply();
        } else
        {
            Toast.makeText(this, "Returned String was empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUserUpdateComplete(String updatedXRequestId)
    {

    }

    @Override
    public void onUserUnregister(Integer resultCode)
    {

    }
}
