package com.caudelldevelopment.udacity.capstone.household.household.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TaskIntentService extends IntentService {

    public static final int PERSONAL_TASK_SERVICE_RESULT_CODE = 41;
    public static final int FAMILY_TASK_SERVICE_RESULT_CODE = 42;
    public static final int WRITE_TASK_SERVICE_RESULT_CODE = 43;
    public static final int DELETE_TASK_SERVICE_RESULT_CODE = 44;

    private static final String LOG_TAG = TaskIntentService.class.getSimpleName();

    private static final String ACTION_ALL_TASKS_FETCH = "com.caudelldevelopment.udacity.capstone.household.household.service.action.ALL_TASKS_FETCH";
    private static final String ACTION_TASK_WRITE = "com.caudelldevelopment.udacity.capstone.household.household.service.action.TASK_WRITE";
    private static final String ACTION_TASK_DELETE = "com.caudelldevelopment.udacity.capstone.household.household.service.action.TASK_DELETE";

    private static final String EXTRA_RESULTS = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.RESULTS";
    private static final String EXTRA_ACCESS_ID = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.ACCESS_ID";
    private static final String EXTRA_IS_FAMILY = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.IS_FAMILY";
    private static final String EXTRA_DELETE_TASK = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.DELETE_TASK";
    public static final String EXTRA_NEW_TASK = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.NEW_TASK";
    public static final String EXTRA_OLD_TASK = "com.caudelldevelopment.udacity.capstone.household.household.service.extra.OLD_TASK";

    private ResultReceiver mResults;

    public TaskIntentService() {
        super("TaskIntentService");
    }

    public static void startAllTasksFetch(Context context, ResultReceiver results, String access_id, boolean family) {
        Intent intent = new Intent(context, TaskIntentService.class);
        intent.setAction(ACTION_ALL_TASKS_FETCH);
        intent.putExtra(EXTRA_RESULTS, results);
        intent.putExtra(EXTRA_ACCESS_ID, access_id);
        intent.putExtra(EXTRA_IS_FAMILY, family);
        context.startService(intent);
    }

    public static void startTaskWrite(Context context, ResultReceiver results, Task new_task, @Nullable Task old_task) {
        Intent intent = new Intent(context, TaskIntentService.class);
        intent.setAction(ACTION_TASK_WRITE);
        intent.putExtra(EXTRA_RESULTS, results);
        intent.putExtra(EXTRA_NEW_TASK, new_task);
        intent.putExtra(EXTRA_OLD_TASK, old_task);
        context.startService(intent);
    }

    public static void startTaskDelete(Context context, ResultReceiver results, Task task) {
        Intent intent = new Intent(context, TaskIntentService.class);
        intent.setAction(ACTION_TASK_DELETE);
        intent.putExtra(EXTRA_RESULTS, results);
        intent.putExtra(EXTRA_DELETE_TASK, task);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mResults = intent.getParcelableExtra(EXTRA_RESULTS);

            final String action = intent.getAction();
            if (ACTION_ALL_TASKS_FETCH.equals(action)) {
                String access_id = intent.getStringExtra(EXTRA_ACCESS_ID);
                boolean is_family = intent.getBooleanExtra(EXTRA_IS_FAMILY, false);
                handleAllTasksFetch(access_id, is_family);
            } else if (ACTION_TASK_WRITE.equals(action)) {
                Task new_task = intent.getParcelableExtra(EXTRA_NEW_TASK);
                Task old_task = intent.getParcelableExtra(EXTRA_OLD_TASK);
                handleTaskWrite(new_task, old_task);
            } else if (ACTION_TASK_DELETE.equals(action)) {
                Task remove_task = intent.getParcelableExtra(EXTRA_DELETE_TASK);
                handleTaskDelete(remove_task);
            }
        }
    }

    private void handleAllTasksFetch(String access_id, boolean family) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference(Task.COL_TAG)
                .orderByChild(Task.ACCESS_ID)
                .equalTo(access_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        finishAllTasksFetch(dataSnapshot, family);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void finishAllTasksFetch(DataSnapshot query, boolean family) {
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

        int result_code;
        if (family) {
            result_code = FAMILY_TASK_SERVICE_RESULT_CODE;
        } else {
            result_code = PERSONAL_TASK_SERVICE_RESULT_CODE;
        }

        Bundle data = new Bundle();
        data.putParcelableArray(Task.COL_TAG, task_arr);
        mResults.send(result_code, data);
        stopSelf();
    }

    private void handleTaskWrite(Task new_task, Task old_task) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        Map<String, Object> updates = new HashMap<>();

        if (old_task == null) {
            // New task, so auto-gen the id.
            String task_id = db.getReference(Task.COL_TAG).push().getKey();
            new_task.setId(task_id);

            String task_path = makeFirebasePath(Task.COL_TAG, task_id);
            updates.put(task_path, new_task.toMap());

            // Update the tags
            for (String tag_id : new_task.getTag_ids().keySet()) {
                String tag_path = makeFirebasePath(Tag.COL_TAG, tag_id, Tag.TASKS_ID, new_task.getId());
                updates.put(tag_path, new_task.getName());
            }
        } else if ((new_task.isComplete() && !old_task.isComplete()) || (!new_task.isComplete() && old_task.isComplete())) {
            // This checks if the complete values are different. If a task is checked, that is the only change that needs to be made.
            String comp_path = makeFirebasePath(Task.COL_TAG, new_task.getId(), Task.COMP_ID);
            updates.put(comp_path, new_task.isComplete());
        } else {
            // Compare the values, and update them.
            String task_path = makeFirebasePath(Task.COL_TAG, new_task.getId());
            updates.put(task_path, new_task.toMap());

            // Will keep a list of all tags, in both new and old. True is add and false is remove.
            // Tags that are in both won't be added.
            Map<String, Boolean> tag_changes = new HashMap<>();

            String[] new_task_tags = new String[0];
            new_task_tags = new_task.getTag_ids().keySet().toArray(new_task_tags);

            String[] old_task_tags = new String[0];
            old_task_tags = old_task.getTag_ids().keySet().toArray(old_task_tags);

            // Add all of them for now.
            for (String curr : new_task_tags) {
                tag_changes.put(curr, true);
            }

            for (String curr : old_task_tags) {
                // If it already exists, it doesn't need to be updated. Remove from the list.
                if (tag_changes.containsKey(curr)) {
                    tag_changes.remove(curr);
                } else {
                    tag_changes.put(curr, false);
                }
            }

            for (String tag_change : tag_changes.keySet()) {
                String tag_path = makeFirebasePath(Tag.COL_TAG, tag_change, Tag.TASKS_ID, new_task.getId());
                updates.put(tag_path, tag_changes.get(tag_change) ? true : null);
            }
        }

        db.getReference()
                .updateChildren(updates)
                .addOnSuccessListener(v -> finishTaskWrite(new_task, old_task));
    }

    private void finishTaskWrite(Task new_task, Task old_task) {
        Bundle data = new Bundle();
        data.putParcelable(EXTRA_NEW_TASK, new_task);
        data.putParcelable(EXTRA_OLD_TASK, old_task);

        mResults.send(WRITE_TASK_SERVICE_RESULT_CODE, data);
    }

    private void handleTaskDelete(Task delete_task) {

        Map<String, Object> updates = new HashMap<>();

        String task_path = makeFirebasePath(Task.COL_TAG, delete_task.getId());
        updates.put(task_path, null);

        for (String tag_id : delete_task.getTag_ids().keySet()) {
            String tag_path = makeFirebasePath(Tag.COL_TAG, tag_id, Tag.TASKS_ID, delete_task.getId());
            updates.put(tag_path, null);
        }

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference()
                .updateChildren(updates)
                .addOnSuccessListener(v -> finishTaskDelete(delete_task));
    }

    private void finishTaskDelete(Task deleted_task) {
        Bundle data = new Bundle();
        data.putParcelable(Task.DOC_TAG, deleted_task);

        mResults.send(DELETE_TASK_SERVICE_RESULT_CODE, data);
        stopSelf();
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
