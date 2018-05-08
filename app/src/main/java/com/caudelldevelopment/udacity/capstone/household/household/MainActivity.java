package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
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
import com.caudelldevelopment.udacity.capstone.household.household.service.FamilyIntentService;
import com.caudelldevelopment.udacity.capstone.household.household.service.MyResultReceiver;
import com.caudelldevelopment.udacity.capstone.household.household.service.TaskIntentService;
import com.caudelldevelopment.udacity.capstone.household.household.service.TagIntentService;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener,
                                     TaskListsFragment.OnListsFragmentListener,
                                     NewTaskDialogFrag.NewTaskDialogListener,
                                     BaseEntryDialog.EntryDialogListener,
                                     MyResultReceiver.Receiver {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String USER_SAVE_STATE = "user_save_state_key";
    private static final String FAMILY_SAVE_STATE = "family_save_state_key";

    public static final int FAMILY_REQ_CODE = 100;
    public static final int SETTING_REQ_CODE = 200;
    public static final String ON_UP_CLICKED = "on_up_button_clicked_key";

    private TaskListsFragment mListFragment;
    private FloatingActionButton mAddTaskBtn;
    private boolean wide_layout;

    private User mUser;
    private Family mFamily;
    private boolean mNoFamily;
    private List<Tag> mAllTags;

    private MyResultReceiver mPersonalTaskResults;
    private MyResultReceiver mFamilyTaskResults;
    private MyResultReceiver mAllTagsResults;
    private MyResultReceiver mFamilyResults;
    private ServiceConnection mConnection;

    private NetworkReceiver mNetworkRec;
    private boolean isConnected;

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
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAddTaskBtn = findViewById(R.id.main_add_task);
        mAddTaskBtn.setOnClickListener(this);

        View view_holder = findViewById(R.id.main_view_holder);
        wide_layout = (view_holder != null);

        mNetworkRec = new NetworkReceiver();

        doPersonalTaskFetch();

        if (mNoFamily) {
            doBindFamilies();
        } else {
            doFamilyTaskFetch();
        }
    }

    @Override
    protected void onDestroy() {
        unbindServices();
        undoResultReceivers();
        super.onDestroy();
    }

    private void unbindServices() {
        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    private void undoResultReceivers() {
        if (mFamilyResults != null) {
            mFamilyResults.setReceiver(null);
            mFamilyResults = null;
        }

        if (mAllTagsResults != null) {
            mAllTagsResults.setReceiver(null);
            mAllTagsResults = null;
        }

        if (mPersonalTaskResults != null) {
            mPersonalTaskResults.setReceiver(null);
            mPersonalTaskResults = null;
        }

        if (mFamilyTaskResults != null) {
            mFamilyTaskResults.setReceiver(null);
            mFamilyTaskResults = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkRec, networkFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mNetworkRec);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NewTaskDialogFrag dialog = (NewTaskDialogFrag) getSupportFragmentManager().findFragmentByTag(NewTaskDialogFrag.DIALOG_TAG);

        // If the layout is wide, but the dialog is showing an AlertDialog,
        // it must have been made in a smaller layout and rotated.
        if (dialog != null) {
            if (wide_layout && dialog.getShowsDialog()) {
                dialog.dismiss();
            } else if (!wide_layout && !dialog.getShowsDialog()) {
                getSupportFragmentManager().beginTransaction()
                                        .remove(dialog)
                                        .commit();
            }
        }

        BaseEntryDialog entry_dialog = (BaseEntryDialog) getSupportFragmentManager().findFragmentByTag(BaseEntryDialog.DIALOG_TAG);

        if (entry_dialog != null) {
            if (wide_layout && entry_dialog.getShowsDialog()) {
                entry_dialog.dismiss();
            } else if (!wide_layout && !entry_dialog.getShowsDialog()) {
                getSupportFragmentManager().beginTransaction()
                        .remove(entry_dialog)
                        .commit();
            }
        }
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
                    family.putExtra(FamilyActivity.FAMILY_EXTRA, mFamily);
                    startActivityForResult(family, FAMILY_REQ_CODE);
                }
                return true;
            case R.id.menu_tags:
                Intent tags = new Intent(this, TagsActivity.class);

                if (mAllTags != null) {
                    Tag[] tags_arr = new Tag[mAllTags.size()];
                    tags_arr = mAllTags.toArray(tags_arr);

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
        Intent on_up_pressed_data = new Intent();
        on_up_pressed_data.putExtra(ON_UP_CLICKED, true);
        setResult(RESULT_OK, on_up_pressed_data);
        finish();
    }

    @Override
    public void onListsFragAttach() {
        mListFragment = (TaskListsFragment) getSupportFragmentManager().findFragmentById(R.id.main_task_lists);
    }

    private void doBindFamilies() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.i(LOG_TAG, "onServiceConnected has started. Connection has been made!!!");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        mFamilyResults = new MyResultReceiver(new Handler());
        mFamilyResults.setReceiver(this);

        FamilyIntentService.bindFamilies(this, mFamilyResults, mConnection);
    }

    private void doPersonalTaskFetch() {
        if (mPersonalTaskResults == null) {
            mPersonalTaskResults = new MyResultReceiver(new Handler());
            mPersonalTaskResults.setReceiver(this);
        }

        TaskIntentService.startAllTasksFetch(this, mPersonalTaskResults, mUser.getId());
    }

    private void doFamilyTaskFetch() {
        if (mFamilyTaskResults == null) {
            mFamilyTaskResults = new MyResultReceiver(new Handler());
            mFamilyTaskResults.setReceiver(this);
        }

        TaskIntentService.startAllTasksFetch(this, mFamilyTaskResults, mUser.getFamily());
    }

    private void doAllTagsFetch() {
        mAllTagsResults = new MyResultReceiver(new Handler());
        mAllTagsResults.setReceiver(this);

        TagIntentService.startAllTagsFetch(this, mAllTagsResults);
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

    @Nullable
    @Override
    public List<Tag> getAllTags() {
        return mAllTags;
    }

    @Override
    public void onFamilyChange(Family family) {
        mFamily = family;
        mNoFamily = (mFamily == null);
        updateFabDesc();

        onEntryDialogClose();

        if (mNoFamily) {
            doBindFamilies();
        }
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
            NewTaskDialogFrag dialog =  NewTaskDialogFrag.newInstance(false, mAllTags, mUser, null);
            FragmentManager manager = getSupportFragmentManager();

            manager.beginTransaction()
                    .replace(R.id.main_view_holder, dialog, NewTaskDialogFrag.DIALOG_TAG)
                    .commit();
        } else {

            if (tab != null) {
                boolean family = tab.equals(getString(R.string.family_title));
                NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mAllTags, mUser, null);
                dialog.show(getSupportFragmentManager(), NewTaskDialogFrag.DIALOG_TAG);
            }
        }
    }

    @Override
    public void onTaskClick(Task task, String tab) {
        if (tab != null && task != null) {
            boolean family = tab.equals(getString(R.string.family_title));
            if (wide_layout) {
                NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mAllTags, mUser, task);
                FragmentManager manager = getSupportFragmentManager();

                manager.beginTransaction()
                        .replace(R.id.main_view_holder, dialog, NewTaskDialogFrag.DIALOG_TAG)
                        .commit();
            } else {
                NewTaskDialogFrag dialog = NewTaskDialogFrag.newInstance(family, mAllTags, mUser, task);
                dialog.show(getSupportFragmentManager(), NewTaskDialogFrag.DIALOG_TAG);
            }
        }
    }

    @Override
    public void onDialogPositiveClick(Task task) {
        NewTaskDialogFrag dialog = (NewTaskDialogFrag) getSupportFragmentManager().findFragmentByTag(NewTaskDialogFrag.DIALOG_TAG);

        if (dialog != null) {
            mListFragment.addNewTask(task, dialog.isAccessIdDiff());
        } else {
            mListFragment.addNewTask(task);
        }

        onNewTaskDialogClose();
    }

    @Override
    public void onNewTaskDialogClose() {
        if (wide_layout) {
            FragmentManager manager = getSupportFragmentManager();
            NewTaskDialogFrag dialog = (NewTaskDialogFrag) manager.findFragmentByTag(NewTaskDialogFrag.DIALOG_TAG);

            if (dialog != null) {
                ViewGroup container = findViewById(R.id.main_view_holder);

                Animation animation = AnimationUtils.loadAnimation(this, R.anim.right_out);
                animation.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        manager.beginTransaction()
                                .remove(dialog)
                                .commit();
                    }

                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                });

                container.startAnimation(animation);
                container.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onEntryDialogClose() {
        if (wide_layout) {
            FragmentManager manager = getSupportFragmentManager();
            BaseEntryDialog dialog = (BaseEntryDialog) manager.findFragmentByTag(BaseEntryDialog.DIALOG_TAG);

            if (dialog != null) {
                ViewGroup container = findViewById(R.id.main_view_holder);

                Animation animation = AnimationUtils.loadAnimation(this, R.anim.right_out);
                animation.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        manager.beginTransaction()
                                .remove(dialog)
                                .commit();
                    }

                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
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
    public void onEntrySave(String name) {
        mListFragment.onFamilyEntered(name);
        onEntryDialogClose();
    }

    public void updateNetworkStatus() {
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

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case TaskIntentService.PERSONAL_TASK_SERVICE_RESULT_CODE:
                Parcelable[] temp_arr = resultData.getParcelableArray(Task.COL_TAG);

                if (temp_arr != null) {
                    Task[] task_arr = Arrays.copyOf(temp_arr, temp_arr.length, Task[].class);
                    List<Task> task_list = new LinkedList<>(Arrays.asList(task_arr));
                    mListFragment.setPersonalTasks(task_list);
                }
                break;
            case TaskIntentService.FAMILY_TASK_SERVICE_RESULT_CODE:
                temp_arr = resultData.getParcelableArray(Task.COL_TAG);

                if (temp_arr != null) {
                    Task[] task_arr = Arrays.copyOf(temp_arr, temp_arr.length, Task[].class);
                    List<Task> task_list = new LinkedList<>(Arrays.asList(task_arr));
                    mListFragment.setFamilyTasks(task_list);
                }
                break;
            case FamilyIntentService.FAMILY_BIND_SERVICE_RESULT_CODE:
                temp_arr = resultData.getParcelableArray(Family.COL_TAG);

                if (temp_arr != null) {
                    Family[] fam_arr = Arrays.copyOf(temp_arr, temp_arr.length, Family[].class);
                    List<Family> fam_list = new LinkedList<>(Arrays.asList(fam_arr));
                    mListFragment.updateFamiliesList(fam_list);
                }

                break;
            case TagIntentService.TAG_SERVICE_RESULT_CODE:
                temp_arr = resultData.getParcelableArray(Tag.COL_TAG);

                if (temp_arr != null) {
                    Tag[] tag_arr = Arrays.copyOf(temp_arr, temp_arr.length, Tag[].class);
                    mAllTags = new LinkedList<>(Arrays.asList(tag_arr));
                }

                break;
        }
    }

    protected class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    isConnected = true;
                    updateNetworkStatus();
                } else {
                    isConnected = false;
                    updateNetworkStatus();
                }
            }
        }
    }
}
