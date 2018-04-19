package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements OnSuccessListener<DocumentSnapshot> {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private static final int MAIN_ACTIVITY_REQ_CODE = 234;

    private static final int SIGN_IN_REQ_CODE = 123;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
    );

    private User mUser;
    private Family mFamily;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(LOG_TAG, "onActivityResult has started!!! requestCode: " + requestCode);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                doLogin();
            } else {
                Log.w(LOG_TAG, "onActivityResult - Error Logging in user. ResultCode: " + resultCode);

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
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(User.COL_TAG)
                    .document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(this);
        } else {
            Snackbar.make(findViewById(R.id.login_root_layout), getString(R.string.login_user_null), Snackbar.LENGTH_LONG);
        }
    }

    private void startMainActivity() {
        Log.v(LOG_TAG, "startMainActivity has started!!!");

        Intent intent = new Intent(this, MainActivity.class);

        // For some reason, if I passed these as Parcelable Extras,
        // the mUser (or one of any two Parcelable objects) would be null.
        // Putting them into a bundle didn't work either.
        intent.putExtra("user_data", new Parcelable[] {mUser, mFamily});

        startActivityForResult(intent, MAIN_ACTIVITY_REQ_CODE);
    }

    @Override
    public void onSuccess(DocumentSnapshot documentSnapshot) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (documentSnapshot.exists()) {
            // Launch activity with user object
            mUser = User.fromDoc(documentSnapshot);
            Log.v(LOG_TAG, "onSuccess, doc exists - mUser == null: " + (mUser == null));

            if (mUser.getFamily() != null && !mUser.getFamily().isEmpty()) {
                db.collection(Family.COL_TAG)
                        .document(mUser.getFamily())
                        .get()
                        .addOnSuccessListener(famSnapshot -> {
                            mFamily = Family.fromDoc(famSnapshot);
                            startMainActivity();
                        });
            } else {
                startMainActivity();
            }
        } else {
            // User must not exist yet. Create it, then...
            mUser = new User(firebaseUser);

            // ... submit it to Firestore. Once complete, launch MainActivity.
            db.collection(User.COL_TAG)
                    .document(mUser.getId())
                    .set(mUser.toMap())
                    .addOnSuccessListener(task -> startMainActivity())
                    .addOnFailureListener(Throwable::printStackTrace);
        }
    }
}

