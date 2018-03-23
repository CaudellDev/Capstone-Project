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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnPersonalFragListener} interface
 * to handle interaction events.
 * Use the {@link PersonalListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalListFragment extends Fragment implements OnCompleteListener<QuerySnapshot>, OnFailureListener {

    private static final String LOG_TAG = PersonalListFragment.class.getSimpleName();
    private static final String PERS_TASK_LIST = "parsonal_task_list";

    private List<Task> data;
    private RecyclerView mTaskList;
    private PersonalAdapter mAdapter;
    private TaskListsFragment mListener;

    public PersonalListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param data Parameter 1.
     * @return A new instance of fragment PersonalListFragment.
     */
    public static PersonalListFragment newInstance(List<Task> data) {
        PersonalListFragment fragment = new PersonalListFragment();
        Bundle args = new Bundle();

        Task[] task_arr = new Task[data.size()];
        task_arr = data.toArray(task_arr);
        args.putParcelableArray(PERS_TASK_LIST, task_arr);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = new LinkedList<>();

        if (getArguments() != null) {
            Parcelable[] temp_arr = getArguments().getParcelableArray(PERS_TASK_LIST);

            if (temp_arr != null && temp_arr.length > 0) {
                Task[] task_arr = Arrays.copyOf(temp_arr, temp_arr.length, Task[].class);
                data = new LinkedList<>(Arrays.asList(task_arr));
            } else {
                Log.w(LOG_TAG, "onCreate - List of tasks could not be retrieved.");
                data = new LinkedList<>();
            }
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

        mAdapter = new PersonalAdapter();
        mAdapter.data = data;
        mTaskList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            Log.v(LOG_TAG, "onActivityCreated - restoring fragment state.");

            Parcelable[] stored = savedInstanceState.getParcelableArray(PERS_TASK_LIST);

            if (stored != null) {
                Task[] temp_arr;
                temp_arr = Arrays.copyOf(stored, stored.length, Task[].class);
                data = Arrays.asList(temp_arr);

                if (mAdapter == null) mAdapter = new PersonalAdapter();
                mAdapter.data = data;
                mAdapter.notifyDataSetChanged();
            }

            // Get array from bundle
            // Create adapter and give it the data
            // Scroll position?
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Task[] temp_arr = new Task[0];
        temp_arr = data.toArray(temp_arr);

        Parcelable[] store;
        store = Arrays.copyOf(temp_arr, temp_arr.length, Parcelable[].class);
        outState.putParcelableArray(PERS_TASK_LIST, store);

        // Keep scroll position?
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        FragmentManager fm = getFragmentManager();
        Fragment parent = fm.findFragmentById(R.id.main_task_lists);

        if (parent != null) {
            if (parent instanceof TaskListsFragment) {
                mListener = (TaskListsFragment) parent;
            } else {
                throw new RuntimeException(parent.toString()
                        + " must implement OnListsFragmentListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setData(List<Task> data) {
        Log.v(LOG_TAG, "setDate - mAdapter == null: " + (mAdapter == null));

        this.data = data;
        if (mAdapter != null) {
            mAdapter.data = this.data;
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
        Log.v(LOG_TAG, "onComplete has started!!!");
        if (task.isSuccessful()) {
            data = new LinkedList<>();
            for (DocumentSnapshot doc : task.getResult()) {
                Task curr = Task.fromDoc(doc, mListener.getUser());
                data.add(curr);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {

    }

    public class PersonalTaskViewHolder extends RecyclerView.ViewHolder
                                        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
        private View item;
        private TextView title;
        private TextView date;
        private TextView desc;
        private CheckBox comp;

        private LinearLayout tags_layout;

        public PersonalTaskViewHolder(View itemView) {
            super(itemView);
            item = itemView;
            title = itemView.findViewById(R.id.task_title);
            date = itemView.findViewById(R.id.task_date);
            desc = itemView.findViewById(R.id.task_desc);
            comp = itemView.findViewById(R.id.task_checkbox);
            tags_layout = itemView.findViewById(R.id.task_tags_ll);

            comp.setOnCheckedChangeListener(this);
            item.setOnClickListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int pos = getAdapterPosition();
            Task curr = mAdapter.data.get(pos);

            curr.setComplete(isChecked);

            mListener.onPersonalTaskCheckClick(curr, pos);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Task curr = mAdapter.data.get(pos);

            Log.v(LOG_TAG, "onClick - task, pos: " + curr.getName() + ", " + pos);

            mListener.onPersonalTaskClick(curr, pos);
        }
    }

    public class PersonalAdapter extends RecyclerView.Adapter<PersonalTaskViewHolder> {

        // Array list of the tasks
        protected List<Task> data;

        @Override
        public PersonalTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            return new PersonalTaskViewHolder(item);
        }

        @Override
        public void onBindViewHolder(PersonalTaskViewHolder holder, int position) {
            Task curr = data.get(position);

            Log.v(LOG_TAG, "PersonalAdapter, onBindViewHolder - task count: " + data.size());
            Log.v(LOG_TAG, "PersonalAdapter, onBindViewHolder - task at " + position + " is null: " + (curr == null));

            holder.title.setText(curr.getName());
            holder.date.setText(curr.getDate());
            holder.desc.setText(curr.getDesc());
            holder.comp.setChecked(curr.isComplete());

            for (int i = 0; i < curr.getTag_ids().size(); i++) {
                // Check if the tag already exists before adding
                ChipView check = (ChipView) holder.tags_layout.getChildAt(i);
                if (check != null && check.getLabel().equals(curr.getTag(i))) {
                    continue;
                }

                ChipView tag = new ChipView(getContext());
                tag.setLabel(curr.getTag(i));
                tag.setPadding(4, 4, 4, 4);
                holder.tags_layout.addView(tag);
            }
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
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
    public interface OnPersonalFragListener {
        void onPersonalTaskCheckClick(Task task, int pos);
        void onPersonalTaskClick(Task task, int pos);
    }
}
