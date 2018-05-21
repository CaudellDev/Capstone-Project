package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Intent;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.caudelldevelopment.udacity.capstone.household.household.service.FamilyIntentService;
import com.caudelldevelopment.udacity.capstone.household.household.service.MyResultReceiver;
import com.caudelldevelopment.udacity.capstone.household.household.service.UserIntentService;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements MyResultReceiver.Receiver {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private static final int MAIN_ACTIVITY_REQ_CODE = 234;

    private static final int SIGN_IN_REQ_CODE = 123;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
    );

    private User mUser;
    private Family mFamily;

    private MyResultReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseUser fireUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fireUser != null) {
            doLogin();
        } else {
            showLogin();
        }
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            mReceiver.setReceiver(null);
            mReceiver = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                doLogin();
            } else {
                Snackbar.make(findViewById(R.id.login_root_layout), R.string.login_error_snackbar, Snackbar.LENGTH_INDEFINITE).show();
            }
        } else if (requestCode == MAIN_ACTIVITY_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                boolean sign_out_clicked = data.getBooleanExtra(SettingsActivity.SIGN_OUT_CLICKED, false);
                if (sign_out_clicked) doSignOut();

                boolean back_btn_clicked = data.getBooleanExtra(MainActivity.ON_UP_CLICKED, false);
                if (back_btn_clicked) finish();
            }
        }
    }

    private void doSignOut() {
        FirebaseAuth.getInstance().signOut();
        showLogin();
    }

    private void showLogin() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                SIGN_IN_REQ_CODE
        );
    }

    private void doLogin() {
        // Get the user object. If it doesn't exist, create one.
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            mReceiver = new MyResultReceiver(new Handler());
            mReceiver.setReceiver(this);

            UserIntentService.startUserFetch(this, mReceiver, firebaseUser.getUid());
        } else {
            Snackbar.make(findViewById(R.id.login_root_layout), getString(R.string.login_user_null), Snackbar.LENGTH_LONG);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);

        // For some reason, if I passed these as separate Parcelable Extras,
        // the mUser (or the first of any two Parcelable objects) would be null.
        // Putting them into a bundle didn't work either.
        intent.putExtra("user_data", new Parcelable[] {mUser, mFamily});

        startActivityForResult(intent, MAIN_ACTIVITY_REQ_CODE);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case UserIntentService.USER_SERVICE_RESULT_CODE:
                User temp_user = resultData.getParcelable(User.DOC_TAG);

                if (temp_user != null) {
                    mUser = temp_user;
                    mReceiver = null;

                    if (mUser.hasFamily() && mFamily == null) {
                        mReceiver = new MyResultReceiver(new Handler());
                        mReceiver.setReceiver(this);

                        FamilyIntentService.startFamilyFetch(this, mReceiver, mUser.getFamily());
                    } else {
                        startMainActivity();
                    }
                } else {
                    // If it's null, it must be a new user. Create the user in Firebase.
                    FirebaseUser fireUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (fireUser != null) {
                        User user = new User(fireUser);

                        mReceiver = new MyResultReceiver(new Handler());
                        mReceiver.setReceiver(this);

                        UserIntentService.startUserWrite(this, mReceiver, user, null);
                    } else {
                        Snackbar.make(findViewById(R.id.login_root_layout), getString(R.string.login_user_null), Snackbar.LENGTH_LONG);
                    }
                }

                break;
            case UserIntentService.WRITE_USER_SERVICE_RESULT_CODE:
                User new_user = resultData.getParcelable(User.DOC_TAG);

                if (new_user != null) {
                    mUser = new_user;
                    // New users won't ever have a family selected, so start main activity now.
                    startMainActivity();
                } else {
                    Snackbar.make(findViewById(R.id.login_root_layout), getString(R.string.login_user_null), Snackbar.LENGTH_LONG);
                }
                break;
            case FamilyIntentService.FAMILY_SERVICE_RESULT_CODE:
                Family temp_fam = resultData.getParcelable(Family.DOC_TAG);

                if (temp_fam != null) {
                    mFamily = temp_fam;
                    mReceiver = null;

                    if (mUser != null) {
                        startMainActivity();
                    }
                }

                break;
        }
    }
}

