package com.caudelldevelopment.udacity.capstone.household.household.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class TagIntentService extends IntentService {

    public static final int TAG_SERVICE_RESULT_CODE = 12;

    private static final String ACTION_TAG_FETCH = "com.caudelldevelopment.udacity.capstone.household.household.service.action.TAG_FETCH";
    private static final String ACTION_ALL_TAGS_FETCH = "com.caudelldevelopment.udacity.capstone.household.household.service.action.ALL_TAGS_FETCH";
    private static final String ACTION_TAG_WRITE = "com.caudelldevelopment.udacity.capstone.household.household.service.action.TAG_WRITE";

    private static final String EXTRA_RESULTS = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.RESULTS";
    private static final String EXTRA_TAG_ID = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.TAG_ID";
    private static final String EXTRA_NEW_TAG = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.NEW_TAG";

    private ResultReceiver mResults;

    public TagIntentService() {
        super("TagIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startTagFetch(Context context, ResultReceiver results, String tag_id) {
        Intent intent = new Intent(context, TagIntentService.class);
        intent.setAction(ACTION_TAG_FETCH);
        intent.putExtra(EXTRA_RESULTS, results);
        intent.putExtra(EXTRA_TAG_ID, tag_id);
        context.startService(intent);
    }

    public static void startAllTagsFetch(Context context, ResultReceiver results) {
        Intent intent = new Intent(context, TagIntentService.class);
        intent.setAction(ACTION_ALL_TAGS_FETCH);
        intent.putExtra(EXTRA_RESULTS, results);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionTagWrite(Context context, ResultReceiver results, Tag tag) {
        Intent intent = new Intent(context, TagIntentService.class);
        intent.setAction(ACTION_TAG_WRITE);
        intent.putExtra(EXTRA_RESULTS, results);
        intent.putExtra(EXTRA_NEW_TAG, tag);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TAG_FETCH.equals(action)) {
                mResults = intent.getParcelableExtra(EXTRA_RESULTS);

                String tag_id = intent.getStringExtra(EXTRA_TAG_ID);
                handleTagFetch(tag_id);
            } else if (ACTION_ALL_TAGS_FETCH.equals(action)) {
                mResults = intent.getParcelableExtra(EXTRA_RESULTS);

                handleAllTagsFetch();
            } else if (ACTION_TAG_WRITE.equals(action)) {
                mResults = intent.getParcelableExtra(EXTRA_RESULTS);

                Tag new_tag = intent.getParcelableExtra(EXTRA_NEW_TAG);
                handleTagWrite(new_tag);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    /**
     * Start the TagFetch.
     */
    private void handleTagFetch(String tag_id) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference(Tag.COL_TAG)
                .child(tag_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        finishTagFetch(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void finishTagFetch(DataSnapshot query) {
        Tag tag;

        if (query.exists()) {
            tag = Tag.fromSnapshot(query);
        } else {
            tag = null;
        }

        Bundle data = new Bundle();
        data.putParcelable(Tag.DOC_TAG, tag);

        mResults.send(TAG_SERVICE_RESULT_CODE, data);
        stopSelf();
    }

    /**
     * Get full list of tags.
     */
    private void handleAllTagsFetch() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference(Tag.COL_TAG)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        finishAllTagsFetch(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void finishAllTagsFetch(DataSnapshot query) {
        int count = (int) query.getChildrenCount();
        Tag[] tags = new Tag[count];

        Iterator<DataSnapshot> iterator = query.getChildren().iterator();
        for (int i = 0; i < count; i++) {
            DataSnapshot child = iterator.next();

            Tag tag = Tag.fromSnapshot(child);
            tags[i] = tag;
        }

        Bundle data = new Bundle();
        data.putParcelableArray(Tag.COL_TAG, tags);

        mResults.send(TAG_SERVICE_RESULT_CODE, data);
        stopSelf();
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleTagWrite(Tag tag) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(Tag.COL_TAG)
                .push();

        tag.setId(ref.getKey());

        ref.setValue(tag)
            .addOnSuccessListener(v -> finishTagWrite(tag));
    }

    private void finishTagWrite(Tag tag) {
        Bundle data = new Bundle();
        data.putParcelable(Tag.DOC_TAG, tag);

        mResults.send(TAG_SERVICE_RESULT_CODE, data);
        stopSelf();
    }
}
