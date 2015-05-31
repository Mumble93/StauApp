package com.sta.dhbw.stauapp.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.sta.dhbw.stauapp.R;

public class SettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
