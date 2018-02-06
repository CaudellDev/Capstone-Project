package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.pchmn.materialchips.ChipView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFamilyFragListener} interface
 * to handle interaction events.
 * Use the {@link FamilyListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FamilyListFragment extends Fragment implements EventListener<QuerySnapshot> {

    private static final String LOG_TAG = FamilyListFragment.class.getSimpleName();
    private static final String FAML_TASK_LIST = "family_task_list";

    private List<Task> data;
    private RecyclerView mTaskList;
    private FamilyAdapter mAdapter;
    private boolean search;

    private OnFamilyFragListener mListener;

    public FamilyListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FamilyListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FamilyListFragment newInstance(List<Task> data) {
        FamilyListFragment fragment = new FamilyListFragment();

        Bundle args = new Bundle();

        Task[] task_arr = new Task[data.size()];
        task_arr = data.toArray(task_arr);
        args.putParcelableArray(FAML_TASK_LIST, task_arr);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = new LinkedList<>();

        if (getArguments() != null) {
            Task[] task_arr = (Task[]) getArguments().getParcelableArray(FAML_TASK_LIST);
            data = new LinkedList<>(Arrays.asList(task_arr));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_family_list, container, false);

        search = false;
        mTaskList = rootView.findViewById(R.id.family_list_rv);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getContext().getDrawable(R.drawable.list_divider));
        mTaskList.addItemDecoration(itemDecoration);
        mTaskList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new FamilyAdapter();
        mAdapter.data = data;
        mTaskList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        // This should initialize the list, in addition to updating changes.
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("tasks")
//                .whereEqualTo("family", true)
//                .addSnapshotListener(this);

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Fragment parent = getParentFragment();

        if (parent != null) {
            if (parent instanceof OnFamilyFragListener) {
                mListener = (OnFamilyFragListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnListsFragmentListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void addTask(Task task) {
        data.add(task);
        if (mAdapter != null) {
            mAdapter.data = data;
            mAdapter.notifyDataSetChanged();
        }
    }

    public void setData(List<Task> data) {
        this.data = data;
        if (mAdapter != null) {
            mAdapter.data = this.data;
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
        // Check if there was an exception first...
        if (e != null) {
            Log.w(LOG_TAG, "Firebase listener - onEvent, exception: " + e);
            e.printStackTrace();
            return;
        }

        for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    DocumentSnapshot added = dc.getDocument();
                    Task added_task = added.toObject(Task.class);
                    Log.v(LOG_TAG, "Firebase listener - onEvent, curr task added: " + added_task.getName() + ", " + added_task.getDesc());
                    data.add(added_task);
                    break;
                case REMOVED:
                    DocumentSnapshot removed = dc.getDocument();
                    Task removed_task = removed.toObject(Task.class);
                    data.remove(removed_task);
                    break;
                case MODIFIED:
                    // TODO

                    DocumentSnapshot modified = dc.getDocument();
                    Task mod_task = modified.toObject(Task.class);

                    Task old_ind = data.get(dc.getOldIndex());
                    Task new_ind = data.get(dc.getNewIndex());

                    Log.v(LOG_TAG, "Firebase listener - onEvent, task modified. " +
                            "Old task " + dc.getOldIndex() +
                            ", new task " + dc.getNewIndex() +
                            ", mod task: " + old_ind.getName() +
                            ", " + new_ind.getName() +
                            ", " + mod_task.getName());

                    // Probably didn't change positions?
                    data.set(dc.getOldIndex(), mod_task);
                    break;
                default:
                    Log.w(LOG_TAG, "onEvent - document change type not recognized! dc.getType: " + dc.getType());
            }
        }

        mAdapter.data = data;
        mAdapter.notifyDataSetChanged();
    }

    private class FamilyTaskViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView date;
        private TextView desc;
        private CheckBox comp;
        private LinearLayout tags_layout;

        public FamilyTaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.task_title);
            date = itemView.findViewById(R.id.task_date);
            desc = itemView.findViewById(R.id.task_desc);
            comp = itemView.findViewById(R.id.task_checkbox);
//            tags = itemView.findViewById(R.id.task_tags_ci);

            tags_layout = itemView.findViewById(R.id.task_tags_ll);
        }
    }

    public class FamilyAdapter extends RecyclerView.Adapter<FamilyTaskViewHolder> {

        // Array list of the tasks
        protected List<Task> data;

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

            for (int i = 0; i < curr.getTag_ids().size(); i++) {
//                holder.tags.addChip(curr.getTag(i), "");

                // Trying to avoid using a ChipInput. The user doesn't need to type here
                // and it would improve performance to have a ViewGroup with ChipViews.
                ChipView chipView = new ChipView(getContext());
                chipView.setLabel(curr.getTag(i));
                chipView.setPadding(4, 4, 4, 4);

                holder.tags_layout.addView(chipView);
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
    public interface OnFamilyFragListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();

        void onFamilyFragAttach();
    }
}
