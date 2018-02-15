package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;

public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener,
                                     TaskListsFragment.OnListsFragmentListener,
                                     NewTaskDialogFrag.NewTaskDialogListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TaskListsFragment mListFragment;
    private FloatingActionButton mAddTaskBtn;
    private boolean wide_layout;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddTaskBtn = findViewById(R.id.main_add_task);
        mAddTaskBtn.setOnClickListener(this);

        View view_holder = findViewById(R.id.main_view_holder);
        wide_layout = (view_holder != null);
        Log.v(LOG_TAG, "onCreate - Is the sw600dp layout being used: " + wide_layout);

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
        if (wide_layout) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            NewTaskDialogFrag dialog =  NewTaskDialogFrag.newInstance(false, mUser, null);

//            transaction.setCustomAnimations(R.anim.right_in, R.anim.right_out);
            transaction.replace(R.id.main_view_holder, dialog, "new_task_fragment");
            transaction.commit();

//            ViewGroup container = findViewById(R.id.main_view_holder);
//            container.setVisibility(View.VISIBLE);
//            container.startAnimation(AnimationUtils.loadAnimation(this, R.anim.right_in));
        } else {
            // Get the selected tab or fragment from the TaskListsFragment and display the dialog
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            TaskListsFragment rootFrag = (TaskListsFragment) fragmentManager.findFragmentById(R.id.main_task_lists);
            mListFragment.onAddTaskPressed();
        }
    }

    @Override
    public void onAddTask(String tab) {
        if (tab != null) {
            boolean family = tab.equals("Family");
            NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mUser, null);
            dialog.show(getSupportFragmentManager(), "new_task_dialog");
        } else {
            Log.w(LOG_TAG, "onAddTask, but tab title could not be retrieved.");
        }
    }

    @Override
    public void onAddTaskComplete() {
        Snackbar.make(findViewById(R.id.appBarLayout), "Task added succesfully.", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskClick(Task task, String tab) {
        if (tab != null && task != null) {
            if (wide_layout) {
                // TODO: Made a NewTaskDialogFrag and start a fragment transaction.
            } else {
                boolean family = tab.equals("Family");
                NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mUser, task);
                dialog.show(getSupportFragmentManager(), "new_task_dialog");
            }
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
        if (wide_layout) {
            FragmentManager manager = getSupportFragmentManager();
            NewTaskDialogFrag dialog = (NewTaskDialogFrag) manager.findFragmentByTag("new_task_fragment");

            if (dialog != null) {
                FragmentTransaction transaction = manager.beginTransaction();

                transaction.remove(dialog);
                transaction.commit();
            }
        }
    }

    @Override
    public void onFragmentReady() {
        if (wide_layout) {
            ViewGroup container = findViewById(R.id.main_view_holder);
            container.setVisibility(View.VISIBLE);
            container.startAnimation(AnimationUtils.loadAnimation(this, R.anim.right_in));
        }
    }

    @Override
    public void deleteTask(Task task) {
        Log.v(LOG_TAG, "deleteTask - deleting task: " + task.getId() + ", " + task.getName());
    }
}
