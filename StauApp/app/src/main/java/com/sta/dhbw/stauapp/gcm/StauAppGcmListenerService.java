package com.sta.dhbw.stauapp.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.sta.dhbw.jambeaconrestclient.util.Constants;
import com.sta.dhbw.stauapp.JamListActivity;
import com.sta.dhbw.stauapp.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class StauAppGcmListenerService extends GcmListenerService
{
    private static final String TAG = StauAppGcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        Log.d(TAG, "Received message from " + from);
        digestMessage(data);
    }

    private void digestMessage(Bundle data)
    {
        String type = data.getString("type");
        if (type != null && type.equals("jam"))
        {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            String adminArea = null;
            try
            {
                List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(data.getString(Constants.JAM_LATITUDE))
                        , Double.parseDouble(data.getString(Constants.JAM_LONGITUDE)), 1);
                if (addresses != null && addresses.size() > 0)
                {
                    Address address = addresses.get(0);
                    adminArea = address.getAdminArea();
                    sendNotification("Ein neuer Stau in " + adminArea + " wurde entdeckt!");
                } else
                {
                    sendNotification("Ein neuer Stau wurde entdeckt!");
                }
            } catch (IOException e)
            {
                Log.e(TAG, "Error reverse geocoding location. " + e.getMessage());
            }
        }
    }

    private void sendNotification(String content)
    {
        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder builder = new Notification.Builder(this).
                setSmallIcon(R.mipmap.dh_notification)
                .setContentTitle("Neuer Stau")
                .setContentText(content)
                .setSound(ringtone);

        Intent resultIntent = new Intent(this, JamListActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(JamListActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
}
