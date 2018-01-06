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

import com.caudelldevelopment.udacity.capstone.household.household.data.Task;

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
                                implements PersonalListFragment.OnPersonalFragListener {

    private static final String LOG_TAG = TaskListsFragment.class.getSimpleName();

    private static final String TASK_LIST = "TASK_LIST";

    private List<Task> mPersonalTasks;
    private List<Task> mFamilyTasks;

    private OnListsFragmentListener mListener;

    private ViewPager mListsPager;
    private TaskListsPagerAdapter mTaskAdapter;

    private TabLayout mTabLayout;
    private TabItem mPersonalTab;
    private TabItem mFamilyTab;

    private PersonalListFragment mPersonalFrag;
    private FamilyListFragment mFamilyFrag;

    public TaskListsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param data Parameter 1.
     * @return A new instance of fragment TaskListsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TaskListsFragment newInstance(List<Task> data) {
        TaskListsFragment fragment = new TaskListsFragment();
        Bundle args = new Bundle();

//        Log.v(LOG_TAG, "newInstance - data.size: " + data.size());
//        for (Task curr : data) {
//            Log.v(LOG_TAG, "newInstance - curr task.title: " + curr.getTitle());
//        }

        Log.v(LOG_TAG, "newInstance - log test");

        Task[] task_arr = new Task[data.size()];
        task_arr = data.toArray(task_arr);
        args.putParcelableArray(TASK_LIST, task_arr);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(LOG_TAG, "onCreate has been run.");

        if (mPersonalTasks == null) mPersonalTasks = new LinkedList<>();

        mFamilyTasks = new LinkedList<>();

        if (getArguments() != null) {
            Task[] task_arr = (Task[]) getArguments().getParcelableArray(TASK_LIST);

            if (task_arr != null) {
                for (int i = 0; i < task_arr.length; i++) {
                    Task temp = task_arr[i];

                    if (temp.isFamily()) mFamilyTasks.add(temp);
                    else                 mPersonalTasks.add(temp);

                }

            } else {
                Log.w(LOG_TAG, "onCreate - task_arr is null!!");
            }
        }
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

    public void updateListData(List<Task> data) {
        Log.v(LOG_TAG, "updateListData - data.size: " + data.size());
        for (Task curr : data) {
            Log.v(LOG_TAG, "updateListData - curr task.title: " + curr.getTitle());
        }

        List<Task> tempPers = new LinkedList<>();
        List<Task> tempFaml = new LinkedList<>();

        for (Task curr : data) {
            Log.v(LOG_TAG, "updateListData - curr.isFamily: " + curr.isFamily());
            if (curr.isFamily()) tempFaml.add(curr);
            else                 tempPers.add(curr);
        }

        mPersonalTasks = tempPers;
        mFamilyTasks = tempFaml;

        Log.v(LOG_TAG, "updateListData - mPersonalTasks.size: " + mPersonalTasks.size());

        if (mPersonalFrag != null) {
            mPersonalFrag.setData(mPersonalTasks);
        }

        if (mFamilyFrag != null) {
            // Same function in family frag class
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onAddTaskPressed() {

        if (mListener != null) {
            String selectedTab = (String) mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).getText();

            // Add new task to the ViewPager fragment.
            mListener.onAddTask(selectedTab);
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
    public void onPersonalFragAttach() {
        mPersonalFrag.setData(mPersonalTasks);
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
                result = new FamilyListFragment();
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
