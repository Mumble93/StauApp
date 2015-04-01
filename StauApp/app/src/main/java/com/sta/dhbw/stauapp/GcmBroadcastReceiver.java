package com.sta.dhbw.stauapp;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //Specify IntentService
        ComponentName componentName = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        //Start service, keep device awake during launch
        startWakefulService(context, (intent.setComponent(componentName)));
        setResultCode(Activity.RESULT_OK);
    }
}
