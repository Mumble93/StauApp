package com.sta.dhbw.stauapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BeaconService extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
