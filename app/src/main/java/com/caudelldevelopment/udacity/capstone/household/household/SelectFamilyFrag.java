package com.caudelldevelopment.udacity.capstone.household.household;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Inflater;

public class SelectFamilyFrag extends Fragment {

    private static final String LOG_TAG = SelectFamilyFrag.class.getSimpleName();
    private static final String SEL_FAM_LIST = "select_family_list";

    private RecyclerView mFamiliesList;
    private SelectAdapter mAdapter;

    private OnSelectFamilyListener mListener;

    public SelectFamilyFrag() {
        // Required empty public constructor
    }

    public static SelectFamilyFrag newInstance(List<Family> data) {
        SelectFamilyFrag fragment = new SelectFamilyFrag();
        Bundle args = new Bundle();

        if (data != null) {
            Family[] temp_list = new Family[data.size()];
            temp_list = data.toArray(temp_list);
            args.putParcelableArray(SEL_FAM_LIST, temp_list);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_family, container, false);

        mFamiliesList = rootView.findViewById(R.id.family_select_list_rv);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getContext().getDrawable(R.drawable.list_divider));
        mFamiliesList.addItemDecoration(itemDecoration);
        mFamiliesList.setLayoutManager(new LinearLayoutManager(getContext()));

        if (mAdapter == null) {
            mAdapter = new SelectAdapter();
        }
        mFamiliesList.setAdapter(mAdapter);

        if (getArguments() != null) {
            Parcelable[] temp_arr = getArguments().getParcelableArray(SEL_FAM_LIST);

            if (temp_arr != null) {
                Family[] fam_arr = Arrays.copyOf(temp_arr, temp_arr.length, Family[].class);
                List<Family> data = new LinkedList<>(Arrays.asList(fam_arr));
                setData(data);
            }
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            Parcelable[] stored = savedInstanceState.getParcelableArray(SEL_FAM_LIST);

            if (stored != null) {
                 Family [] select_arr = Arrays.copyOf(stored, stored.length, Family[].class);

                 if (mAdapter == null) {
                     mAdapter = new SelectAdapter();

                     if (mFamiliesList != null) {
                         mFamiliesList.setAdapter(mAdapter);
                     }
                 }

                 mAdapter.update(Arrays.asList(select_arr));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Family[] select_arr = new Family[0];
        select_arr = mAdapter.data.toArray(select_arr);

        Parcelable[] store;
        store = Arrays.copyOf(select_arr, select_arr.length, Parcelable[].class);

        outState.putParcelableArray(SEL_FAM_LIST, store);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Fragment parent = getFragmentManager().findFragmentById(R.id.main_task_lists);

        if (parent != null) {
            if (parent instanceof OnSelectFamilyListener) {
                mListener = (OnSelectFamilyListener) parent;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnSelectFamilyListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setData(List<Family> data) {
        if (mAdapter != null) {
            mAdapter.data = data;
            mAdapter.notifyDataSetChanged();
        }
    }

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

        SelectAdapter() {
            data = new LinkedList<>();
        }

        public void update(List<Family> data) {
            this.data = data;
            notifyDataSetChanged();
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
