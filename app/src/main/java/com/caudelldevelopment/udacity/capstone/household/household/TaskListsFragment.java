package com.caudelldevelopment.udacity.capstone.household.household;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.caudelldevelopment.udacity.capstone.household.household.widget.FamilyWidgetRemoteViewsService;
import com.caudelldevelopment.udacity.capstone.household.household.widget.PersonalWidgetRemoteViewsService;
import com.caudelldevelopment.udacity.capstone.household.household.widget.TasksWidget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnListsFragmentListener} interface
 * to handle interaction events.
 */
public class TaskListsFragment extends Fragment
                                implements PersonalListFragment.OnPersonalFragListener,
                                            FamilyListFragment.OnFamilyFragListener,
                                            SelectFamilyFrag.OnSelectFamilyListener,
                                            OnCompleteListener<Void>, OnFailureListener {

    private static final String LOG_TAG = TaskListsFragment.class.getSimpleName();

    private static final String TASK_LIST = "TASK_LIST";

    private FirebaseFirestore mDatabase;
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
            User temp = savedInstanceState.getParcelable("user");
            if (temp != null) {
                mUser = temp;
            }

            Parcelable[] temp_arr = savedInstanceState.getParcelableArray("all_tags");

            if (temp_arr != null && temp_arr.length > 0) {
                Tag[] tag_arr = Arrays.copyOf(temp_arr, temp_arr.length, Tag[].class);
                mTagsList = new LinkedList<>(Arrays.asList(tag_arr));
            }
        }

        mDatabase = FirebaseFirestore.getInstance();

        startTagsQuery();
        startPersonalQuery();
        startFamiliesQuery();

        if (!mNoFamily) startFamilyQuery();
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
        Log.v(LOG_TAG, "onAttach has started!!!");
        super.onAttach(context);

        if (context instanceof OnListsFragmentListener) {
            mListener = (OnListsFragmentListener) context;
            mUser = mListener.getUser();
            mFamily = mListener.getFamily();
            mNoFamily = (mFamily == null);
            mListener.onListsFragAttach();
            Log.v(LOG_TAG, "onAttach - global variables have been set. User == null: " + (mUser == null) + ", Family == null: " + (mFamily == null));
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
        outState.putParcelable("user", mUser);

        Tag[] tag_arr = new Tag[mTagsList.size()];
        tag_arr = mTagsList.toArray(tag_arr);
        outState.putParcelableArray("all_tags", tag_arr);

        super.onSaveInstanceState(outState);
    }

    // Would this be better in a WorkerThread or AsyncTask? Is this thread safe?
    public void addNewTask(Task task) {
        WriteBatch batch = mDatabase.batch();

        DocumentReference taskRef;
        if (task.getId() == null || task.getId().isEmpty()) {
            taskRef = mDatabase.collection(Task.COL_TAG).document();
            task.setId(taskRef.getId());
            batch.set(taskRef, task.toMap());
        } else {
            taskRef = mDatabase.collection(Task.COL_TAG).document(task.getId());
            batch.update(taskRef, task.toMap());
        }

        // Check each tag, and add or remove the id's that are missing.
        if (mTagsList != null) {
            for (int i = 0; i < mTagsList.size(); i++) {
                Tag curr_tag = mTagsList.get(i);

                boolean tag_has_task = curr_tag.getTask_ids().contains(task.getId());
                boolean task_has_tag = task.getTag_ids().contains(curr_tag.getId());

                // If tag does not have it but the task does, it's a new tag. Add it to the task.
                if (!tag_has_task && task_has_tag) {
                    curr_tag.addTask(task.getId());
                }

                // If tag has the task id, but the task does not, it has been removed.
                if (tag_has_task && !task_has_tag) {
                    curr_tag.removeTask(task.getId());
                }

                // Submit updated tag to the batch operation.
                DocumentReference tagRef = mDatabase.collection(Tag.COL_TAG).document(curr_tag.getId());
                batch.update(tagRef, Tag.TASKS_ID, curr_tag.getTask_ids());
            }
        }

        batch.commit()
                .addOnSuccessListener(v -> onAddNewTaskComplete())
                .addOnFailureListener(Throwable::printStackTrace);
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

    private void startPersonalQuery() {
        if (mPersonalTasks == null) {
            mDatabase.collection(Task.COL_TAG)
                    .whereEqualTo(Task.FAM_ID, false)
                    .whereEqualTo(Task.ACCESS_ID, mUser.getId())
                    .orderBy(Task.DATE_ID)
                    .addSnapshotListener(this::doPersonalTasks);
        }
    }

    private void startFamilyQuery() {
        // Doesn't check if list is null, because this query will need to
        // be restarted when leaving then joining families.
        mDatabase.collection(Task.COL_TAG)
                .whereEqualTo(Task.FAM_ID, true)
                .whereEqualTo(Task.ACCESS_ID, mUser.getFamily())
                .orderBy(Task.DATE_ID)
                .addSnapshotListener(this::doFamilyTasks);
    }

    private void startFamiliesQuery() {
        if (mFamiliesList == null) {
            mDatabase.collection(Family.COL_TAG)
                    .orderBy(Family.NAME_ID)
                    .addSnapshotListener(this::doFamilies);
        }
    }

    private void startTagsQuery() {
        if (mTagsList == null) {
            mDatabase.collection(Tag.COL_TAG)
                    .addSnapshotListener(this::doTags);
        }
    }

    private void doPersonalTasks(QuerySnapshot query, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(LOG_TAG, "doPersonalTasks - Firebase Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (mPersonalTasks == null) mPersonalTasks = new LinkedList<>();

        for (DocumentChange dc : query.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    DocumentSnapshot added = dc.getDocument();
                    Task added_task = Task.fromDoc(added, mUser);

                    mPersonalTasks.add(added_task);
                    break;
                case MODIFIED:
                    DocumentSnapshot mod = dc.getDocument();
                    Task mod_task = Task.fromDoc(mod, mUser);

                    for (int i = 0; i < mPersonalTasks.size(); i++) {
                        Task curr = mPersonalTasks.get(i);
                        if (curr.equals(mod_task)) {
                            mPersonalTasks.set(i, mod_task);
                            break;
                        }
                    }
                    break;
                case REMOVED:
                    DocumentSnapshot removed = dc.getDocument();
                    Task removed_task = Task.fromDoc(removed, mUser);

                    mPersonalTasks.remove(removed_task);
                    break;
                default:
                    Log.w(LOG_TAG, "doPersonalTasks - document change not recognized!!! Type: " + dc.getType());
                    return;
            }
        }

        updatePersonalTasks();
    }

    private void doFamilyTasks(QuerySnapshot query, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(LOG_TAG, "doFamilyTasks - Firebase Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (mFamilyTasks == null) mFamilyTasks = new LinkedList<>();

        for (DocumentChange dc : query.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    DocumentSnapshot added = dc.getDocument();
                    Task added_task = Task.fromDoc(added, mUser);

                    if (!mFamilyTasks.contains(added_task)) {
                        mFamilyTasks.add(added_task);
                    }
                    break;
                case MODIFIED:
                    DocumentSnapshot mod = dc.getDocument();
                    Task mod_task = Task.fromDoc(mod, mUser);

                    for (int i = 0; i < mFamilyTasks.size(); i++) {
                        Task curr = mFamilyTasks.get(i);
                        if (curr.equals(mod_task)) {
                            mFamilyTasks.set(i, mod_task);
                            break;
                        }
                    }
                    break;
                case REMOVED:
                    DocumentSnapshot removed = dc.getDocument();
                    Task removed_task = Task.fromDoc(removed, mUser);

                    mFamilyTasks.remove(removed_task);
                    break;
                default:
                    Log.w(LOG_TAG, "doFamilyTasks - document change not recognized!!! Type: " + dc.getType());
                    return;
            }
        }

        updateFamilyTasks();
    }

    private void updatePersonalTasks() {
        if (mPersonalFrag != null) {
            mPersonalFrag.setData(mPersonalTasks);
            updateWidget(true);
        }
    }

    private void updateFamilyTasks() {
        if (mFamilyFrag != null) {
            mFamilyFrag.setData(mFamilyTasks);
            updateWidget(false);
        }
    }

    private void doFamilies(QuerySnapshot query, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(LOG_TAG, "doFamilies - Firebase Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (mFamiliesList == null) mFamiliesList = new LinkedList<>();

        for (DocumentChange dc : query.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    DocumentSnapshot added = dc.getDocument();
                    Family added_fam = Family.fromDoc(added);

                    mFamiliesList.add(added_fam);
                    break;
                case MODIFIED:
                    DocumentSnapshot mod = dc.getDocument();
                    Family mod_fam = Family.fromDoc(mod);

                    for (int i = 0; i < mFamiliesList.size(); i++) {
                        Family curr = mFamiliesList.get(i);
                        if (curr.equals(mod_fam)) {
                            mFamiliesList.set(i, mod_fam);
                            break;
                        }
                    }
                    break;
                case REMOVED:
                    // Families won't be removed. Ignore this.
                    return;
                default:
                    Log.w(LOG_TAG, "doFamilies - document change not recognized!!! Type: " + dc.getType());
                    return;
            }
        }

        if (mSelectFrag != null) {
            mSelectFrag.setData(mFamiliesList);
        }
    }

    private void doTags(QuerySnapshot querySnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(LOG_TAG, "doTags - Firebase Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (mTagsList == null) mTagsList = new LinkedList<>();

        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
            switch(dc.getType()) {
                case ADDED:
                    DocumentSnapshot added = dc.getDocument();
                    Tag added_tag = Tag.fromDoc(added);
                    mTagsList.add(added_tag);
                    break;
                case MODIFIED:
                    DocumentSnapshot mod = dc.getDocument();
                    Tag mod_tag = Tag.fromDoc(mod);

                    for (int i = 0; i < mTagsList.size(); i++) {
                        Tag curr = mTagsList.get(i);
                        if (curr.equals(mod_tag)) {
                            mTagsList.set(i, mod_tag);
                            break;
                        }
                    }
                    break;
                case REMOVED:
                    // Do nothing
                    break;
                default:
                    return;
            }
        }

        if (mPersonalFrag != null) {
            mPersonalFrag.updateTags();
        }

        if (mFamilyFrag != null) {
            mFamilyFrag.updateTags();
        }
    }

    @Nullable
    @Override
    public Tag getTag(String id) {
        for (Tag curr : mTagsList) {
            if (curr.getId().equals(id)) {
                return curr;
            }
        }

        return null;
    }

    @Nullable
    public List<Tag> getAllTags() {
        return mTagsList;
    }

    private void updateWidget(boolean personal) {
        Log.v(LOG_TAG, "updateWidget - context == null: " + (getContext() == null));

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
        mDatabase.collection(Task.COL_TAG)
                .document(task.getId())
                .update(Task.COMP_ID, task.isComplete());
    }

    @Override
    public void onFamilyTaskCheckClick(Task task, int pos) {
        mDatabase.collection(Task.COL_TAG)
                .document(task.getId())
                .update(Task.COMP_ID, task.isComplete());
    }

    public void onFamilyLeft() {
        mUser.setFamily("");
        Family temp_family = mFamily;
        temp_family.removeMember(mUser.getId());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        DocumentReference famRef = db.collection(Family.COL_TAG).document(temp_family.getId());
        batch.set(famRef, temp_family.toMap());

        DocumentReference userRef = db.collection(User.COL_TAG).document(mUser.getId());
        batch.set(userRef, mUser.toMap());

        batch.commit()
                .addOnSuccessListener(v -> notifyFamiliesUpdate());
    }

    public void onFamilyEntered(String name) {
        Family family = new Family(name, mUser);
        saveSelectedFamily(family, true);
    }

    @Override
    public void onFamilyItemClick(Family family) {
        saveSelectedFamily(family, false);
    }

    private void saveSelectedFamily(Family family, boolean isNewFamily) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        DocumentReference famRef;
        if (isNewFamily) {
            famRef = db.collection(Family.COL_TAG).document();
            family.setId(famRef.getId());
        } else {
            famRef = db.collection(Family.COL_TAG).document(family.getId());
            family.addMember(mUser.getId());
        }

        mFamily = family;
        batch.set(famRef, mFamily.toMap());

        DocumentReference userRef = db.collection(User.COL_TAG).document(mUser.getId());
        mUser.setFamily(famRef.getId());
        batch.set(userRef, mUser.toMap());

        batch.commit()
                .addOnCompleteListener(task -> notifyFamilyUpdate())
                .addOnFailureListener(this);
    }

    // Erase the unused fragment and data at some point?

    private void notifyFamilyUpdate() {
        mNoFamily = false;
        mFamiliesList.clear();
        mListener.onFamilyChange(mFamily);
        mListener.updateFabDesc();
        mTaskAdapter.notifyDataSetChanged();
        startFamilyQuery();
    }

    private void notifyFamiliesUpdate() {
        mFamily = null;
        mNoFamily = true;
        mFamilyTasks.clear();
        mListener.onFamilyChange(null);
        mListener.updateFabDesc();
        mTaskAdapter.notifyDataSetChanged();
        startFamiliesQuery();
    }

    @Override
    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
        mNoFamily = false;
        mTaskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        mUser.setFamily("");
        mFamily = null;
        mNoFamily = true;

        mListener.doSnackbar(R.string.family_add_failure);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
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
            Log.v(LOG_TAG, "TaskListsPagerAdapter - getItem has been run.");

            Fragment result = null;

            if (position == 0) {
//                Log.v(LOG_TAG, "TaskListPagerAdapter, getPersonalFrag - mPersonalTasks.size: " + mPersonalTasks.size());
                Log.v(LOG_TAG, "TaskListPagerAdapter, getPersonalFrag - mPersonalFrag == null: " + (mPersonalFrag == null));
                if (mPersonalFrag == null) {
                    result = PersonalListFragment.newInstance(mPersonalTasks);
                    mPersonalFrag = (PersonalListFragment) result;
                } else {
                    mPersonalFrag.setData(mPersonalTasks);
                    result = mPersonalFrag;
                }
            } else if (position == 1) {
//                Log.v(LOG_TAG, "TaskListPagerAdapter, getFamilyFrag - mFamilyTasks.size: " + mFamilyTasks.size());
                Log.v(LOG_TAG, "TaskListPagerAdapter, getFamilyFrag - mFamilyFrag == null: " + (mFamilyFrag == null));

                if (mNoFamily) {
                    if (mSelectFrag == null) {
                        Log.v(LOG_TAG, "TaskListPagerAdapter, getSelectFrag - mFamiliesList size: " + ((mFamiliesList == null) ? "null" : mFamiliesList.size()));
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
            } else {
                Log.w(LOG_TAG, "TaskListPagerAdapter - position not recognized! getItem.position: " + position);
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
                default:
                    Log.w(LOG_TAG, "Warning!!! TaskListsFragment - getPageTitle() position not recognized: " + position);
                    break;
            }

            Log.v(LOG_TAG, "TaskListsPagerAdapter - getPageTitle: " + result);

            return result;
        }
    }
}
