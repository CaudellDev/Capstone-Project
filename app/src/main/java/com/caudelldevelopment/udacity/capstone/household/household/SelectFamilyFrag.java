package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Inflater;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnSelectFamilyListener} interface
 * to handle interaction events.
 * Use the {@link SelectFamilyFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectFamilyFrag extends Fragment {

    private static final String LOG_TAG = SelectFamilyFrag.class.getSimpleName();
    private static final String SEL_FAM_LIST = "select_family_list";

    private SelectAdapter mAdapter;
    private OnSelectFamilyListener mListener;

    public SelectFamilyFrag() {
        // Required empty public constructor
    }

    public static SelectFamilyFrag newInstance(List<Family> data) {
        SelectFamilyFrag result = new SelectFamilyFrag();

        Family[] temp_list;

        if (data == null) {
            temp_list = new Family[0];
        } else {
            temp_list = new Family[data.size()];
            temp_list = data.toArray(temp_list);
        }

        Bundle args = new Bundle();
        args.putParcelableArray(SEL_FAM_LIST, temp_list);

        result.setArguments(args);

        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_family, container, false);

        RecyclerView mFamilyList = rootView.findViewById(R.id.family_select_list_rv);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getContext().getDrawable(R.drawable.list_divider));
        mFamilyList.addItemDecoration(itemDecoration);
        mFamilyList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new SelectAdapter();
        mFamilyList.setAdapter(mAdapter);

        if (getArguments() != null) {
            Bundle args = getArguments();
            Parcelable[] temp_arr = args.getParcelableArray(SEL_FAM_LIST);

            if (temp_arr != null) {
                List<Family> fam_arr = new LinkedList<>();
                for (Parcelable curr : temp_arr) {
                    fam_arr.add((Family) curr);
                }

                setData(fam_arr);

            } else {
                Log.w(LOG_TAG, "onCreate - Parcelable array was null! args contains SEL_FAM_LIST: " + args.containsKey(SEL_FAM_LIST));
            }
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        TaskListsFragment parent = (TaskListsFragment) getFragmentManager().findFragmentById(R.id.main_task_lists);

        if (parent != null) {
            mListener = parent;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSelectFamilyListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setData(List<Family> data) {
        Log.v(LOG_TAG, "setData has started!!! ");
        if (mAdapter != null) {
            mAdapter.data = data;
            mAdapter.notifyDataSetChanged();
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
    public interface OnSelectFamilyListener {
        void onFamilyItemClick(Family family);
    }

    public class SelectHolder extends RecyclerView.ViewHolder {

        public TextView mFamilyName;
        public TextView mFamilyCount;

        public SelectHolder(View itemView) {
            super(itemView);

            mFamilyName = itemView.findViewById(R.id.family_item_name);
            mFamilyCount = itemView.findViewById(R.id.family_item_count);

            View root = itemView.findViewById(R.id.family_item_layout);
            root.setOnClickListener(v -> {
                Family family = mAdapter.data.get(getAdapterPosition());
                Log.v(LOG_TAG, "SelectHolder constructor, item onClick - family: " + family.getName() + ", member 0: " + family.getMembers().get(0));
                mListener.onFamilyItemClick(family);
            });
        }
    }

    public class SelectAdapter extends RecyclerView.Adapter<SelectHolder> {

        List<Family> data;

        public SelectAdapter() {
            data = new LinkedList<>();
        }

        @Override
        public SelectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(getContext()).inflate(R.layout.family_list_item, parent, false);
            return new SelectHolder(rootView);
        }

        @Override
        public void onBindViewHolder(SelectHolder holder, int position) {
            Family curr = data.get(position);

            holder.mFamilyName.setText(curr.getName());
            holder.mFamilyCount.setText(getString(R.string.family_item_count,  + curr.getMembers().size()));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
