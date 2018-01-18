package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabItem;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnListsFragmentListener} interface
 * to handle interaction events.
 * Use the {@link TaskListsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskListsFragment extends Fragment
                                implements PersonalListFragment.OnPersonalFragListener,
                                            FamilyListFragment.OnFamilyFragListener,
                                            EventListener {

    private static final String LOG_TAG = TaskListsFragment.class.getSimpleName();

    private static final String TASK_LIST = "TASK_LIST";

    private FirebaseFirestore mDatabase;
    private FirebaseUser mFirebaseUser;
    private User mUser;
    private Family mFamily;

    private List<Task> mPersonalTasks;
    private List<Task> mFamilyTasks;
    private List<Tag> mTags;

    private OnListsFragmentListener mListener;

    private ViewPager mListsPager;
    private TaskListsPagerAdapter mTaskAdapter;

    private TabLayout mTabLayout;

    private PersonalListFragment mPersonalFrag;
    private FamilyListFragment mFamilyFrag;

    public TaskListsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TaskListsFragment.
     */
    public static TaskListsFragment newInstance() {
        TaskListsFragment fragment = new TaskListsFragment();

//        Bundle args = new Bundle(); fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate has been run.");

        if (mPersonalTasks == null) mPersonalTasks = new LinkedList<>();
        if (mFamilyTasks == null) mFamilyTasks = new LinkedList<>();

        mDatabase = FirebaseFirestore.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase.collection("users")
                .document(mFirebaseUser.getUid())
                .addSnapshotListener(this);

//        mDatabase.collection("families")
//                .whereEqualTo("members", )

        // Listener for the personal tasks
        mDatabase.collection(Task.TASKS_ID)
                .whereEqualTo(Task.FAM_ID, false)
                .addSnapshotListener(this);

        // Listener for the family tasks
        mDatabase.collection(Task.TASKS_ID)
                .whereEqualTo(Task.FAM_ID, true)
                .addSnapshotListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_task_lists, container, false);

        FragmentManager fragmentManager = getFragmentManager();
        mTaskAdapter = new TaskListsPagerAdapter(fragmentManager);

        mListsPager = rootView.findViewById(R.id.main_view_pager);
        mTabLayout = rootView.findViewById(R.id.main_tab_layout);

        mListsPager.setAdapter(mTaskAdapter);
        mTabLayout.setupWithViewPager(mListsPager);

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onAddTaskPressed() {

        if (mListener != null) {
            String selectedTab = (String) mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).getText();

            // We may not need this in the future. It's only to do the Snackbar with the CoordinatorLayout in MainActivity.
            mListener.onAddTask(selectedTab);

            // Launch the dialog fragment...
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListsFragmentListener) {
            mListener = (OnListsFragmentListener) context;
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
    public void onFragmentInteraction() {

    }

    @Override
    public void onFamilyFragAttach() {

    }

    @Override
    public void onPersonalFragAttach() {
        mPersonalFrag.setData(mPersonalTasks);
    }

//    @Override
//    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//        // Check if there was an exception first.
//        if (e != null) {
//            Log.w(LOG_TAG, "onEvent - Firebase Exception: " + e.getMessage());
//            e.printStackTrace();
//            return;
//        }
//
//
//
//        boolean pers = false;
//        boolean fam = false;
//        for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
//
//            Log.v(LOG_TAG, "onEvent - Document change Collection path: " + dc.getDocument().getReference().getParent().getPath());
//
//            switch (dc.getType()) {
//                case ADDED:
//                    DocumentSnapshot added = dc.getDocument();
//
//                    added.getReference().getParent().getPath();
//
//                    Task added_task = added.toObject(Task.class);
//                    if (added_task.isFamily()) {
//                        mFamilyTasks.add(added_task);
//                        fam = true;
//                    } else {
//                        mPersonalTasks.add(added_task);
//                        pers = true;
//                    }
//                    break;
//                case MODIFIED:
//                    DocumentSnapshot mod = dc.getDocument();
//                    Task mod_task = mod.toObject(Task.class);
//
//                    // Figure out if the access_id is different than before
//                    // Is it going from personal to family, or family to personal?
//                    // Remove from the old list, change the family property, and add to the new list.
//
//                    String access_id = mod_task.getAccess_id();
//                    boolean family = mod_task.isFamily();
//                    int old_ind = dc.getOldIndex();
//
//                    if (family) {
//                        Task old_task = mFamilyTasks.get(old_ind);
//
//                    }
//
//                    if (family) {
//                        fam = true;
//                    } else {
//                        pers = true;
//                    }
//
//                    break;
//                case REMOVED:
//                    DocumentSnapshot removed = dc.getDocument();
//                    Task removed_task = removed.toObject(Task.class);
//                    if (removed_task.isFamily()) {
//                        mFamilyTasks.remove(removed_task);
//                        fam = true;
//                    } else {
//                        mPersonalTasks.remove(removed_task);
//                        pers = true;
//                    }
//                    break;
//                default:
//                    Log.w(LOG_TAG, "onEvent - document change not recognized!!! Type: " + dc.getType());
//            }
//        }
//
//        if (fam) mFamilyFrag.setData(mFamilyTasks);
//        if (pers) mPersonalFrag.setData(mPersonalTasks);
//    }

    @Override
    public void onEvent(Object result, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(LOG_TAG, "onEvent - Firebase Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (result instanceof QuerySnapshot) { // Event from Tasks collection
            QuerySnapshot documentSnapshots = (QuerySnapshot) result;

            boolean pers = false;
            boolean fam = false;
            for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {

                Log.v(LOG_TAG, "onEvent - Document change Collection path: " + dc.getDocument().getReference().getParent().getPath());

                switch (dc.getType()) {
                    case ADDED:
                        DocumentSnapshot added = dc.getDocument();

                        added.getReference().getParent().getPath();

                        Task added_task = added.toObject(Task.class);
                        if (added_task.isFamily()) {
                            mFamilyTasks.add(added_task);
                            fam = true;
                        } else {
                            mPersonalTasks.add(added_task);
                            pers = true;
                        }
                        break;
                    case MODIFIED:
                        DocumentSnapshot mod = dc.getDocument();
                        Task mod_task = mod.toObject(Task.class);

                        // Figure out if the access_id is different than before
                        // Is it going from personal to family, or family to personal?
                        // Remove from the old list, change the family property, and add to the new list.

                        String access_id = mod_task.getAccess_id();
                        boolean family = mod_task.isFamily();
                        int old_ind = dc.getOldIndex();

                        if (family) {
                            Task old_task = mFamilyTasks.get(old_ind);

                        }

                        if (family) {
                            fam = true;
                        } else {
                            pers = true;
                        }

                        break;
                    case REMOVED:
                        DocumentSnapshot removed = dc.getDocument();
                        Task removed_task = removed.toObject(Task.class);
                        if (removed_task.isFamily()) {
                            mFamilyTasks.remove(removed_task);
                            fam = true;
                        } else {
                            mPersonalTasks.remove(removed_task);
                            pers = true;
                        }
                        break;
                    default:
                        Log.w(LOG_TAG, "onEvent - document change not recognized!!! Type: " + dc.getType());
                }
            }

            if (fam) mFamilyFrag.setData(mFamilyTasks);
            if (pers) mPersonalFrag.setData(mPersonalTasks);

        } else if (result instanceof DocumentSnapshot) { // Event from Families collection
            DocumentSnapshot documentSnapshot = (DocumentSnapshot) result;


        }
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
        // TODO: Update argument type and name
        void onAddTask(String tab);
        void onListsFragAttach();
    }


    // ####---- View Pager ----####

    private class TaskListsPagerAdapter extends FragmentPagerAdapter {

        public TaskListsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment result = null;

            if (position == 0) {
                Log.v(LOG_TAG, "TaskListPagerAdapter, getPersonalFrag - mPersonalTasks.size: " + mPersonalTasks.size());
                if (mPersonalFrag == null) {
                    result = PersonalListFragment.newInstance(mPersonalTasks);
                    mPersonalFrag = (PersonalListFragment) result;
                } else {
                    result = mPersonalFrag;
                }
            } else if (position == 1) {
                if (mFamilyFrag == null) {
                    result = FamilyListFragment.newInstance(mFamilyTasks);
                    mFamilyFrag = (FamilyListFragment) result;
                } else {
                    result = mFamilyFrag;
                }
            } else {
                Log.w(LOG_TAG, "TaskListPagerAdapter - position not recognized! getItem.position: " + position);
            }

            return result;
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
