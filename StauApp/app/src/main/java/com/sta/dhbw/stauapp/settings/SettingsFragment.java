package com.sta.dhbw.stauapp.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

import com.sta.dhbw.stauapp.MainActivity;
import com.sta.dhbw.stauapp.R;

public class SettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        EditTextPreference editText = (EditTextPreference) findPreference("registrationId_preference");
        editText.getEditText().setText(sharedPreferences.getString(MainActivity.PROPERTY_REG_ID, ""));
    }
}
