package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.annotation.Nullable;
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
import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener,
                                     TaskListsFragment.OnListsFragmentListener,
                                     NewTaskDialogFrag.NewTaskDialogListener,
                                     BaseEntryDialog.EntryDialogListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String USER_SAVE_STATE = "user_save_state_key";
    private static final String FAMILY_SAVE_STATE = "family_save_state_key";

    public static final int FAMILY_REQ_CODE = 100;
    public static final int SETTING_REQ_CODE = 200;
    public static final String ON_UP_CLICKED = "on_up_button_clicked_key";

    private TaskListsFragment mListFragment;
    private FloatingActionButton mAddTaskBtn;
    private boolean wide_layout;
    private boolean isConnected;

    private User mUser;
    private Family mFamily;
    private boolean mNoFamily;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mUser = savedInstanceState.getParcelable(USER_SAVE_STATE);
            mFamily = savedInstanceState.getParcelable(FAMILY_SAVE_STATE);
            mNoFamily = (mFamily == null);
        } else {
            Parcelable[] user_data = getIntent().getParcelableArrayExtra("user_data");
            if (user_data != null) {
                mUser = (User) user_data[0];
                mFamily = (Family) user_data[1];
            }

            mNoFamily = (mFamily == null);

            if (!mNoFamily) {
                for (String member : mFamily.getMembers()) Log.v(LOG_TAG, "onCreate - family member: " + member);
            }
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAddTaskBtn = findViewById(R.id.main_add_task);
        mAddTaskBtn.setOnClickListener(this);

        View view_holder = findViewById(R.id.main_view_holder);
        wide_layout = (view_holder != null);

        NetworkReceiver receiver = new NetworkReceiver();
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, networkFilter);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(USER_SAVE_STATE, mUser);
        outState.putParcelable(FAMILY_SAVE_STATE, mFamily);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isConnected) {
            doSnackbar(R.string.no_network_menu_msg);
            return super.onOptionsItemSelected(item);
        }

        switch(item.getItemId()) {
            case R.id.menu_family:
                if (mNoFamily) {
                    doSnackbar(R.string.no_family_snackbar);
                } else {
                    Intent family = new Intent(this, FamilyActivity.class);
                    family.putExtra(FamilyActivity.USER_EXTRA, mUser);
//                    startActivity(family);
                    startActivityForResult(family, FAMILY_REQ_CODE);
                }
                return true;
            case R.id.menu_tags:
                Intent tags = new Intent(this, TagsActivity.class);
                List<Tag> all_tags = mListFragment.getAllTags();

                if (all_tags != null) {
                    Tag[] tags_arr = new Tag[all_tags.size()];
                    tags_arr = all_tags.toArray(tags_arr);

                    Parcelable[] temp_arr = Arrays.copyOf(tags_arr, tags_arr.length, Parcelable[].class);
                    tags.putExtra("all_tags", temp_arr);

                    startActivity(tags);
                }
                return true;
            case R.id.menu_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivityForResult(settings, SETTING_REQ_CODE);
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
        } else if (requestCode == SETTING_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                boolean sign_out_clicked = data.getBooleanExtra(SettingsActivity.SIGN_OUT_CLICKED, false);

                if (sign_out_clicked) {
                    Intent sign_out_data = new Intent();
                    sign_out_data.putExtra(SettingsActivity.SIGN_OUT_CLICKED, true);
                    setResult(RESULT_OK, sign_out_data);
                    finish();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        Intent on_up_pressed_data = new Intent();
        on_up_pressed_data.putExtra(ON_UP_CLICKED, true);
        setResult(RESULT_OK, on_up_pressed_data);
        finish();
    }

    @Override
    public void onListsFragAttach() {
        mListFragment = (TaskListsFragment) getSupportFragmentManager().findFragmentById(R.id.main_task_lists);
    }

    @Override
    public User getUser() {
        return mUser;
    }

    @Override
    @Nullable
    public Family getFamily() {
        return mFamily;
    }

    @Override
    public void onFamilyChange(Family family) {
        mFamily = family;
        mNoFamily = (mFamily == null);
    }

    @Override
    public void onClick(View view) {
        if (!isConnected) {
            doSnackbar(R.string.disconnected_click_msg);
            return;
        }

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
            NewTaskDialogFrag dialog =  NewTaskDialogFrag.newInstance(false, mListFragment.getAllTags(), mUser, null);
            FragmentManager manager = getSupportFragmentManager();

            manager.beginTransaction()
                    .replace(R.id.main_view_holder, dialog, NewTaskDialogFrag.DIALOG_TAG)
                    .commit();
        } else {

            if (tab != null) {
                boolean family = tab.equals(getString(R.string.family_title));
                NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mListFragment.getAllTags(), mUser, null);
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
                NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mListFragment.getAllTags(), mUser, task);
                FragmentManager manager = getSupportFragmentManager();

                manager.beginTransaction()
                        .replace(R.id.main_view_holder, dialog, NewTaskDialogFrag.DIALOG_TAG)
                        .commit();
            } else {
                boolean family = tab.equals("Family");
                NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mListFragment.getAllTags(), mUser, task);
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

    public void updateNetworkStatus() {
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            doSnackbar(R.string.main_not_connected_msg);
        }
    }

    // This is just to make sure Talk Back will be clear and stay updated when the app changes.
    @Override
    public void updateFabDesc() {
        String tab = mListFragment.getSelectedTab();
        if (tab.equals(getString(R.string.personal_title))) {
            mAddTaskBtn.setContentDescription(getString(R.string.pers_fab_desc));
        } else if (tab.equals(getString(R.string.family_title))) {
            if (mNoFamily) {
                mAddTaskBtn.setContentDescription(getString(R.string.fam_fab_desc));
            } else {
                mAddTaskBtn.setContentDescription(getString(R.string.new_fam_fab_desc));
            }
        }
    }

    @Override
    public void doSnackbar(int message) {
        Snackbar.make(findViewById(R.id.appBarLayout), message, Snackbar.LENGTH_LONG).show();
    }

    protected class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    Log.v(LOG_TAG, "NetworkReceiver.onReceive: Network is connected!!!");

                    isConnected = true;
                    updateNetworkStatus();
                } else {
                    Log.v(LOG_TAG, "NetworkReceiver.onReceive:   ----!!!!!----   Network is not connected!!!!!!");

                    isConnected = false;
                    updateNetworkStatus();
                }
            }
        }
    }
}
