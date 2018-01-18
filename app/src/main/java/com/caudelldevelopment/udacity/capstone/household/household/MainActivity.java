package com.caudelldevelopment.udacity.capstone.household.household;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener, TaskListsFragment.OnListsFragmentListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TaskListsFragment mListFragment;
    private FloatingActionButton mAddTaskBtn;

    private LinkedList<Task> mock_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddTaskBtn = findViewById(R.id.main_add_task);
        mAddTaskBtn.setOnClickListener(this);

        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.main_task_lists);
        Log.v(LOG_TAG, "onCreate - task lists frag is null: " + (frag == null));

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            Log.v(LOG_TAG, "onCreate - Firebase user display name: " + user.getDisplayName() + ", " + user.getUid());
//        } else {
//            Log.w(LOG_TAG, "onCreate - Firebase user is nul!!!!!");
//        }

        Log.v(LOG_TAG, "onCreate - end of onCreate");
    }

    @Override
    public void onListsFragAttach() {
        mListFragment = (TaskListsFragment) getSupportFragmentManager().findFragmentById(R.id.main_task_lists);
        Log.v(LOG_TAG, "onListsFragAttach - lists fragment == null: " + (mListFragment == null));
        if (mListFragment != null) {

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
            Snackbar.make(mAddTaskBtn, "onAddTask in " + tab, Snackbar.LENGTH_SHORT).show();
        } else {
            Log.w(LOG_TAG, "onAddTask, but task title could not be retrieved.");
        }
    }
}
