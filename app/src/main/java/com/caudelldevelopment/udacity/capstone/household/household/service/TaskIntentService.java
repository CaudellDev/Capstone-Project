package com.caudelldevelopment.udacity.capstone.household.household.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Iterator;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class TaskIntentService extends IntentService {

    public static final int PERSONAL_TASK_SERVICE_RESULT_CODE = 13;
    public static final int FAMILY_TASK_SERVICE_RESULT_CODE = 14;

    private static final String LOG_TAG = TaskIntentService.class.getSimpleName();

    private static final String ACTION_TASK_FETCH = "com.caudelldevelopment.udacity.capstone.household.household.service.action.TASK_FETCH";
    private static final String ACTION_ALL_TASKS_FETCH = "com.caudelldevelopment.udacity.capstone.household.household.service.action.ALL_TASKS_FETCH";
    private static final String ACTION_TASK_WRITE = "com.caudelldevelopment.udacity.capstone.household.household.service.action.TASK_WRITE";

    private static final String EXTRA_RESULTS = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.RESULTS";
    private static final String EXTRA_TASK_ID = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.TASK_ID";
    private static final String EXTRA_ACCESS_ID = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.ACCESS_ID";
    private static final String EXTRA_NEW_TASK = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.NEW_TASK";
    private static final String EXTRA_OLD_TASK = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.OLD_TASK";

    private ResultReceiver mResults;

    public TaskIntentService() {
        super("TaskIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startTaskFetch(Context context, ResultReceiver results, String task_id) {
        Intent intent = new Intent(context, TaskIntentService.class);
        intent.setAction(ACTION_TASK_FETCH);
        intent.putExtra(EXTRA_RESULTS, results);
        intent.putExtra(EXTRA_TASK_ID, task_id);
        context.startService(intent);
    }

    public static void startAllTasksFetch(Context context, ResultReceiver results, String access_id) {
        Intent intent = new Intent(context, TaskIntentService.class);
        intent.setAction(ACTION_ALL_TASKS_FETCH);
        intent.putExtra(EXTRA_RESULTS, results);
        intent.putExtra(EXTRA_ACCESS_ID, access_id);

        Log.v(LOG_TAG, "startAllTasksFetch - pre service start.");
        context.startService(intent);
        Log.v(LOG_TAG, "startAllTasksFetch - post service start.");
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startTaskWrite(Context context, ResultReceiver results, Task new_task, @Nullable Task old_task) {
        Intent intent = new Intent(context, TaskIntentService.class);
        intent.setAction(ACTION_TASK_WRITE);
        intent.putExtra(EXTRA_RESULTS, results);
        intent.putExtra(EXTRA_NEW_TASK, new_task);
        intent.putExtra(EXTRA_OLD_TASK, old_task);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TASK_FETCH.equals(action)) {
                mResults = intent.getParcelableExtra(EXTRA_RESULTS);

                String task_id = intent.getStringExtra(EXTRA_TASK_ID);
                handleTaskFetch(task_id);
            } else if (ACTION_ALL_TASKS_FETCH.equals(action)) {
                mResults = intent.getParcelableExtra(EXTRA_RESULTS);

                String access_id = intent.getStringExtra(EXTRA_ACCESS_ID);
                handleAllTasksFetch(access_id);
            } else if (ACTION_TASK_WRITE.equals(action)) {
                mResults = intent.getParcelableExtra(EXTRA_RESULTS);

                Task new_task = intent.getParcelableExtra(EXTRA_NEW_TASK);
                Task old_task = intent.getParcelableExtra(EXTRA_OLD_TASK);
                handleTaskWrite(new_task, old_task);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleTaskFetch(String task_id) {

    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleAllTasksFetch(String access_id) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference(Task.COL_TAG)
                .orderByChild(Task.ACCESS_ID)
                .equalTo(access_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        finishAllTasksFetch(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void finishAllTasksFetch(DataSnapshot query) {
        int count = (int) query.getChildrenCount();
        Task[] task_arr = new Task[count];

        Iterator<DataSnapshot> iterator = query.getChildren().iterator();
        for (int i = 0; i < count; i++) {
            DataSnapshot curr = iterator.next();

            Task task = Task.fromSnapshot(curr);
            task_arr[i] = task;
        }

        // Sort by the date
        Arrays.sort(task_arr);

        for (Task curr : task_arr) {
            Log.v(LOG_TAG, "finishAllTasksFetch - task name: " + curr.getName());
        }

        int result_code = PERSONAL_TASK_SERVICE_RESULT_CODE; // Personal will just be default, but it shouldn't matter.
        if (task_arr.length > 0) {
            boolean family = task_arr[0].isFamily();
            if (family) {
                result_code = FAMILY_TASK_SERVICE_RESULT_CODE;
            } else {
                result_code = PERSONAL_TASK_SERVICE_RESULT_CODE;
            }
        }


        Bundle data = new Bundle();
        data.putParcelableArray(Task.COL_TAG, task_arr);
        mResults.send(result_code, data);
        stopSelf();
    }

    private void handleTaskWrite(Task new_task, Task old_task) {

    }
}
