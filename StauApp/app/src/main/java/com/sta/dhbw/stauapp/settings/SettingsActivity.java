package com.sta.dhbw.stauapp.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Class that provides the settings editable by the user.
 */
public class SettingsActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }
}
