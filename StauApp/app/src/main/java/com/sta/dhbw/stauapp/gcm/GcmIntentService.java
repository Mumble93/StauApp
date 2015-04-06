package com.sta.dhbw.stauapp.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sta.dhbw.stauapp.MainActivity;
import com.sta.dhbw.stauapp.R;

public class GcmIntentService extends IntentService
{
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GcmIntentService";
    NotificationCompat.Builder builder;

    public GcmIntentService()
    {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())
        {
            //Filter messages by message type
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                //ToDo: Handle incoming messages
            }
            sendNotification("Received: " + extras.toString());
            Log.i(TAG, "Received: " + extras.toString());
        }
        //Release wake lock of WakefulBroadcastReceiver
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg)
    {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.airdroid)
                .setContentTitle("GCM Notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
