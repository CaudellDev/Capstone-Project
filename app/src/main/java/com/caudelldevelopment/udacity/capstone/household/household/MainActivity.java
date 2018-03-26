package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener,
                                     TaskListsFragment.OnListsFragmentListener,
                                     NewTaskDialogFrag.NewTaskDialogListener,
                                     BaseEntryDialog.EntryDialogListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int FAMILY_REQ_CODE = 100;

    private TaskListsFragment mListFragment;
    private FloatingActionButton mAddTaskBtn;
    private boolean wide_layout;

    private User mUser;
    private Family mFamily;
    private boolean mNoFamily;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mUser = intent.getParcelableExtra(User.DOC_TAG);
        Log.v(LOG_TAG, "onCreate - mUser == null: " + (mUser == null));
        if (mUser != null) Log.v(LOG_TAG, "onCreate - mUser.name: " + mUser.getName() + ", id: " + mUser.getId());
        mNoFamily = (mUser.getFamily() == null) || (mUser.getFamily().isEmpty());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAddTaskBtn = findViewById(R.id.main_add_task);
        mAddTaskBtn.setOnClickListener(this);

        View view_holder = findViewById(R.id.main_view_holder);
        wide_layout = (view_holder != null);
        Log.v(LOG_TAG, "onCreate - Is the sw600dp layout being used: " + wide_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.menu_family:
                if (mNoFamily) {
                    Snackbar.make(findViewById(R.id.appBarLayout), R.string.no_family_snackbar, Snackbar.LENGTH_SHORT).show();
                } else {
                    Intent family = new Intent(this, FamilyActivity.class);
                    family.putExtra(FamilyActivity.USER_EXTRA, mUser);
//                    startActivity(family);
                    startActivityForResult(family, FAMILY_REQ_CODE);
                }
                return true;
            case R.id.menu_tags:
                Intent tags = new Intent(this, TagsActivity.class);
                startActivity(tags);
                return true;
            case R.id.menu_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FAMILY_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra(FamilyActivity.LEFT_FAMILY)) {
                    boolean left_fam = data.getBooleanExtra(FamilyActivity.LEFT_FAMILY, false);

                    if (left_fam) {
                        mListFragment.onFamilyLeft();
                    }
                }
            }
        }
    }

    @Override
    public void onListsFragAttach() {
        mListFragment = (TaskListsFragment) getSupportFragmentManager().findFragmentById(R.id.main_task_lists);
        Log.v(LOG_TAG, "onListsFragAttach - lists fragment == null: " + (mListFragment == null));
        if (mListFragment != null) {
//            mListFragment.setUser(mUser);
        }
    }

    @Override
    public User getUser() {
        return mUser;
    }

    @Override
    public void onClick(View view) {
        // Get the selected tab or fragment from the TaskListsFragment and display the dialog
        String tab = mListFragment.getSelectedTab();

        if (tab.equals(getString(R.string.family_title)) && mNoFamily) {
            if (wide_layout) {
                BaseEntryDialog dialog = BaseEntryDialog.getInstance(BaseEntryDialog.ENTRY_FAMILY);
                FragmentManager manager = getSupportFragmentManager();

                manager.beginTransaction()
                        .replace(R.id.main_view_holder, dialog, BaseEntryDialog.DIALOG_TAG)
                        .commit();
            } else {
                BaseEntryDialog dialog = BaseEntryDialog.getInstance(BaseEntryDialog.ENTRY_FAMILY);
                dialog.show(getSupportFragmentManager(), BaseEntryDialog.DIALOG_TAG);
            }

            return;
        }

        if (wide_layout) {
            NewTaskDialogFrag dialog =  NewTaskDialogFrag.newInstance(false, mUser, null);
            FragmentManager manager = getSupportFragmentManager();

            manager.beginTransaction()
                    .replace(R.id.main_view_holder, dialog, NewTaskDialogFrag.DIALOG_TAG)
                    .commit();
        } else {

            if (tab != null) {
                boolean family = tab.equals(getString(R.string.family_title));
                NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mUser, null);
                dialog.show(getSupportFragmentManager(), NewTaskDialogFrag.DIALOG_TAG);
            } else {
                Log.w(LOG_TAG, "onClick, but tab title could not be retrieved.");
            }
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
                boolean family = tab.equals("Family");
                NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mUser, task);
                FragmentManager manager = getSupportFragmentManager();

                manager.beginTransaction()
                        .replace(R.id.main_view_holder, dialog, NewTaskDialogFrag.DIALOG_TAG)
                        .commit();
            } else {
                boolean family = tab.equals("Family");
                NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mUser, task);
                dialog.show(getSupportFragmentManager(), NewTaskDialogFrag.DIALOG_TAG);
            }
        } else {
            Log.w(LOG_TAG, "onTaskClick, but tab title or edit task could not be retrieved.");
        }
    }

    @Override
    public void onDialogPositiveClick(Task task) {
        NewTaskDialogFrag dialog = (NewTaskDialogFrag) getSupportFragmentManager().findFragmentByTag(NewTaskDialogFrag.DIALOG_TAG);
        Log.v(LOG_TAG, "onDialogPositiveClick - dialog == null: " + (dialog == null ) + ((dialog == null) ? "" : ", " + dialog.isAccessIdDiff()));

        if (dialog != null) {
            mListFragment.addNewTask(task, dialog.isAccessIdDiff());
        } else {
            mListFragment.addNewTask(task);
        }
    }

    @Override
    public void onDialogNegativeClick() {
        if (wide_layout) {
            FragmentManager manager = getSupportFragmentManager();
            NewTaskDialogFrag dialog = (NewTaskDialogFrag) manager.findFragmentByTag(NewTaskDialogFrag.DIALOG_TAG);

            if (dialog != null) {
                ViewGroup container = findViewById(R.id.main_view_holder);

                Animation animation = AnimationUtils.loadAnimation(this, R.anim.right_out);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        manager.beginTransaction()
                                .remove(dialog)
                                .commit();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                container.startAnimation(animation);
                container.setVisibility(View.GONE);
            }
        }
    }

    /*
     * Wait until the @NewTaskDialogFrag has finished loading Firebase info.
     */
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

    @Override
    public void onEntrySave(String name) {
        mListFragment.onFamilyEntered(name);
    }

    @Override
    public void doSnackbar(int message) {
        Snackbar.make(findViewById(R.id.appBarLayout), message, Snackbar.LENGTH_LONG).show();
    }
}
