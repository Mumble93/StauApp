package com.sta.dhbw.stauapp.dialogs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestIssueBroadcastReceiver extends BroadcastReceiver
{
    public static final String REST_ISSUE = "com.sta.dhbw.stauapp.REST_ISSUE";

    private static final String TAG = RestIssueBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.w(TAG, "Received REST Issue");
    }
}
