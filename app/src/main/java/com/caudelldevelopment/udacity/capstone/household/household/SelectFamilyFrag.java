package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class SelectFamilyFrag extends Fragment implements EventListener<QuerySnapshot> {

    private static final String LOG_TAG = SelectFamilyFrag.class.getSimpleName();

    private User mUser;

    private RecyclerView mFamilyList;
    private SelectAdapter mAdapter;
    private TaskListsFragment mListener;

    public SelectFamilyFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectFamilyFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectFamilyFrag newInstance(User user) {
        SelectFamilyFrag fragment = new SelectFamilyFrag();

        Bundle args = new Bundle();
        args.putParcelable(User.DOC_TAG, user);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            mUser = args.getParcelable(User.DOC_TAG);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Family.COL_TAG)
            .addSnapshotListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_family, container, false);

        mFamilyList = rootView.findViewById(R.id.family_select_list_rv);
        mFamilyList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new SelectAdapter();
        mFamilyList.setAdapter(mAdapter);

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

    @Override
    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
        List<Family> familyList = new LinkedList<>();

        for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    DocumentSnapshot added = dc.getDocument();
                    Family added_family = Family.fromDoc(added);
                    familyList.add(added_family);
                    break;
                case MODIFIED:
                    // This could be more efficient, but this will do for now.
                    DocumentSnapshot mod = dc.getDocument();
                    Family mod_family = Family.fromDoc(mod);
                    familyList.add(mod_family);
                    break;
                case REMOVED:
                    // Family members won't be removable for now.
                    break;
                default:
                    Log.w(LOG_TAG, "onEvent - document change not recognized!!! Type: " + dc.getType());
            }
        }

        for (Family curr : familyList) {
            Log.v(LOG_TAG, "onEvent - family: " + curr.getName());
        }

        mAdapter.data = familyList;
        mAdapter.notifyDataSetChanged();
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
