package com.sta.dhbw.stauapp.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.sta.dhbw.stauapp.MainActivity;
import com.sta.dhbw.stauapp.R;

public class SettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        /*SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        EditTextPreference editText = (EditTextPreference) findPreference("registrationId_preference");
        editText.getEditText().setText(sharedPreferences.getString(MainActivity.PROPERTY_REG_ID, ""));*/
    }
}
