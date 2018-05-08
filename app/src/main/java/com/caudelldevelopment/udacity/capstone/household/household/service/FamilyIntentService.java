package com.caudelldevelopment.udacity.capstone.household.household.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FamilyIntentService extends IntentService {

    public static final int FAMILY_SERVICE_RESULT_CODE = 11;
    public static final int FAMILY_BIND_SERVICE_RESULT_CODE = 111;

    private static final String LOG_TAG = FamilyIntentService.class.getSimpleName();

    private static final String ACTION_FAMILY_FETCH = "com.caudelldevelopment.udacity.capstone.household.household.service.action.FAMILY_FETCH";
    private static final String ACTION_ALL_FAMILIES_FETCH = "com.caudelldevelopment.udacity.capstone.household.household.service.action.ALL_FAMILIES_FETCH";
    private static final String ACTION_FAMILY_WRITE = "com.caudelldevelopment.udacity.capstone.household.household.service.action.FAMILY_WRITE";
    private static final String ACTION_FAMILY_BIND = "com.caudelldevelopment.udacity.capstone.household.household.service.action.FAMILY_BIND";

    private static final String EXTRA_RESULTS = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.RESULTS";
    private static final String EXTRA_FAM_ID = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.FAM_ID";
    private static final String EXTRA_NEW_FAM = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.FAM_ID";
    private static final String EXTRA_SERVICE_CONN = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.SERVICE_CONNECTION";

    private ResultReceiver mResults;
    private MyBinder mBinder;

    public FamilyIntentService() {
        super("FamilyIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startFamilyFetch(Context context, ResultReceiver receiver, String family_id) {
        Intent intent = new Intent(context, FamilyIntentService.class);
        intent.setAction(ACTION_FAMILY_FETCH);
        intent.putExtra(EXTRA_RESULTS, receiver);
        intent.putExtra(EXTRA_FAM_ID, family_id);
        context.startService(intent);
    }

    public static void startAllFamiliesFetch(Context context, ResultReceiver results) {
        Intent intent = new Intent(context, FamilyIntentService.class);
        intent.setAction(ACTION_ALL_FAMILIES_FETCH);
        intent.putExtra(EXTRA_RESULTS, results);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * This will probably only be called when the user creates a new family.
     *
     * @see IntentService
     */
    public static void startFamilyWrite(Context context, ResultReceiver receiver, Family new_family) {
        Intent intent = new Intent(context, FamilyIntentService.class);
        intent.setAction(ACTION_FAMILY_WRITE);
        intent.putExtra(EXTRA_RESULTS, receiver);
        intent.putExtra(EXTRA_NEW_FAM, new_family);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FAMILY_FETCH.equals(action)) {
                mResults = intent.getParcelableExtra(EXTRA_RESULTS);

                String family_id = intent.getStringExtra(EXTRA_FAM_ID);
                handleFamilyFetch(family_id);
            } else if (ACTION_FAMILY_WRITE.equals(action)) {
                mResults = intent.getParcelableExtra(EXTRA_RESULTS);

                Family new_family = intent.getParcelableExtra(EXTRA_NEW_FAM);
                handleFamilyWrite(new_family);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleFamilyFetch(String fam_id) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference(Family.COL_TAG)
                .child(fam_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        finishFamilyFetch(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void finishFamilyFetch(DataSnapshot query) {
        Family family;

        if (query.exists()) {
            Log.v(LOG_TAG, "finishFamilyFetch - query key: " + query.getKey());
            family = Family.fromSnapshot(query);
        } else {
            family = null;
        }

        Bundle data = new Bundle();
        data.putParcelable(Family.DOC_TAG, family);

        mResults.send(FAMILY_SERVICE_RESULT_CODE, data);
        stopSelf();
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleFamilyWrite(Family new_family) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // Assign an id to the new family
        String fam_id = db.getReference(Family.COL_TAG).push().getKey();
        new_family.setId(fam_id);

        Map<String, Object> updates = new HashMap<>();

        String family_path = makeFirebasePath(Family.COL_TAG, new_family.getId());
        updates.put(family_path, new_family.toMap());

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String user_path = makeFirebasePath(User.COL_TAG, user_id, User.FAMILY_ID);
        updates.put(user_path, new_family.getId());

        db.getReference()
                .updateChildren(updates)
                .addOnSuccessListener(v -> finishFamilyWrite(new_family));
    }

    private void finishFamilyWrite(Family family) {
        Bundle data = new Bundle();
        data.putParcelable(Family.DOC_TAG, family);

        mResults.send(FAMILY_SERVICE_RESULT_CODE, data);
        stopSelf();
    }


    // ++++++ Binding ++++++

    public static void bindFamilies(Context context, ResultReceiver results, ServiceConnection connection) {
        Intent intent = new Intent(context, FamilyIntentService.class);
        intent.setAction(ACTION_FAMILY_BIND);
        intent.putExtra(EXTRA_RESULTS, results);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_FAMILY_BIND:
                    mResults = intent.getParcelableExtra(EXTRA_RESULTS);
                    handleFamilyBind();
                    mBinder = new MyBinder();
                    return mBinder;
            }
        }

        return null;
    }

    private void handleFamilyBind() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference(Family.COL_TAG)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        updateFamilyBind(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateFamilyBind(DataSnapshot query) {
        Log.v(LOG_TAG, "updateFamilyBind has started!!!! query key: " + query.getKey() + ", count: " + query.getChildrenCount());

        int count = (int) query.getChildrenCount();
        Family[] family_list = new Family[count];

        Iterator<DataSnapshot> iterator = query.getChildren().iterator();
        for (int i = 0; i < count; i++) {
            DataSnapshot curr = iterator.next();
            Log.v(LOG_TAG, "updateFamilyBind - curr child key: " + curr.getKey());

            Family family = Family.fromSnapshot(curr);
            family_list[i] = family;
        }

        Bundle data = new Bundle();
        data.putParcelableArray(Family.COL_TAG, family_list);

        mResults.send(FAMILY_BIND_SERVICE_RESULT_CODE, data);
    }

    public class MyBinder extends Binder {
        FamilyIntentService getService() {
            return FamilyIntentService.this;
        }
    }


    // ++++++ Utility ++++++

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
