package com.sta.dhbw.stauapp.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.sta.dhbw.stauapp.JamListActivity;
import com.sta.dhbw.stauapp.R;

public class StauAppGcmListenerService extends GcmListenerService
{
    private static final String TAG = StauAppGcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        String message = data.getString("message");
        Log.d(TAG, "Received message from " + from + ". Message was " + message);
        //ToDo: Implement correct message digestion
        sendNotification("Without content");
    }

    private void sendNotification(String content)
    {
        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder builder = new Notification.Builder(this).
                setSmallIcon(R.mipmap.dh_launcher)
                .setContentTitle("Neuer Stau")
                .setContentText("Ein neuer Stau wurde entdeckt!\n" + content)
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
