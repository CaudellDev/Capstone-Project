package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener,
                                     TaskListsFragment.OnListsFragmentListener,
                                     NewTaskDialogFrag.NewTaskDialogListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TaskListsFragment mListFragment;
    private FloatingActionButton mAddTaskBtn;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate has been started.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddTaskBtn = findViewById(R.id.main_add_task);
        mAddTaskBtn.setOnClickListener(this);

        Intent intent = getIntent();
        mUser = intent.getParcelableExtra(User.DOC_TAG);
    }

    @Override
    public void onListsFragAttach() {
        mListFragment = (TaskListsFragment) getSupportFragmentManager().findFragmentById(R.id.main_task_lists);
        Log.v(LOG_TAG, "onListsFragAttach - lists fragment == null: " + (mListFragment == null));
        if (mListFragment != null) {
            mListFragment.setUser(mUser);
        }
    }

    @Override
    public void onClick(View view) {
        // Open DialogFragment
        // Save Task Value
        // Add Task to Firebase
        // Update the Fragment

        // Get the selected tab or fragment from the TaskListsFragment and display snackbar
        FragmentManager fragmentManager = getSupportFragmentManager();
        TaskListsFragment rootFrag = (TaskListsFragment) fragmentManager.findFragmentById(R.id.main_task_lists);
        rootFrag.onAddTaskPressed();
    }

    @Override
    public void onAddTask(String tab) {
        if (tab != null) {
//            Snackbar.make(mAddTaskBtn, "onAddTask in " + tab, Snackbar.LENGTH_SHORT).show();
            boolean family = tab.equals("Family");
            NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mUser, null);
            dialog.show(getSupportFragmentManager(), "new_task_dialog");

        } else {
            Log.w(LOG_TAG, "onAddTask, but tab title could not be retrieved.");
        }
    }

    @Override
    public void onTaskClick(Task task, String tab) {
        if (tab != null && task != null) {
            boolean family = tab.equals("Family");
            NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mUser, task);
            dialog.show(getSupportFragmentManager(), "new_task_dialog");
        } else {
            Log.w(LOG_TAG, "onTaskClick, but tab title or edit task could not be retrieved.");
        }
    }

    @Override
    public void onDialogPositiveClick(Task task) {
        mListFragment.addNewTask(task);
    }

    @Override
    public void onDialogNegativeClick() {
        // Do nothing?
    }

    @Override
    public void deleteTask(Task task) {
        Log.v(LOG_TAG, "deleteTask - deleting task: " + task.getId() + ", " + task.getName());
    }
}
