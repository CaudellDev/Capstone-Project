package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.pchmn.materialchips.ChipView;
import com.pchmn.materialchips.ChipsInput;

import java.util.Arrays;
import java.util.Collections;
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
public class PersonalListFragment extends Fragment {

    private static final String LOG_TAG = PersonalListFragment.class.getSimpleName();
    private static final String PERS_TASK_LIST = "parsonal_task_list";

    private List<Task> data;
    private RecyclerView mTaskList;
    private PersonalAdapter mAdapter;

    private OnPersonalFragListener mListener;

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
    // TODO: Rename and change types and number of parameters
    public static PersonalListFragment newInstance(List<Task> data) {
        PersonalListFragment fragment = new PersonalListFragment();
        Bundle args = new Bundle();

        Log.v(LOG_TAG, "newInstance - data.size: " + data.size());
        for (Task curr : data) {
            Log.v(LOG_TAG, "newInstance - curr task.title: " + curr.getTitle());
        }

        Task[] task_arr = new Task[data.size()];
        task_arr = data.toArray(task_arr);
        args.putParcelableArray(PERS_TASK_LIST, task_arr);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Task[] task_arr = (Task[]) getArguments().getParcelableArray(PERS_TASK_LIST);
            data = new LinkedList<>(Arrays.asList(task_arr));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Fragment parent = getFragmentManager().findFragmentById(R.id.main_task_lists);
        Log.d(LOG_TAG, "onCreateView - parent fragment is null: " + (parent == null));

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
            if (parent instanceof OnPersonalFragListener) {
                mListener = (OnPersonalFragListener) parent;
                mListener.onPersonalFragAttach();
            } else {
                throw new RuntimeException(parent.toString()
                        + " must implement OnListsFragmentListener");
            }
        } else {
            Log.w(LOG_TAG, "onAttach - parent fragment is null!!!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setData(List<Task> data) {
        this.data = data;
        if (mAdapter != null) {
            mAdapter.data = this.data;
            mAdapter.notifyDataSetChanged();
        }
    }

    public class PersonalTaskViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView date;
        private TextView desc;
        private CheckBox comp;
//        private ChipsInput tags;

        private LinearLayout tags_layout;

        public PersonalTaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.task_title);
            date = itemView.findViewById(R.id.task_date);
            desc = itemView.findViewById(R.id.task_desc);
            comp = itemView.findViewById(R.id.task_checkbox);
//            tags = itemView.findViewById(R.id.task_tags_ci);

            tags_layout = itemView.findViewById(R.id.task_tags_ll);
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

            holder.title.setText(curr.getTitle());
            holder.date.setText(curr.getDate());
            holder.desc.setText(curr.getDesc());
            holder.comp.setChecked(curr.isComplete());

            for (int i = 0; i < curr.getTag_ids().size(); i++) {
//                holder.tags.addChip(curr.getTag(i), "");

                // Trying to avoid using a ChipInput. The user doesn't need to type here
                // and it would improve performance to have a ViewGroup with ChipViews.
                ChipView chipView = new ChipView(getContext());
                chipView.setLabel(curr.getTag(i));

                holder.tags_layout.addView(chipView);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
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
        // TODO: Update argument type and name
        void onFragmentInteraction();

        void onPersonalFragAttach();
    }
}
