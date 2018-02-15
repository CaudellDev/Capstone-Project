package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
public class LoginActivity extends AppCompatActivity {

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

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();

        startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                SIGN_IN_REQ_CODE
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Do something with auth? It should open to MainActivity anyway.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                doLogin();

//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                Log.v(LOG_TAG, "onActivityResult - user.displayName: " + user.getDisplayName());
            } else {
                Log.w(LOG_TAG, "onActivityResult - Error Logging in user. ResultCode: " + resultCode);
            }
        }
    }

    private void doLogin() {
        Log.v(LOG_TAG, "doLogin has been started.");

        // Get the user object. If it doesn't exist, create one.
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(User.COL_TAG)
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.v(LOG_TAG, "doLogin - onSuccessListener has started. docSnap.exists: " + documentSnapshot.exists());

                    User user;

                    if (documentSnapshot.exists()) {
                        // Launch activity with user object
                        user = documentSnapshot.toObject(User.class);
                        startMainActivity(user);
                    } else {
                        // User must not exist yet. Create it, then...
                        user = new User();
                        user.setId(firebaseUser.getUid());
                        user.setName(firebaseUser.getDisplayName());
                        user.setFamily(""); // Dialog to ask about the family at login?

                        // ... submit it to Firestore. Once complete, launch MainActivity.
                        db.collection(User.COL_TAG)
                                .document(user.getId())
                                .set(user)
                                .addOnCompleteListener(task -> {
                                    Log.v(LOG_TAG, "doLogin, user query failed. Submitted user to Firebase. " + user.getName());
                                    startMainActivity(user);
                                }).addOnFailureListener(err -> {
                                    Log.v(LOG_TAG, "doLogin, user query failed. Also failed to submit new user to Firebase. " + user.getName() + ", " + err.getMessage());
                                    err.printStackTrace();
                                });
                    }
                });
    }

    private void startMainActivity(User user) {
        Log.v(LOG_TAG, "Starting MainActivity!!!!");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(User.DOC_TAG, user);

        startActivity(intent);
    }
}

