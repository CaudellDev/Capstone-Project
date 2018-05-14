package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
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

import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.pchmn.materialchips.ChipView;
import com.pchmn.materialchips.model.Chip;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PersonalListFragment extends Fragment {

    private static final String LOG_TAG = PersonalListFragment.class.getSimpleName();
    private static final String PERS_TASK_LIST = "parsonal_task_list";

    private RecyclerView mTaskList;
    private PersonalAdapter mAdapter;
    private TextView mEmptyView;

    private OnPersonalFragListener mListener;

    public PersonalListFragment() {
        // Required empty public constructor
    }

    public static PersonalListFragment newInstance(@Nullable List<Task> data) {
        PersonalListFragment fragment = new PersonalListFragment();
        Bundle args = new Bundle();

        if (data != null) {
            Task[] task_arr = new Task[data.size()];
            task_arr = data.toArray(task_arr);
            args.putParcelableArray(PERS_TASK_LIST, task_arr);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<Task> data;

        if (getArguments() != null) {
            Parcelable[] temp_arr = getArguments().getParcelableArray(PERS_TASK_LIST);

            if (temp_arr != null && temp_arr.length > 0) {
                Task[] task_arr = Arrays.copyOf(temp_arr, temp_arr.length, Task[].class);
                data = new LinkedList<>(Arrays.asList(task_arr));
            } else {
                data = new LinkedList<>();
            }

            if (mAdapter == null) mAdapter = new PersonalAdapter();
            mAdapter.data = data;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_personal_list, container, false);

        mTaskList = rootView.findViewById(R.id.personal_list_rv);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getContext().getDrawable(R.drawable.list_divider));
        mTaskList.addItemDecoration(itemDecoration);
        mTaskList.setLayoutManager(new LinearLayoutManager(getContext()));

        mTaskList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mEmptyView = rootView.findViewById(R.id.personal_empty_tv);

        updateEmpty();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            Parcelable[] stored = savedInstanceState.getParcelableArray(PERS_TASK_LIST);

            if (stored != null) {
                Task[] temp_arr = Arrays.copyOf(stored, stored.length, Task[].class);

                if (mAdapter == null) {
                    mAdapter = new PersonalAdapter();

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
        outState.putParcelableArray(PERS_TASK_LIST, store);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        FragmentManager fm = getFragmentManager();
        Fragment parent = fm.findFragmentById(R.id.main_task_lists);

        if (parent != null) {
            if (parent instanceof OnPersonalFragListener) {
                mListener = (OnPersonalFragListener) parent;
            } else {
                throw new RuntimeException(parent.toString()
                        + " must implement OnPersonalFragListener");
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

    class PersonalTaskViewHolder extends RecyclerView.ViewHolder {

        private View item;
        private TextView title;
        private TextView date;
        private TextView desc;
        private CheckBox comp;

        private LinearLayout tags_layout;

        PersonalTaskViewHolder(View itemView) {
            super(itemView);

            item = itemView;
            title = itemView.findViewById(R.id.task_title);
            date = itemView.findViewById(R.id.task_date);
            desc = itemView.findViewById(R.id.task_desc);
            comp = itemView.findViewById(R.id.task_checkbox);

            tags_layout = itemView.findViewById(R.id.task_tags_ll);

            comp.setOnCheckedChangeListener(this::onCompleteChanged);
            item.setOnClickListener(this::onItemClick);
        }

        void onCompleteChanged(CompoundButton buttonView, boolean isChecked) {
            int pos = getAdapterPosition();
            Task curr = mAdapter.data.get(pos);

            curr.setComplete(isChecked);
            mListener.onPersonalTaskCheckClick(curr);
        }

        void onItemClick(View v) {
            int pos = getAdapterPosition();
            Task curr = mAdapter.data.get(pos);

            mListener.onPersonalTaskClick(curr);
        }
    }

    public class PersonalAdapter extends RecyclerView.Adapter<PersonalTaskViewHolder> {

        // Array list of the tasks
        protected List<Task> data;

        PersonalAdapter() {
            data = new LinkedList<>();
        }

        public void update(List<Task> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public PersonalTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            return new PersonalTaskViewHolder(item);
        }

        @Override
        public void onBindViewHolder(PersonalTaskViewHolder holder, int position) {
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

    public interface OnPersonalFragListener {
        void onPersonalTaskCheckClick(Task task);
        void onPersonalTaskClick(Task task);
    }
}
