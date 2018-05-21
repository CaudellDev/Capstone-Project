package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.pchmn.materialchips.ChipView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FamilyListFragment extends Fragment {

    private static final String LOG_TAG = FamilyListFragment.class.getSimpleName();
    private static final String FAML_TASK_LIST = "family_task_list";

    private RecyclerView mTaskList;
    private FamilyAdapter mAdapter;
    private TextView mEmptyView;

    private OnFamilyFragListener mListener;

    public FamilyListFragment() {
        // Required empty public constructor
    }

    public static FamilyListFragment newInstance(@Nullable List<Task> data) {
        FamilyListFragment fragment = new FamilyListFragment();
        Bundle args = new Bundle();

        if (data != null) {
            Task[] task_arr = new Task[data.size()];
            task_arr = data.toArray(task_arr);
            args.putParcelableArray(FAML_TASK_LIST, task_arr);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_family_list, container, false);
        mEmptyView = rootView.findViewById(R.id.family_empty_tv);
        mTaskList = rootView.findViewById(R.id.family_list_rv);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getContext().getDrawable(R.drawable.list_divider));
        mTaskList.addItemDecoration(itemDecoration);
        mTaskList.setLayoutManager(new LinearLayoutManager(getContext()));

        if (mAdapter == null) {
            mAdapter = new FamilyAdapter();
        }
        mTaskList.setAdapter(mAdapter);

        if (getArguments() != null) {
            Parcelable[] temp_arr = getArguments().getParcelableArray(FAML_TASK_LIST);

            if (temp_arr != null) {
                Task[] task_arr = Arrays.copyOf(temp_arr, temp_arr.length, Task[].class);
                List<Task> data = new LinkedList<>(Arrays.asList(task_arr));
                setData(data);
            }
        }

        updateEmpty();

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            Parcelable[] stored = savedInstanceState.getParcelableArray(FAML_TASK_LIST);

            if (stored != null) {
                Task[] temp_arr = Arrays.copyOf(stored, stored.length, Task[].class);

                if (mAdapter == null) {
                    mAdapter = new FamilyAdapter();

                    if (mTaskList != null) {
                        mTaskList.setAdapter(mAdapter);
                    }
                }

                mAdapter.update(Arrays.asList(temp_arr));
                updateEmpty();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Task[] temp_arr = new Task[0];
        temp_arr = mAdapter.data.toArray(temp_arr);

        Parcelable[] store;
        store = Arrays.copyOf(temp_arr, temp_arr.length, Parcelable[].class);
        outState.putParcelableArray(FAML_TASK_LIST, store);
    }

    @Override
    public void onAttach(Context context) {
        Log.v(LOG_TAG, "onAttach has started!!!");
        super.onAttach(context);

        Fragment parent = getFragmentManager().findFragmentById(R.id.main_task_lists);

        if (parent != null) {
            if (parent instanceof OnFamilyFragListener) {
                mListener = (OnFamilyFragListener) parent;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnFamilyFragListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setData(List<Task> data) {
        mAdapter.update(data);
        updateEmpty();
    }

    public void updateTags() {
        mAdapter.notifyDataSetChanged();
    }

    private void updateEmpty() {
        boolean isEmpty = (mAdapter.data == null) || mAdapter.data.isEmpty();

        if (isEmpty) {
            mTaskList.setVisibility(View.GONE);
            mEmptyView.setText(R.string.empty_task_msg);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mTaskList.setVisibility(View.VISIBLE);
        }
    }

    private class FamilyTaskViewHolder extends RecyclerView.ViewHolder {

        private View item;
        private TextView title;
        private TextView date;
        private TextView desc;
        private CheckBox comp;

        private LinearLayout tags_layout;

        public FamilyTaskViewHolder(View itemView) {
            super(itemView);

            item = itemView;
            title = itemView.findViewById(R.id.task_title);
            date = itemView.findViewById(R.id.task_date);
            desc = itemView.findViewById(R.id.task_desc);
            comp = itemView.findViewById(R.id.task_checkbox);

            tags_layout = itemView.findViewById(R.id.task_tags_ll);

            comp.setOnClickListener(view -> onCompleteChanged());
            item.setOnClickListener(view -> onItemClick());
        }

        public void onCompleteChanged() {
            int pos = getAdapterPosition();
            Task curr = mAdapter.data.get(pos);

            mListener.onFamilyTaskCheckClick(curr);
        }

        public void onItemClick() {
            int pos = getAdapterPosition();
            Task curr = mAdapter.data.get(pos);

            mListener.onFamilyTaskClick(curr);
        }
    }

    public class FamilyAdapter extends RecyclerView.Adapter<FamilyTaskViewHolder> {

        // Array list of the tasks
        protected List<Task> data;

        FamilyAdapter() {
            data = new LinkedList<>();
        }

        void update(List<Task> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public FamilyTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            return new FamilyTaskViewHolder(item);
        }

        @Override
        public void onBindViewHolder(FamilyTaskViewHolder holder, int position) {
            Task curr = data.get(position);

            holder.title.setText(curr.getName());
            holder.date.setText(curr.getDate());
            holder.desc.setText(curr.getDesc());
            holder.comp.setChecked(curr.isComplete());

            List<ChipView> tags_list = getTagChipList(curr);

            holder.tags_layout.removeAllViews();
            for (ChipView curr_chip : tags_list) {
                holder.tags_layout.addView(curr_chip);
            }
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        private List<ChipView> getTagChipList(Task task) {
            List<ChipView> result = new LinkedList<>();

            for (String curr : task.getTag_ids().values()) {
                ChipView chip = new ChipView(getContext());
                chip.setLabel(curr);
                chip.setPadding(4, 4, 4, 4);
                chip.setLabelColor(getResources().getColor(R.color.black));
                chip.setChipBackgroundColor(getResources().getColor(R.color.colorAccent));

                result.add(chip);
            }

            return result;
        }
    }

    public interface OnFamilyFragListener {
        void onFamilyTaskCheckClick(Task task);
        void onFamilyTaskClick(Task task);
    }
}
