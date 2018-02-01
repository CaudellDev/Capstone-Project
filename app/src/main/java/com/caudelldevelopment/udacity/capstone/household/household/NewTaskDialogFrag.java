package com.caudelldevelopment.udacity.capstone.household.household;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;

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
    private boolean accessIdChange;
    private NewTaskDialogListener mListener;

    private EditText mName;
    private Switch mFamily;
    private EditText mDate;
    private Button mDatePickerBtn;
    private EditText mDesc;
    private ChipsInput mTagInput;

    public static NewTaskDialogFrag newInstance(boolean family, User user, @Nullable Task task) {

        Bundle args = new Bundle();

        args.putBoolean("family", family);
        args.putParcelable("user", user);

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
        accessIdChange = false;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tags")
            .get()
            .addOnCompleteListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof NewTaskDialogListener) {
            mListener = (NewTaskDialogListener) context;
        } else {
            // Throw exception...
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.new_task_dialog_frag, null, false);

        mName = rootView.findViewById(R.id.dialog_name_tv);
        mFamily = rootView.findViewById(R.id.dialog_family_switch);
        mDate = rootView.findViewById(R.id.dialog_date_tv);
        mDatePickerBtn = rootView.findViewById(R.id.dialog_date_btn);
        mDesc = rootView.findViewById(R.id.dialog_desc_tv);
        mTagInput = rootView.findViewById(R.id.dialog_tag_ci);


        Bundle args = getArguments();

        User user = args.getParcelable("user");
        if (user.getFamily() == null || user.getFamily().equals("")) {
            mFamily.setEnabled(false);
        }

        // Automatically set it based on the selected tab.
        mFamily.setChecked(args.getBoolean("family"));

        if (args.containsKey(Task.TAG)) {
            mTask = (Task) args.get(Task.TAG);
            updateViews();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("New Task")
                .setView(rootView)
                .setPositiveButton("SAVE", (dialog, which) -> {
                    Log.v(LOG_TAG, "onCreateDialog - positive button click. Name: " + mName.getText());
                    Task newTask = new Task();
                    newTask.setName(mName.getText().toString());
                    newTask.setDesc(mDesc.getText().toString());
                    newTask.setDateStr(mDate.getText().toString());
                    newTask.setFamily(mFamily.isChecked());

                    List<? extends ChipInterface> tags = mTagInput.getSelectedChipList();
                    for (ChipInterface tag : tags) {
                        newTask.addTag_id(tag.getLabel());
                    }

                    // If mTask was already set, the dialog was to edit it.
                    // Make sure the other values carry over and aren't set to default.
                    if (mTask != null) {
                        newTask.setComplete(mTask.isComplete());
                        // If this is different, we need to change the access id to the right one.
                        if (newTask.isFamily() != mTask.isFamily()) {
                            // Get user data....
                            accessIdChange = true;
                            if (newTask.isFamily()) {
                                newTask.setAccess_id(user.getFamily());
                            } else {
                                newTask.setAccess_id(user.getId());
                            }
                        } else {
                            accessIdChange = false;
                            newTask.setAccess_id(mTask.getAccess_id());
                        }
                    } else {
                        // New task is simple. Just one or the other.
                        if (newTask.isFamily()) {
                            newTask.setAccess_id(user.getFamily());
                        } else {
                            newTask.setAccess_id(user.getId());
                        }
                    }

                    mTask = newTask;
                    mListener.onDialogPositiveClick(mTask);
                }).setNeutralButton("TEST", (dialog, which) -> {
            Log.v(LOG_TAG, "onCreateDialog - neutral button click.");
                }).setNegativeButton("DISMISS", (dialog, which) -> {
                    Log.v(LOG_TAG, "onCreateDialog - negative button click.");
                    mListener.onDialogNegativeClick();
                });

        return builder.create();
    }

    public Task getTask() {
        return mTask;
    }

    public boolean isAccessIdDiff() {
        return accessIdChange;
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
        void onDialogPositiveClick(Task task);
        void onDialogNegativeClick();
    }
}