package com.caudelldevelopment.udacity.capstone.household.household;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.v(LOG_TAG, "onAttach - context instance of NewTaskDialogListener: " + (context instanceof NewTaskDialogListener));

        if (context instanceof NewTaskDialogListener) {
            mListener = (NewTaskDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement NewTaskDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = null;

        if (!getShowsDialog()) {
            super.onCreateView(inflater, container, savedInstanceState);

            rootView = inflater.inflate(R.layout.dialog_frag_container, container, false);

            initViews(rootView);

//            CardView cardView = rootView.findViewById(R.id.dialog_container_cv);
//            if (container != null) {
//                Log.v(LOG_TAG, "onCreateView - Starting CardView animation!!!!");
//
//                Animation animation = AnimationUtils.makeInAnimation(getContext(), false);
//                animation.setDuration(5000);
//
//                cardView.startAnimation(animation);
//            }

            Button pos = rootView.findViewById(R.id.dialog_container_pos_btn);
            Button neg = rootView.findViewById(R.id.dialog_container_neg_btn);

            pos.setOnClickListener(v -> {
                Log.v(LOG_TAG, "onCreateView - positive dialog onClick listener");
            });

            neg.setOnClickListener(v -> {
                Log.v(LOG_TAG, "onCreateView - negative dialog onClick listener");
                mListener.onDialogNegativeClick();
            });
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateDialog has started!!!!");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.new_task_dialog_frag, null, false);

        initViews(rootView);

//        mName = rootView.findViewById(R.id.dialog_name_tv);
//        mFamily = rootView.findViewById(R.id.dialog_family_switch);
//        mDate = rootView.findViewById(R.id.dialog_date_tv);
//        mDatePickerBtn = rootView.findViewById(R.id.dialog_date_btn);
//        mDesc = rootView.findViewById(R.id.dialog_desc_tv);
//        mTagInput = rootView.findViewById(R.id.dialog_tag_ci);
//
//        mDate.setEnabled(false);
//        mDatePickerBtn.setOnClickListener(this);
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("tags")
//                .get()
//                .addOnCompleteListener(this);


        // Building dialog starts here below here...
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
                    newTask.setDate(mDate.getText().toString());
                    newTask.setFamily(mFamily.isChecked());

                    List<? extends ChipInterface> tags = mTagInput.getSelectedChipList();
                    for (ChipInterface tag : tags) {
                        newTask.addTag_id(tag.getLabel());
                    }

                    // If mTask was already set, the dialog was to edit it.
                    // Make sure the other values carry over and aren't set to default.
                    if (mTask != null) {
                        newTask.setId(mTask.getId());
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
                }).setNegativeButton("DISMISS", (dialog, which) -> {
                    Log.v(LOG_TAG, "onCreateDialog - negative button click.");
                    mListener.onDialogNegativeClick();
                });

        if (mTask != null) {
            Log.v(LOG_TAG, "onCreateDialog - mTask is not null. Editing task. Adding delete button to dialog.");

            builder.setNeutralButton("DELETE TASK", (dialog, which) -> {
                AlertDialog.Builder conf_builder = new AlertDialog.Builder(getContext());
                conf_builder.setTitle("Are you sure?")
                        .setMessage("You are about to delete task " + mTask.getName() + ". Are you sure?") // Not using the edit field text because those changes haven't been saved.
                        .setPositiveButton("DELETE", (conf_dialog, conf_which) -> {
                            Log.v(LOG_TAG, "onCreateDialog, confirm delete positive click listener - deleting task: " + mTask.getName());
                            FirebaseFirestore db_task = FirebaseFirestore.getInstance();
                            db_task.collection(Task.TASKS_ID)
                                    .document(mTask.getId())
                                    .delete();
                        }).setNegativeButton("CANCEL", (conf_dialog, conf_which) -> {

                        });

                conf_builder.create().show();
            });
        }

        return builder.create();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    public Task getTask() {
        return mTask;
    }

    public boolean isAccessIdDiff() {
        return accessIdChange;
    }

    private void initViews(View rootView) {
        mName = rootView.findViewById(R.id.dialog_name_tv);
        mFamily = rootView.findViewById(R.id.dialog_family_switch);
        mDate = rootView.findViewById(R.id.dialog_date_tv);
        mDatePickerBtn = rootView.findViewById(R.id.dialog_date_btn);
        mDesc = rootView.findViewById(R.id.dialog_desc_tv);
        mTagInput = rootView.findViewById(R.id.dialog_tag_ci);

        mDate.setEnabled(false);
        mDatePickerBtn.setOnClickListener(this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tags")
                .get()
                .addOnCompleteListener(this);
    }

    private void updateViews() {
        // Update the views
        mName.setText(mTask.getName());
        mFamily.setChecked(mTask.isFamily());
        mDate.setText(mTask.getDate());
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

        // We only need this when starting an animation.
        if (!getShowsDialog()) {
            mListener.onFragmentReady();
        }
    }

    @Override
    public void onClick(View v) {
        Log.v(LOG_TAG, "onClick - view clicked. Id: " + v.getId());
        if (v.getId() == R.id.dialog_date_btn) {
            Log.v(LOG_TAG, "onClick - Date button has been clicked. Setting it to today, for now...");
            Date current = Calendar.getInstance().getTime();
            String today = new SimpleDateFormat("mm/DD/yyyy", Locale.US).format(current);
            mDate.setText(today);

            // TODO: Launch dialog with calendar picker widget....
        } else {
            Log.w(LOG_TAG, "onClick - view id not recognized. id: " + v.getId());
        }
    }

    public interface NewTaskDialogListener {
        void onDialogPositiveClick(Task task);
        void onDialogNegativeClick();
        void onFragmentReady();
        void deleteTask(Task task);
    }
}