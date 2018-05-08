package com.caudelldevelopment.udacity.capstone.household.household;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.caudelldevelopment.udacity.capstone.household.household.service.FamilyIntentService;
import com.caudelldevelopment.udacity.capstone.household.household.service.MyResultReceiver;
import com.caudelldevelopment.udacity.capstone.household.household.service.TagIntentService;
import com.caudelldevelopment.udacity.capstone.household.household.service.TaskIntentService;
import com.caudelldevelopment.udacity.capstone.household.household.service.UserIntentService;
import com.caudelldevelopment.udacity.capstone.household.household.widget.TasksWidget;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TaskListsFragment
        extends Fragment
        implements PersonalListFragment.OnPersonalFragListener,
                    FamilyListFragment.OnFamilyFragListener,
                    SelectFamilyFrag.OnSelectFamilyListener,
                    MyResultReceiver.Receiver {

    private static final String LOG_TAG = TaskListsFragment.class.getSimpleName();

    private MyResultReceiver mReceiver;

    private User mUser;
    private Family mFamily;
    private boolean mNoFamily;

    private List<Task> mPersonalTasks;
    private List<Task> mFamilyTasks;
    private List<Family> mFamiliesList;
    private List<Tag> mTagsList;

    private OnListsFragmentListener mListener;
    private TaskListsPagerAdapter mTaskAdapter;
    private TabLayout mTabLayout;

    private PersonalListFragment mPersonalFrag;
    private FamilyListFragment   mFamilyFrag;
    private SelectFamilyFrag     mSelectFrag;

    public TaskListsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            User temp = savedInstanceState.getParcelable(User.DOC_TAG);
            if (temp != null) {
                mUser = temp;
            }

            Parcelable[] temp_arr = savedInstanceState.getParcelableArray("all_tags");
            if (temp_arr != null && temp_arr.length > 0) {
                Tag[] tag_arr = Arrays.copyOf(temp_arr, temp_arr.length, Tag[].class);
                mTagsList = new LinkedList<>(Arrays.asList(tag_arr));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_lists, container, false);

        FragmentManager fragmentManager = getFragmentManager();
        mTaskAdapter = new TaskListsPagerAdapter(fragmentManager);

        ViewPager mListsPager = rootView.findViewById(R.id.main_view_pager);
        mTabLayout  = rootView.findViewById(R.id.main_tab_layout);

        mListsPager.setAdapter(mTaskAdapter);
        mTabLayout.setupWithViewPager(mListsPager);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mListener.updateFabDesc();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        return rootView;
    }

    @SuppressWarnings("all")
    public String getSelectedTab() {
        return mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).getText().toString();
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListsFragmentListener) {
            mListener = (OnListsFragmentListener) context;
            mUser = mListener.getUser();
            mFamily = mListener.getFamily();
            mNoFamily = (mFamily == null);
            mListener.onListsFragAttach();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListsFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(User.DOC_TAG, mUser);

//        if (mTagsList != null) {
//            Tag[] tag_arr = new Tag[mTagsList.size()];
//            tag_arr = mTagsList.toArray(tag_arr);
//            outState.putParcelableArray("all_tags", tag_arr);
//        }

        super.onSaveInstanceState(outState);
    }

    // Would this be better in a WorkerThread or AsyncTask? Is this thread safe?
    public void addNewTask(Task task) {
//        WriteBatch batch = mDatabase.batch();
//
//        DocumentReference taskRef;
//        if (task.getId() == null || task.getId().isEmpty()) {
//            taskRef = mDatabase.collection(Task.COL_TAG).document();
//            task.setId(taskRef.getId());
//            batch.set(taskRef, task.toMap());
//        } else {
//            taskRef = mDatabase.collection(Task.COL_TAG).document(task.getId());
//            batch.update(taskRef, task.toMap());
//        }
//
//        // Check each tag, and add or remove the id's that are missing.
//        if (mTagsList != null) {
//            for (int i = 0; i < mTagsList.size(); i++) {
//                Tag curr_tag = mTagsList.get(i);
//
//                boolean tag_has_task = curr_tag.getTask_ids().containsKey(task.getId());
//                boolean task_has_tag = task.getTag_ids().containsKey(curr_tag.getId());
//
//                // If tag does not have it but the task does, it's a new tag. Add it to the task.
//                if (!tag_has_task && task_has_tag) {
//                    curr_tag.addTask(task.getId(), task.getName());
//                }
//
//                // If tag has the task id, but the task does not, it has been removed.
//                if (tag_has_task && !task_has_tag) {
//                    curr_tag.removeTask(task.getId());
//                }
//
//                // Submit updated tag to the batch operation.
//                DocumentReference tagRef = mDatabase.collection(Tag.COL_TAG).document(curr_tag.getId());
//                batch.update(tagRef, Tag.TASKS_ID, curr_tag.getTask_ids());
//            }
//        }
//
//        batch.commit()
//                .addOnSuccessListener(v -> onAddNewTaskComplete())
//                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void addNewTask(Task task, boolean accessChange) {
        // If the task has changed between personal or family, we need to remove it from the original list
        if (accessChange) {
            // If it's currently family, it used to be personal and vice versa.
            if (task.isFamily()) {
                mPersonalTasks.remove(task);
            } else {
                mFamilyTasks.remove(task);
            }
        }

        addNewTask(task);
    }

    private void onAddNewTaskComplete() {
        // Just show the snackbar. The snapshot listener will trigger and update/add the task.
        mListener.doSnackbar(R.string.new_task_comp_msg);
    }

    public void setPersonalTasks(List<Task> personalTasks) {
        mPersonalTasks = personalTasks;
        updatePersonalTasks();
    }

    private void updatePersonalTasks() {
        if (mPersonalFrag != null) {
            mPersonalFrag.setData(mPersonalTasks);
            updateWidget(true);
        }
    }

    public void setFamilyTasks(List<Task> familyTasks) {
        mFamilyTasks = familyTasks;
        updateFamilyTasks();
    }

    private void updateFamilyTasks() {
        if (mFamilyFrag != null) {
            mFamilyFrag.setData(mFamilyTasks);
            updateWidget(false);
        }
    }

    private void updateWidget(boolean personal) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        intent.putExtra(TasksWidget.IS_PERSONAL, personal);
        if (getContext() != null) {
            getContext().sendBroadcast(intent);
        }
    }

    @Override
    public void onPersonalTaskClick(Task task, int pos) {
        mListener.onTaskClick(task, getString(R.string.personal_title));
    }

    @Override
    public void onFamilyTaskClick(Task task, int pos) {
        mListener.onTaskClick(task, getString(R.string.family_title));
    }

    @Override
    public void onPersonalTaskCheckClick(Task task, int pos) {
//        mDatabase.collection(Task.COL_TAG)
//                .document(task.getId())
//                .update(Task.COMP_ID, task.isComplete());
    }

    @Override
    public void onFamilyTaskCheckClick(Task task, int pos) {
//        mDatabase.collection(Task.COL_TAG)
//                .document(task.getId())
//                .update(Task.COMP_ID, task.isComplete());
    }

    public void onFamilyLeft() {
        mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        // Represents the user before leaving the family.
        User old_user = new User();
        old_user.setId(mUser.getId());
        old_user.setName(mUser.getName());
        old_user.setFamily(mUser.getId());

        // Update user
        mUser.setFamily("");
        mFamily = null;

        mFamilyTasks = null;

        UserIntentService.startUserWrite(getContext(), mReceiver, mUser, old_user);
    }

    public void onFamilyEntered(String name) {
        Family family = new Family(name, mUser);
        saveSelectedFamily(family, true);
    }

    public void updateFamiliesList(List<Family> families) {
        if (mNoFamily && mSelectFrag != null) {
            mFamiliesList = families;
            mSelectFrag.setData(families);
        }
    }

    @Override
    public void onFamilyItemClick(Family family) {
        saveSelectedFamily(family, false);
    }

    private void saveSelectedFamily(Family family, boolean isNewFamily) {
        mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        if (isNewFamily) {
            // This will also update the user family id field.
            FamilyIntentService.startFamilyWrite(getContext(), mReceiver, family);
        } else {
            // Update mUser with family id.
            mUser.setFamily(family.getId());

            // User just joined a family, so make a user without a family to represent it before the change.
            User old_user = new User();
            old_user.setId(mUser.getId());
            old_user.setName(mUser.getName());
            old_user.setFamily("");

            UserIntentService.startUserWrite(getContext(), mReceiver, mUser, old_user);
        }
    }

//    private void notifyFamilyUpdate() {
//        mNoFamily = false;
//        mListener.onFamilyChange(mFamily);
//        mListener.updateFabDesc();
//        mTaskAdapter.notifyDataSetChanged();
//        startFamilyQuery();
//    }

//    private void notifyFamiliesUpdate() {
//        mFamily = null;
//        mNoFamily = true;
//        mListener.onFamilyChange(null);
//        mListener.updateFabDesc();
//        mTaskAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case UserIntentService.USER_SERVICE_RESULT_CODE:
                User temp_user = resultData.getParcelable(User.DOC_TAG);

                if (temp_user != null) {
                    mUser = temp_user;
                    mNoFamily = !mUser.hasFamily();

                    // If user shows as having a family but mFamily is null, we need to fetch it.
                    if (!mNoFamily && mFamily == null) {
                        mFamilyTasks = null;

                        if (mReceiver == null) {
                            mReceiver = new MyResultReceiver(new Handler());
                            mReceiver.setReceiver(this);
                        }

                        FamilyIntentService.startFamilyFetch(getContext(), mReceiver, mUser.getFamily());
                    } else {
                        mListener.onFamilyChange(mFamily);
                    }
                }
                break;
            case FamilyIntentService.FAMILY_SERVICE_RESULT_CODE:
                Family temp_family = resultData.getParcelable(Family.DOC_TAG);

                if (temp_family != null) {
                    mFamily = temp_family;
                    mListener.onFamilyChange(mFamily);
                    TaskIntentService.startAllTasksFetch(getContext(), mReceiver, mUser.getFamily());
                }
                break;
            case TaskIntentService.PERSONAL_TASK_SERVICE_RESULT_CODE:
                Parcelable[] temp_arr = resultData.getParcelableArray(Task.COL_TAG);

                if (temp_arr != null) {
                    Task[] pers_arr = Arrays.copyOf(temp_arr, temp_arr.length, Task[].class);
                    List<Task> pers_list = new LinkedList<>(Arrays.asList(pers_arr));
                    setPersonalTasks(pers_list);
                }
                break;
            case TaskIntentService.FAMILY_TASK_SERVICE_RESULT_CODE:
                temp_arr = resultData.getParcelableArray(Task.COL_TAG);

                if (temp_arr != null) {
                    Task[] fam_arr = Arrays.copyOf(temp_arr, temp_arr.length, Task[].class);
                    List<Task> fam_list = new LinkedList<>(Arrays.asList(fam_arr));
                    setFamilyTasks(fam_list);
                }
                break;
            case TagIntentService.TAG_SERVICE_RESULT_CODE:
                temp_arr = resultData.getParcelableArray(Tag.COL_TAG);

                List<Tag> tags_list = new LinkedList<>();
                if (temp_arr != null) {
                    for (Parcelable curr : temp_arr) {
                        Tag curr_tag = (Tag) curr;
                        tags_list.add(curr_tag);
                    }

                    mTagsList = tags_list;
                }

                break;
        }
    }

    public interface OnListsFragmentListener {
        void onListsFragAttach();
        void onTaskClick(Task task, String tab);
        User getUser();
        Family getFamily();
        void onFamilyChange(@Nullable Family family);
        void updateFabDesc();
        void doSnackbar(int str_res);
    }


    // ####---- View Pager ----####

    private class TaskListsPagerAdapter extends FragmentStatePagerAdapter {

        TaskListsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment result = null;

            if (position == 0) {
                if (mPersonalFrag == null) {
                    result = PersonalListFragment.newInstance(mPersonalTasks);
                    mPersonalFrag = (PersonalListFragment) result;
                } else {
                    mPersonalFrag.setData(mPersonalTasks);
                    result = mPersonalFrag;
                }
            } else if (position == 1) {
                if (mNoFamily) {
                    if (mSelectFrag == null) {
                        result = SelectFamilyFrag.newInstance(mFamiliesList);
                        mSelectFrag = (SelectFamilyFrag) result;
                    } else {
                        mSelectFrag.setData(mFamiliesList);
                        result = mSelectFrag;
                    }
                } else {
                    if (mFamilyFrag == null) {
                        result = FamilyListFragment.newInstance(mFamilyTasks);
                        mFamilyFrag = (FamilyListFragment) result;
                    } else {
                        mFamilyFrag.setData(mFamilyTasks);
                        result = mFamilyFrag;
                    }
                }
            }

            return result;
        }

        @Override
        public int getItemPosition(Object object) {
            // If the object is still of SelectFamilyFrag and user now has a family, then we need to change the fragment.
            // POSITION_NONE should call getItem again and will return a FamilyListFragment
            if (object instanceof SelectFamilyFrag && !mNoFamily) {
                return POSITION_NONE;
            }

            if (object instanceof FamilyListFragment && mNoFamily) {
                return POSITION_NONE;
            }

            return POSITION_UNCHANGED;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String result = null;

            switch(position) {
                case 0:
                    result = getString(R.string.personal_title);
                    break;
                case 1:
                    result = getString(R.string.family_title);
                    break;
            }

            return result;
        }
    }
}
