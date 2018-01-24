package com.caudelldevelopment.udacity.capstone.household.household;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by caude on 1/23/2018.
 */

public class NewTaskDialogFrag extends DialogFragment implements OnCompleteListener<QuerySnapshot>, View.OnClickListener {

    private static final String LOG_TAG = NewTaskDialogFrag.class.getSimpleName();

    private Task mTask;
    private List<Tag> mTags;
    private List<Chip> mChips;

    private EditText mName;
    private Switch mFamily;
    private EditText mDate;
    private Button mDatePickerBtn;
    private EditText mDesc;
    private ChipsInput mTagInput;

    public static NewTaskDialogFrag newInstance(boolean family, @Nullable Task task) {

        Bundle args = new Bundle();

        args.putBoolean("family", family);

        // Task will usually be null. Will be used to handle ListItemClicks to edit existing tags.
        if (task != null) args.putParcelable(Task.TAG, task);

        NewTaskDialogFrag fragment = new NewTaskDialogFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTags = new LinkedList<>();
        mChips = new LinkedList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tags")
            .get()
            .addOnCompleteListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.new_task_dialog_frag, container, false);

        mName = rootView.findViewById(R.id.dialog_name_tv);
        mFamily = rootView.findViewById(R.id.dialog_family_switch);
        mDate = rootView.findViewById(R.id.dialog_date_tv);
        mDatePickerBtn = rootView.findViewById(R.id.dialog_date_btn);
        mDesc = rootView.findViewById(R.id.dialog_desc_tv);
        mTagInput = rootView.findViewById(R.id.dialog_tag_ci);


        Bundle args = getArguments();

        // Automatically set it based on the selected tab.
        mFamily.setChecked(args.getBoolean("family"));

        if (args.containsKey(Task.TAG)) {
            mTask = (Task) args.get(Task.TAG);
            updateViews();
        }

        return rootView;
    }

    private void updateViews() {
        // Update the views
        mName.setText(mTask.getName());
        mFamily.setChecked(mTask.isFamily());
        mDate.setText(mTask.getDateStr());
        mDate.setOnClickListener(this);
        mDesc.setText(mTask.getDesc());

        mTagInput.removeAllViews();
        for (String tag : mTask.getTag_ids()) {
            mTagInput.addChip(tag, null);
        }
    }

    @Override
    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
        List<Tag> tags = new LinkedList<>();
        List<Chip> chips = new LinkedList<>();

        for (DocumentSnapshot doc : task.getResult()) {
//            Log.v(LOG_TAG, "onCreate - query, document: " + task.getResult().toString() + ", " + doc.getId() + " " + doc.exists());

            Tag curr = doc.toObject(Tag.class);
            tags.add(curr);
            chips.add(new Chip(curr.getName(), null));
        }

        // All of the available tags that the user could assign to the task.
        mTags = tags;
        mChips = chips;

        mTagInput.setFilterableList(mChips);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.dialog_date_btn) {
            // Open calendar chooser dialog then update date edit text.
        } else {
            Log.w(LOG_TAG, "onClick - view id not recognized. id: " + v.getId());
        }
    }


    public interface NewTaskDialogListener {
        void onDialogPositiveClick();
        void onDialogNegativeClick();
    }
}