package com.caudelldevelopment.udacity.capstone.household.household;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnSettingsFragListener {

    public static final String SIGN_OUT_CLICKED = "on_sign_out_clicked_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.settings_view_holder, SettingsFragment.getInstance(), "settings_fragment")
                                    .commit();
    }

    @Override
    public void doSignOut() {
        Intent data = new Intent();
        data.putExtra(SIGN_OUT_CLICKED, true);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
