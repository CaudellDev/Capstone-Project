package com.caudelldevelopment.udacity.capstone.household.household.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling operations to fetch
 * or update User data, and other dependant values in the database.
 */
public class UserIntentService extends IntentService {

    private static final String LOG_TAG = UserIntentService.class.getSimpleName();

    private static final String ACTION_USER_FETCH = "com.caudelldevelopment.udacity.capstone.household.household.data.action.USER_FETCH";
    private static final String ACTION_USER_WRITE = "com.caudelldevelopment.udacity.capstone.household.household.data.action.USER_WRITE";

    private static final String EXTRA_USER_ID = "com.caudelldevelopment.udacity.capstone.household.household.data.extra.USER_ID";
    private static final String EXTRA_RESULTS = "com.caudelldevelopment.udacity.capstone.household.household.data.extra.RESULTS";
    private static final String EXTRA_NEW_USER = "com.caudelldevelopment.udacity.capstone.household.household.data.extra.NEW_USER";
    private static final String EXTRA_OLD_USER = "com.caudelldevelopment.udacity.capstone.household.household.data.extra.OLD_USER";

    private ResultReceiver mResults;

    public UserIntentService() {
        super("UserIntentService");
    }

    /**
     * Starts this service to perform action User Fetch using the given id. ResultReceiver
     * will be used to alert the activity or fragment the fetch is complete, and provide the user.
     */
    public static void startUserFetch(Context context, ResultReceiver results, String user_id) {
        Intent intent = new Intent(context, UserIntentService.class);
        intent.setAction(ACTION_USER_FETCH);
        intent.putExtra(EXTRA_USER_ID, user_id);
        intent.putExtra(EXTRA_RESULTS, results);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action User Write with the new and old user values. The old_user
     * will be null if it's a new user.
     */
    public static void startUserWrite(Context context, ResultReceiver results, User new_user, User old_user) {
        Intent intent = new Intent(context, UserIntentService.class);
        intent.setAction(ACTION_USER_WRITE);
        intent.putExtra(EXTRA_RESULTS, results);
        intent.putExtra(EXTRA_NEW_USER, new_user);
        intent.putExtra(EXTRA_OLD_USER, old_user);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_USER_FETCH.equals(action)) {
                mResults = intent.getParcelableExtra(EXTRA_RESULTS);

                final String user_id = intent.getStringExtra(EXTRA_USER_ID);
                handleUserFetch(user_id);
            } else if (ACTION_USER_WRITE.equals(action)) {
                mResults = intent.getParcelableExtra(EXTRA_RESULTS);

                User new_user = intent.getParcelableExtra(EXTRA_NEW_USER);
                User old_user = intent.getParcelableExtra(EXTRA_OLD_USER);
                handleUserWrite(new_user, old_user);
            }
        }
    }

    /**
     * Handle action User Fetch.
     */
    private void handleUserFetch(String user_id) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference(User.COL_TAG)
                .child(user_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        finishUserFetch(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void finishUserFetch(DataSnapshot query) {
        User user;

        if (query.exists()) {
            user = User.fromSnapshot(query);
        } else {
            user = null;
        }

        Bundle data = new Bundle();
        data.putParcelable(User.DOC_TAG, user);

        mResults.send(0, data);
        stopSelf();
    }

    /**
     * Handle action User Write.
     */
    private void handleUserWrite(User new_user, User old_user) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        Map<String, Object> updates = new HashMap<>();

        String user_path = makeFirebasePath(User.COL_TAG, new_user.getId());
        updates.put(user_path, new_user.toMap());

        // old_user will be null if it's a new user.
        if (old_user != null) {
            // Comparing old and new values to see what changed.

            // Joined a family
            if (new_user.hasFamily() && !old_user.hasFamily()) {
                String path = makeFirebasePath(Family.COL_TAG, new_user.getFamily(), Family.MEMBERS_ID, new_user.getId());
                updates.put(path, new_user.getName());
            }

            // Left a family
            if (!new_user.hasFamily() && old_user.hasFamily()) {
                String path = makeFirebasePath(Family.COL_TAG, old_user.getFamily(), Family.MEMBERS_ID, new_user.getId());
                updates.put(path, null);
            }
        }

        db.getReference()
                .updateChildren(updates)
                .addOnSuccessListener(v -> finishUserWrite(new_user));
    }

    private void finishUserWrite(User user) {
        Bundle data = new Bundle();
        data.putParcelable(User.DOC_TAG, user);

        mResults.send(0, data);
        stopSelf();
    }

    private String makeFirebasePath(String... args) {
        StringBuilder builder = new StringBuilder();

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String curr = args[i];

                builder.append(curr);

                // Add a slash between the args, except the last one
                if (i != args.length - 1) {
                    builder.append("/");
                }
            }
        }

        return builder.toString();
    }
}
