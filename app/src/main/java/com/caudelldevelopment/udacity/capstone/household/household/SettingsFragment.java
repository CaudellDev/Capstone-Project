package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by caude on 2/23/2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    private OnSettingsFragListener mListener;

    public static SettingsFragment getInstance() {
        SettingsFragment result = new SettingsFragment();

        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference preference = getPreferenceManager().findPreference(getString(R.string.logout_key));
        preference.setOnPreferenceClickListener(pref -> {
            mListener.doSignOut();
            return true;
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnSettingsFragListener) {
            mListener = (OnSettingsFragListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSettingsFragListener");
        }
    }

    public interface OnSettingsFragListener {
        void doSignOut();
    }
}
