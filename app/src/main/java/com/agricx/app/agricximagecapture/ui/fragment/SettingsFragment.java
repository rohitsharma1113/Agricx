package com.agricx.app.agricximagecapture.ui.fragment;

import android.content.SharedPreferences;
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
        bindPreferenceToSummary(findPreference(AgricxPreferenceKeys.PF_KEY_CAMERA_ANGLE), AppConstants.DEFAULT_ALLOWED_CAMERA_ANGLE);
        bindPreferenceToSummary(findPreference(AgricxPreferenceKeys.PF_MARKER_TYPE), AppConstants.DEFAULT_MARKER_TYPE);
        return view;
    }

    public void bindPreferenceToSummary(Preference preference, String defValue) {
        preference.setOnPreferenceChangeListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        EditTextPreference editTextPreference = (EditTextPreference) preference;
        editTextPreference.setSummary(preferences.getString(preference.getKey(), defValue));
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        EditTextPreference editTextPreference = (EditTextPreference) preference;
        String value = ((String) newValue).trim();
        if (!TextUtils.isEmpty(value)) {
            editTextPreference.setSummary(String.valueOf(newValue));
            return true;
        } else {
            Toast.makeText(preference.getContext(), R.string.value_cannot_be_blank, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
