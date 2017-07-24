package com.agricx.app.agricximagecapture.ui.fragment;


import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.agricx.app.agricximagecapture.R;
import com.agricx.app.agricximagecapture.utility.AgricxPreferenceKeys;
import com.agricx.app.agricximagecapture.utility.AppConstants;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        addPreferencesFromResource(R.xml.agricx_preferences);
        bindPreferenceToSummary(findPreference(AgricxPreferenceKeys.PF_KEY_CAMERA_ANGLE));
        return view;
    }

    public void bindPreferenceToSummary(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        EditTextPreference anglePreference = (EditTextPreference) preference;
        anglePreference.setSummary(PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), AppConstants.DEFAULT_ALLOWED_CAMERA_ANGLE));
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        EditTextPreference anglePreference = (EditTextPreference) preference;
        if (!TextUtils.isEmpty((String) newValue)) {
            anglePreference.setSummary(String.valueOf(newValue));
            return true;
        } else {
            Toast.makeText(preference.getContext(), R.string.angle_cannot_be_blank, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
