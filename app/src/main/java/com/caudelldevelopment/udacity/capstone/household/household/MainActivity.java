package com.caudelldevelopment.udacity.capstone.household.household;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.caudelldevelopment.udacity.capstone.household.household.data.Task;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener, TaskListsFragment.OnListsFragmentListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private FloatingActionButton mAddTaskBtn;

    private LinkedList<Task> mock_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddTaskBtn = findViewById(R.id.main_add_task);
        mAddTaskBtn.setOnClickListener(this);

        initMockData();
    }

    private void initMockData() {
        mock_data = new LinkedList<>();

        Task task1 = new Task();
        task1.setTitle("Laundry");
        task1.setDate("11-02-1994");
        task1.setDesc("Do laundry");
        task1.addTag_id("Chores");
        task1.addTag_id("Clothes");
        task1.setUser_id("1001");
        mock_data.add(task1);

        Task task2 = new Task();
        task2.setTitle("Cat Litter");
        task2.setDate("12-23-2006");
        task2.setDesc("Change litter");
        task2.addTag_id("Chores");
        task2.addTag_id("Cats");
        task2.setUser_id("1001");
        mock_data.add(task2);

        Task task3 = new Task();
        task3.setTitle("Clean Bathroom");
        task3.setDate("03-13-2007");
        task3.setDesc("Clean toilet and mirror");
        task3.addTag_id("Chores");
        task3.addTag_id("Bathroom");
        task3.setUser_id("F2102");
        task3.setFamily(true);
        mock_data.add(task3);
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
