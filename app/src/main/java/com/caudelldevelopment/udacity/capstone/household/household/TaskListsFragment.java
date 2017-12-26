package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
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
public class TaskListsFragment extends Fragment {

    private static final String LOG_TAG = TaskListsFragment.class.getSimpleName();

    private static final String TASK_LIST = "TASK_LIST";

    private List<Task> mPersonalTasks;
    private List<Task> mFamilyTasks;

    private OnListsFragmentListener mListener;

    private ViewPager mTaskLists;
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

        Task[] task_arr = new Task[data.size()];
        task_arr = data.toArray(task_arr);
        args.putParcelableArray(TASK_LIST, task_arr);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPersonalTasks = new LinkedList<>();
        mFamilyTasks = new LinkedList<>();

        if (getArguments() != null) {
            Task[] task_arr = (Task[]) getArguments().getParcelableArray(TASK_LIST);

            if (task_arr != null) {
                for (int i = 0; i < task_arr.length; i++) {
                    Task temp = task_arr[i];

                    if (temp.isFamily()) mFamilyTasks.add(temp);
                    else                 mPersonalTasks.add(temp);

                }

                // Do something to give mPersonalTasks to mPersonalFrag
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

        mTaskLists = rootView.findViewById(R.id.main_view_pager);
        mTaskLists.setAdapter(mTaskAdapter);

        mTabLayout = rootView.findViewById(R.id.main_tab_layout);
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.personal_title)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.family_title)));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mTabLayout.setupWithViewPager(mTaskLists);

//        mPersonalTab = rootView.findViewById(R.id.tab_personal);
//        mFamilyTab = rootView.findViewById(R.id.tab_family);

        return rootView;
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
    }


    // ####---- View Pager ----####

    private class TaskListsPagerAdapter extends FragmentStatePagerAdapter {

        public TaskListsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment result = null;

            if (position == 0) {
                result = new PersonalListFragment();
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

            return result;
        }
    }
}
