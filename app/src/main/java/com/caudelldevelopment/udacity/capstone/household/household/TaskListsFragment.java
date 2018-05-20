package com.caudelldevelopment.udacity.capstone.household.household;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.caudelldevelopment.udacity.capstone.household.household.service.MyResultReceiver;
import com.caudelldevelopment.udacity.capstone.household.household.widget.TasksWidget;

import java.util.LinkedList;
import java.util.List;

public class TaskListsFragment
        extends Fragment
        implements PersonalListFragment.OnPersonalFragListener,
                    FamilyListFragment.OnFamilyFragListener,
                    SelectFamilyFrag.OnSelectFamilyListener {

    private static final String LOG_TAG = TaskListsFragment.class.getSimpleName();

    // We need to keep these lists so we have a place for the
    // tasks before the child Fragments are crated.
    private List<Task> mPersonalTasks;
    private List<Task> mFamilyTasks;
    private List<Family> mFamiliesList;

    private TaskListsPagerAdapter mTaskAdapter;
    private TabLayout mTabLayout;

    private PersonalListFragment mPersonalFrag;
    private FamilyListFragment   mFamilyFrag;
    private SelectFamilyFrag     mSelectFrag;
    private OnListsFragmentListener mListener;

    public TaskListsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                mListener.onTabChanged();
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void addNewTask(Task new_task, @Nullable Task old_task) {
        if (mFamilyTasks == null) mFamilyTasks = new LinkedList<>();
        if (old_task == null) {
            if (new_task.isFamily()) {
                mFamilyTasks.add(new_task);
                updateFamilyTasks();
            } else {
                mPersonalTasks.add(new_task);
                updatePersonalTasks();
            }
        } else {
            boolean access_change = !new_task.getAccess_id().equals(old_task.getAccess_id());

            if (access_change) {
                if (new_task.isFamily()) {
                    // Remove from personal and add to family
                    mPersonalTasks.remove(old_task);
                    mFamilyTasks.add(new_task);
                } else {
                    // Remove from family and add to personal
                    mFamilyTasks.remove(old_task);
                    mPersonalTasks.add(new_task);
                }
                updatePersonalTasks();
                updateFamilyTasks();
            } else {
                if (new_task.isFamily()) {
                    // Replace task in family list
                    int index = mFamilyTasks.indexOf(old_task);
                    mFamilyTasks.set(index, new_task);
                    updateFamilyTasks();
                } else {
                    // Replace task in personal list
                    int index = mPersonalTasks.indexOf(old_task);
                    mPersonalTasks.set(index, new_task);
                    updatePersonalTasks();
                }
            }
        }
    }

    public List<Task> getPersonalTasks() {
        return mPersonalTasks;
    }

    public void setPersonalTasks(List<Task> personalTasks) {
        mPersonalTasks = personalTasks;
        updatePersonalTasks();
    }

    private void updatePersonalTasks() {
        if (mPersonalFrag != null) {
            mPersonalFrag.setData(mPersonalTasks);
        }

        if (mTaskAdapter != null) {
            mTaskAdapter.notifyDataSetChanged();
        }
    }

    public List<Task> getFamilyTasks() {
        return mFamilyTasks;
    }

    public void setFamilyTasks(List<Task> familyTasks) {
        mFamilyTasks = familyTasks;
        updateFamilyTasks();
    }

    private void updateFamilyTasks() {
        if (mFamilyFrag != null) {
            mFamilyFrag.setData(mFamilyTasks);
        }

        if (mTaskAdapter != null) {
            mTaskAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPersonalTaskClick(Task task) {
        mListener.onTaskClick(task, getString(R.string.personal_title));
    }

    @Override
    public void onFamilyTaskClick(Task task) {
        mListener.onTaskClick(task, getString(R.string.family_title));
    }

    @Override
    public void onPersonalTaskCheckClick(Task task) {
        mListener.onTaskCheckClick(task);
    }

    @Override
    public void onFamilyTaskCheckClick(Task task) {
        mListener.onTaskCheckClick(task);
    }

    public void onFamilyLeft() {
        mFamilyTasks = null;

        if (mTaskAdapter != null) {
            mTaskAdapter.notifyDataSetChanged();
        }
    }

    public void updateFamiliesList(List<Family> families) {
        mFamiliesList = families;

        if (mTaskAdapter != null) {
            mTaskAdapter.notifyDataSetChanged();
        }

        if (mSelectFrag != null) {
            mSelectFrag.setData(families);
        }
    }

    @Override
    public void onFamilyItemClick(Family family) {
        mListener.onFamilyItemClicked(family);
    }

    public interface OnListsFragmentListener {
        void onListsFragAttach();

        User getUser();
        Family getFamily();
        boolean isNoFamily();
        MyResultReceiver getServiceReceiver(String type);

        void onTaskClick(Task task, String tab);
        void onTaskCheckClick(Task task);

        void onFamilyItemClicked(Family family);
        void onFamilyChange(@Nullable Family family);

        void onTabChanged();
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
                if (mListener.isNoFamily()) {
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
            if (object instanceof SelectFamilyFrag && !mListener.isNoFamily()) {
                return POSITION_NONE;
            }

            if (object instanceof FamilyListFragment && mListener.isNoFamily()) {
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
