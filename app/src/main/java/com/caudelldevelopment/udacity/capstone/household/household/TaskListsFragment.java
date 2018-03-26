package com.caudelldevelopment.udacity.capstone.household.household;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

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

    private OnListsFragmentListener mListener;

    private ViewPager mListsPager;
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
        Log.v(LOG_TAG, "onCreate has been run.");

        if (mPersonalTasks == null) mPersonalTasks = new LinkedList<>();
        if (mFamilyTasks   == null) mFamilyTasks   = new LinkedList<>();
        if (mFamiliesList  == null) mFamiliesList  = new LinkedList<>();

        mDatabase = FirebaseFirestore.getInstance();

        // Listener for the personal tasks
        mDatabase.collection(Task.COL_TAG)
                .whereEqualTo(Task.FAM_ID, false)
                .whereEqualTo(Task.ACCESS_ID, mUser.getId())
                .orderBy(Task.DATE_ID)
                .addSnapshotListener(this::doPersonalTasks);

        if (mNoFamily) {
            mDatabase.collection(Family.COL_TAG)
                    .orderBy(Family.NAME_ID)
                    .addSnapshotListener(this::doFamilies);
        } else {
            // Listener for the family tasks
            mDatabase.collection(Task.COL_TAG)
                    .whereEqualTo(Task.FAM_ID, true)
                    .whereEqualTo(Task.ACCESS_ID, mUser.getFamily())
                    .orderBy(Task.DATE_ID)
                    .addSnapshotListener(this::doFamilyTasks);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_task_lists, container, false);

        FragmentManager fragmentManager = getFragmentManager();
        mTaskAdapter = new TaskListsPagerAdapter(fragmentManager);

        mListsPager = rootView.findViewById(R.id.main_view_pager);
        mTabLayout  = rootView.findViewById(R.id.main_tab_layout);

        mListsPager.setAdapter(mTaskAdapter);
        mTabLayout.setupWithViewPager(mListsPager);

        return rootView;
    }

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
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListsFragmentListener) {
            mListener = (OnListsFragmentListener) context;
            mUser = mListener.getUser();
            mNoFamily = (mUser.getFamily() == null) || (mUser.getFamily().isEmpty());
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
        super.onSaveInstanceState(outState);
    }


    // Would this be better in a WorkerThread or AsyncTask? Is this thread safe?
    public void addNewTask(Task task) {
        // Added this to check if this triggers when a task has been edited.
        Log.v(LOG_TAG, "addNewTask has started!!! task: " + task.getName() + ", id: " + task.getId());

        WriteBatch batch = mDatabase.batch();

        DocumentReference taskRef;
        if (task.getId() == null || task.getId().isEmpty()) {
            taskRef = mDatabase.collection(Task.COL_TAG).document();
            batch.set(taskRef, task.toMap());
        } else {
            taskRef = mDatabase.collection(Task.COL_TAG).document(task.getId());
            batch.update(taskRef, task.toMap());
        }

        // Add the task reference id to the tags the task is using.
        // For now, Firebase doesn't support adding an item to an array field.
        // This just replaces the entry. For a final product, I would probably
        // get the tags from Firebase first. Once that query is complete, I could
        // add the new item and submit the full list.
        for (int i = 0; i < task.getTag_ids().size(); i++) {
            Tag currTag = new Tag(task.getTag(i));
            DocumentReference currRef = mDatabase.collection(Tag.COL_TAG).document(currTag.getId());
            batch.update(currRef, Tag.TASKS_ID, Collections.singletonList(taskRef.getId()));
        }

        // Add the task id to the list of tasks in the family or user
        if (task.isFamily()) {
            DocumentReference famRef = mDatabase.collection(Family.COL_TAG).document(task.getAccess_id());
            List<String> task_ids = mFamily.getTask_ids();

            // Check if the new task already exists to avoid duplicates.
            if (!task_ids.contains(taskRef.getId())) {
                task_ids.add(taskRef.getId());
                batch.update(famRef, Family.TASKS_ID, task_ids);
            }
        } else {
            DocumentReference persRef = mDatabase.collection(User.COL_TAG).document(task.getAccess_id());
            List<String> task_ids = mUser.getTask_ids();

            if (!task_ids.contains(taskRef.getId())) {
                task_ids.add(taskRef.getId());
                batch.update(persRef, User.TASKS_ID, task_ids);
            }
        }

        batch.commit()
                .addOnSuccessListener(Void -> mListener.onAddTaskComplete())
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void addNewTask(Task task, boolean accessChange) {
        if (accessChange) {
            // If the list is currently set for family, it used to be in the personal task list.
            List<Task> remove = task.isFamily() ? mPersonalTasks : mFamilyTasks;

            // I probably can't use the remove function because any of the values could be different. Only the id will always be the same.
            for (Task curr : remove) {
                if (curr.getId().equals(task.getId())) {
                    remove.remove(curr);
                }
            }
        }

        addNewTask(task);
    }

    private void doPersonalTasks(QuerySnapshot query, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(LOG_TAG, "doPersonalTasks - Firebase Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }

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

                    mPersonalTasks.add(mod_task);
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

        if (mPersonalFrag != null) {
            mPersonalFrag.setData(mPersonalTasks);
            updateWidget(true);
        }
    }

    private void doFamilyTasks(QuerySnapshot query, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(LOG_TAG, "doFamilyTasks - Firebase Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        for (DocumentChange dc : query.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    DocumentSnapshot added = dc.getDocument();
                    Task added_task = Task.fromDoc(added, mUser);

                    mFamilyTasks.add(added_task);
                    break;
                case MODIFIED:
                    DocumentSnapshot mod = dc.getDocument();
                    Task mod_task = Task.fromDoc(mod, mUser);

                    mFamilyTasks.add(mod_task);
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

        for (DocumentChange dc : query.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    DocumentSnapshot added = dc.getDocument();
                    Family added_fam = Family.fromDoc(added);
                    mFamiliesList.add(added_fam);
                    break;
                case MODIFIED:
                    Log.v(LOG_TAG, "doFamilies - families modified.");
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
`
//    @Override
//    public void onEvent(QuerySnapshot result, FirebaseFirestoreException e) {
//        if (e != null) {
//            Log.w(LOG_TAG, "onEvent - Firebase Exception: " + e.getMessage());
//            e.printStackTrace();
//            return;
//        }
//
//        if (result instanceof QuerySnapshot) { // Event from Tasks and Family collection
//            QuerySnapshot documentSnapshots = (QuerySnapshot) result;
//
//            boolean pers = false;
//            boolean fam = false;
//            boolean sel_fam = false;
//            for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
//
//                Log.v(LOG_TAG, "onEvent, Tasks/Family collections - change type: " + dc.getType() + ", document path: " + dc.getDocument().getReference().getPath());
//
//                switch (dc.getType()) {
//
//                    case ADDED:
//                        DocumentSnapshot added = dc.getDocument();
//
//                        if (isDocFamily(added)) {
//                            Family added_fam = Family.fromDoc(added);
//                            mFamiliesList.add(added_fam);
//                            sel_fam = true;
//                        } else {
//                            Task added_task = Task.fromDoc(added, mUser);
//                            if (added_task.isFamily()) {
//                                mFamilyTasks.add(added_task);
//                                fam = true;
//                            } else {
//                                mPersonalTasks.add(added_task);
//                                pers = true;
//                            }
//                        }
//                        break;
//                    case MODIFIED:
//                        DocumentSnapshot mod = dc.getDocument();
//
//                        if (isDocFamily(mod)) {
//                            Family mod_fam = Family.fromDoc(mod);
//                            mFamiliesList.add(mod_fam);
//                            sel_fam = true;
//                        } else {
//                            Task mod_task = Task.fromDoc(mod, mUser);
//                            if (mod_task.isFamily()) { // Change in access id is already handled.
//                                mFamilyTasks.add(mod_task);
//                                fam = true;
//                            } else {
//                                mPersonalTasks.add(mod_task);
//                                pers = true;
//                            }
//                        }
//                        break;
//                    case REMOVED:
//                        // Families won't be removed, so we can ignore them here.
//                        DocumentSnapshot removed = dc.getDocument();
//
//                        Task removed_task = Task.fromDoc(removed, mUser);
//                        if (removed_task.isFamily()) {
//                            mFamilyTasks.remove(removed_task);
//                            fam = true;
//                        } else {
//                            mPersonalTasks.remove(removed_task);
//                            pers = true;
//                        }
//                        break;
//                    default:
//                        Log.w(LOG_TAG, "onEvent - document change not recognized!!! Type: " + dc.getType());
//                }
//            }
//
//            if (fam && mFamilyFrag != null) {
//                mFamilyFrag.setData(mFamilyTasks);
//                updateWidget(false);
//            }
//
//            if (pers && mPersonalFrag != null) {
//                mPersonalFrag.setData(mPersonalTasks);
//                updateWidget(true);
//            }
//
//            if (sel_fam && mSelectFrag != null) mSelectFrag.setData(mFamiliesList);
//
//        } else if (result instanceof DocumentSnapshot) { // Event from Families and Users collection
//            DocumentSnapshot documentSnapshot = (DocumentSnapshot) result;
//
//            Log.v(LOG_TAG, "onEvent, DocumentSnapshot contains family: " + documentSnapshot.contains(User.FAMILY_ID));
//
//            if (documentSnapshot.contains(User.FAMILY_ID)) { // Users
//
//
//                mUser = User.fromDoc(documentSnapshot);
//
//                Log.v(LOG_TAG, "onEvent, DocumentSnapshot - user id: " + mUser.getName() + ", " + mUser.getId() + ", " + mUser.getFamily());
//
//                // Now that I know the family id, I can query for that family object.
//                if (mUser.getFamily() != null && !mUser.getFamily().isEmpty()) {
//                    mDatabase.collection(Family.COL_TAG)
//                            .document(mUser.getFamily())
//                            .addSnapshotListener(this);
//                } else {
//                    // User hasn't chosen a family yet...
//                }
//
//            } else if (documentSnapshot.contains(Family.MEMBERS_ID)) { // Families
//                mFamily = Family.fromDoc(documentSnapshot);
//            }
//        } else {
//            Log.w(LOG_TAG, "onEvent - result type not recognized! result.class.name: " + result.getClass().getName());
//        }
//    }
//    private boolean isDocFamily(DocumentSnapshot doc) {
//        CollectionReference famRef = mDatabase.collection(Family.COL_TAG);
//        CollectionReference testRef = doc.getReference().getParent();
//
//        Log.v(LOG_TAG, "isDocFamily - result: " + (famRef == testRef) + ", path: " + doc.getReference().getPath());
//        return famRef == testRef;
//    }

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
        Map<String, Object> map = new HashMap<>();
        map.put(Task.COMP_ID, task.isComplete());

        mDatabase.collection(Task.COL_TAG)
                .document(task.getId())
                .update(map);
    }

    @Override
    public void onFamilyTaskCheckClick(Task task, int pos) {
        Map<String, Object> map = new HashMap<>();
        map.put(Task.COMP_ID, task.isComplete());

        mDatabase.collection(Task.COL_TAG)
                .document(task.getId())
                .update(map);
    }

    public void onFamilyLeft() {
        mNoFamily = true;
        mUser.setFamily("");
        mFamily.removeMember(mUser.getId());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Family.COL_TAG)
                .document(mFamily.getId())
                .update(mFamily.toMap());

        db.collection(User.COL_TAG)
                .document(mUser.getId())
                .update(User.FAMILY_ID, mUser.getFamily());


    }

    public void onFamilyEntered(String name) {
        Family family = new Family(name, mUser);
        saveSelectedFamily(family);
    }

    @Override
    public void onFamilyItemClick(Family family) {
        saveSelectedFamily(family);
    }

    private void saveSelectedFamily(Family family) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        DocumentReference famRef = db.collection(Family.COL_TAG).document();
        mFamily = family;
        batch.set(famRef, mFamily.toMap());

        DocumentReference userRef = db.collection(User.COL_TAG).document(mUser.getId());
        mUser.setFamily(famRef.getId());
        batch.set(userRef, mUser.toMap());

        batch.commit()
                .addOnCompleteListener(this)
                .addOnFailureListener(this);
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
        void onAddTaskComplete();
        void onTaskClick(Task task, String tab);
        User getUser();
        void doSnackbar(int str_res);
    }


    // ####---- View Pager ----####

    private class TaskListsPagerAdapter extends FragmentPagerAdapter {

        public TaskListsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.v(LOG_TAG, "TaskListsPagerAdapter - getItem has been run.");

            Fragment result = null;

            if (position == 0) {
                Log.v(LOG_TAG, "TaskListPagerAdapter, getPersonalFrag - mPersonalTasks.size: " + mPersonalTasks.size());
                Log.v(LOG_TAG, "TaskListPagerAdapter, getPersonalFrag - mPersonalFrag == null: " + (mPersonalFrag == null));
                if (mPersonalFrag == null) {
                    result = PersonalListFragment.newInstance(mPersonalTasks);
                    mPersonalFrag = (PersonalListFragment) result;
                } else {
                    mPersonalFrag.setData(mPersonalTasks);
                    result = mPersonalFrag;
                }
            } else if (position == 1) {
                Log.v(LOG_TAG, "TaskListPagerAdapter, getFamilyFrag - mFamilyTasks.size: " + mFamilyTasks.size());
                Log.v(LOG_TAG, "TaskListPagerAdapter, getFamilyFrag - mFamilyFrag == null: " + (mFamilyFrag == null));

                if (mNoFamily) {
                    if (mSelectFrag == null) {
                        result = SelectFamilyFrag.newInstance(mFamiliesList);
                        mSelectFrag = (SelectFamilyFrag) result;
                    } else {
                        // No data to update?
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
