package com.caudelldevelopment.udacity.capstone.household.household;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

/**
 * Created by caude on 2/23/2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    public static SettingsFragment getInstance() {
        SettingsFragment result = new SettingsFragment();

        Bundle args = new Bundle();
        // ....
        result.setArguments(args);

        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference preference = getPreferenceManager().findPreference(getString(R.string.logout_key));
        preference.setOnPreferenceClickListener(preference1 -> {
            Log.v(LOG_TAG, "onCreate, onClickListener - preference has been clicked!!!");
            return true;
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }
}
