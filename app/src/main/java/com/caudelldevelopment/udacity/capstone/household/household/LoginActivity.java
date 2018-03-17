package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

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

    private static final int SIGN_IN_REQ_CODE = 123;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseUser fireUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fireUser != null) {
            doLogin();
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    SIGN_IN_REQ_CODE
            );
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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
            }
        }
    }

    private void doLogin() {
        // Get the user object. If it doesn't exist, create one.
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            db.collection(User.COL_TAG)
                    .document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(this);
        } else {
            Snackbar.make(findViewById(R.id.login_root_layout), getString(R.string.login_user_null), Snackbar.LENGTH_LONG);
        }
    }

    private void startMainActivity(User user) {
        Log.v(LOG_TAG, "startMainActivity has started!!!");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(User.DOC_TAG, user);

        startActivity(intent);
    }

    @Override
    public void onSuccess(DocumentSnapshot documentSnapshot) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User user;

        if (documentSnapshot.exists()) {
            // Launch activity with user object
            user = User.fromDoc(documentSnapshot);
            startMainActivity(user);
        } else {
            // User must not exist yet. Create it, then...
            user = new User(firebaseUser);
             // Dialog to ask about the family at login?

            // ... submit it to Firestore. Once complete, launch MainActivity.
            db.collection(User.COL_TAG)
                    .document(user.getId())
                    .set(user)
                    .addOnCompleteListener(task -> startMainActivity(user))
                    .addOnFailureListener(Throwable::printStackTrace);
        }
    }
}

