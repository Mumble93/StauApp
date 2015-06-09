package com.sta.dhbw.stauapp.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.sta.dhbw.stauapp.R;

/**
 * Class that handles all user editable settings.<br>
 * Default value for delay between beacon activation and beginning of jam detection is set to 3 minutes.
 */
public class SettingsFragment extends PreferenceFragment
{

    EditTextPreference regId, requestHeader, appVersion;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //For debugging only, REMOVE IN PRODUCTION
        regId = (EditTextPreference) findPreference("gcm_id");
        regId.setText(sharedPreferences.getString(PrefFields.PROPERTY_REG_ID, ""));

        //For debugging only, REMOVE IN PRODUCTION
        requestHeader = (EditTextPreference) findPreference("request_header");
        requestHeader.setText(sharedPreferences.getString(PrefFields.PROPERTY_X_REQUEST_ID, ""));

        appVersion = (EditTextPreference) findPreference("app_version");
        appVersion.setText("" + sharedPreferences.getInt(PrefFields.PROPERTY_APP_VERSION, 0));
    }
}
