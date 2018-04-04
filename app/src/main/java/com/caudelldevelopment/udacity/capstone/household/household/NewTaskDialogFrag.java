package com.caudelldevelopment.udacity.capstone.household.household;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pchmn.materialchips.ChipView;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by caude on 1/23/2018.
 */

public class NewTaskDialogFrag extends DialogFragment
                                implements OnCompleteListener<QuerySnapshot>,
                                            View.OnClickListener,
                                            Dialog.OnClickListener {

    private static final String LOG_TAG = NewTaskDialogFrag.class.getSimpleName();

    public static final String DIALOG_TAG = "new_task_dialog";

    private Task mTask;
    private List<Tag> mTags;
    private List<Chip> mChips;
    private boolean accessIdChange;
    private NewTaskDialogListener mListener;

    private EditText mName;
    private Switch mFamily;
    private EditText mDate;
//    private Button mDatePickerBtn;
    private EditText mDesc;
    private ChipsInput mTagInput;

    private Button mPosConf;
    private Button mNegConf;
    private Button mNeuConf;

    private ImageButton mDatePicker;

    public static NewTaskDialogFrag newInstance(boolean family, List<Tag> all_tags, User user, @Nullable Task task) {

        Bundle args = new Bundle();

        args.putBoolean(Family.DOC_TAG, family);
        args.putParcelable(User.DOC_TAG, user);

        Parcelable[] tag_arr = new Parcelable[all_tags.size()];
        tag_arr = all_tags.toArray(tag_arr);
        args.putParcelableArray("all_tags", tag_arr);

        // Task will usually be null. Will be used to handle ListItemClicks to edit existing tags.
        if (task != null) args.putParcelable(Task.TAG, task);

        NewTaskDialogFrag fragment = new NewTaskDialogFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



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

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy has started!!!");
        super.onDestroy();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

//        if (savedInstanceState != null) {
//            String[] chip_labels = savedInstanceState.getStringArray("sel_tags");
//
//            if (chip_labels != null && chip_labels.length > 0) {
//                for (String label : chip_labels) {
//                    mTagInput.addChip(new Chip(label, ""));
//                }
//            }
//        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            String[] chip_labels = savedInstanceState.getStringArray("sel_tags");

            if (chip_labels != null && chip_labels.length > 0) {
                for (String label : chip_labels) {
                    mTagInput.addChip(new Chip(label, ""));
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        List<? extends ChipInterface> sel_chips = mTagInput.getSelectedChipList();
        String[] chip_labels = new String[sel_chips.size()];
        for (int i = 0; i < sel_chips.size(); i++) {
            chip_labels[i] = sel_chips.get(i).getLabel();
        }

        outState.putStringArray("sel_tags", chip_labels);

        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = null;

        if (getShowsDialog()) {
            rootView = super.onCreateView(inflater, container, savedInstanceState);
        } else {
            super.onCreateView(inflater, container, savedInstanceState);

            rootView = inflater.inflate(R.layout.dialog_frag_container, container, false);
            initViews(rootView);
        }

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.new_task_dialog_frag, null, false);

        initViews(rootView);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(rootView)
                .setPositiveButton("SAVE", this)
                .setNegativeButton("DISMISS", this);

        // If the user selected a task, this will nto be null and will allow them to delete the task.
        if (mTask != null) {
            builder.setNeutralButton("DELETE TASK", this);
        }

        return builder.create();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    public void setTask(Task task) {
        mTask = task;
        updateViews();
    }

    public Task getTask() {
        return mTask;
    }

    public void removeTask() {
        mTask = null;
        updateViews();
    }

    public boolean isAccessIdDiff() {
        return accessIdChange;
    }

    private void initViews(View rootView) {
        mName = rootView.findViewById(R.id.dialog_name_tv);
        mFamily = rootView.findViewById(R.id.dialog_family_switch);
        mDate = rootView.findViewById(R.id.dialog_date_tv);
        mDatePicker = rootView.findViewById(R.id.dialog_date_imgbtn);
        mDesc = rootView.findViewById(R.id.dialog_desc_tv);
        mTagInput = rootView.findViewById(R.id.dialog_tag_ci);

        mDate.setEnabled(false);
        mDatePicker.setOnClickListener(this);

        Bundle args = getArguments();

        Parcelable[] temp_arr = getArguments().getParcelableArray("all_tags");

        mTags = new LinkedList<>();
        if (temp_arr != null && temp_arr.length > 0) {
            Tag[] tag_arr = Arrays.copyOf(temp_arr, temp_arr.length, Tag[].class);
            mTags = new LinkedList<>(Arrays.asList(tag_arr));
        }

        for (Tag tag : mTags) {
            Chip tag_chip = new Chip(tag.getName(), "");
            mChips.add(tag_chip);
        }

        mTagInput.setFilterableList(mChips);

        // Disable the family switch if they haven't selected a family yet.
        User user = args.getParcelable(User.DOC_TAG);
        if (user != null) {
            if (user.getFamily() == null || user.getFamily().isEmpty()) {
                mFamily.setEnabled(false);
            } else {
                // The user can open the dialog in the Family Tab, but this won't change if they don't have a Family.
                mFamily.setChecked(args.getBoolean(Family.DOC_TAG));
            }
        }

        // We have to manually manage the buttons without the AlertDialog.
        if (!getShowsDialog()) {
            mPosConf = rootView.findViewById(R.id.dialog_container_pos_btn);
            mNegConf = rootView.findViewById(R.id.dialog_container_neg_btn);
            mNeuConf = rootView.findViewById(R.id.dialog_container_neu_btn);

            mPosConf.setOnClickListener(v -> {
                onClick(null, Dialog.BUTTON_POSITIVE);
            });

            mNegConf.setOnClickListener(v -> {
                onClick(null, Dialog.BUTTON_NEGATIVE);
            });

            mNeuConf.setOnClickListener(v -> {
                onClick(null, Dialog.BUTTON_NEUTRAL);
            });
        }

        // Fill the views with the values of the Task
        if (args.containsKey(Task.TAG)) {
            mTask = (Task) args.get(Task.TAG);
            updateViews();
        }

        // We only need this when starting an animation.
        if (!getShowsDialog()) {
            mListener.onFragmentReady();
        }
    }

    private void updateViews() {
        if (mTask != null) {
            // Update the views
            mName.setText(mTask.getName());
            mFamily.setChecked(mTask.isFamily());
            mDate.setText(mTask.getDate());
            mDesc.setText(mTask.getDesc());


            if (mTags != null && !mTags.isEmpty()) {
                for (String tag_id : mTask.getTag_ids()) {
                    Log.v(LOG_TAG, "updateViews, tag loop - tag: " + tag_id);

                    for (Tag curr : mTags) {
                        if (curr.getId().equals(tag_id)) {
                            mTagInput.addChip(curr.getName(), null);
                            break;
                        }
                    }
                }
            }

            // If there's a task and using container, manually show delete task button.
            if (!getShowsDialog()) {
                mNeuConf.setVisibility(View.VISIBLE);
            }
        } else {
            // Empty all of the views in the dialog

            // Make sure there is no delete button, if there's no Task to delete
            if (!getShowsDialog()) {
                mNeuConf.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
        List<Tag> tags = new LinkedList<>();
        List<Chip> chips = new LinkedList<>();

        for (DocumentSnapshot doc : task.getResult()) {
            Tag curr = Tag.fromDoc(doc);
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
        if (v.getId() == R.id.dialog_date_imgbtn) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                DatePickerDialog pickerDialog = new DatePickerDialog(getContext());
                pickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth);
                    String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(cal.getTime());
                    mDate.setText(date);
                });

                pickerDialog.show();
            } else {
                Date current = Calendar.getInstance().getTime();
                String today = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(current);
                mDate.setText(today);

                // Do something else... ?
            }

        } else {
            Log.w(LOG_TAG, "onClick - view id not recognized. id: " + v.getId());
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Bundle args = getArguments();

        User user = args.getParcelable("user");
        if (user.getFamily() == null || user.getFamily().equals("")) {
            mFamily.setEnabled(false);
        }

        // Automatically set it based on the selected tab.
        mFamily.setChecked(args.getBoolean("family"));

        switch (which) {
            case Dialog.BUTTON_POSITIVE:

                Log.v(LOG_TAG, "onCreateDialog - positive button click. Name: " + mName.getText());
                Task newTask = new Task();
                newTask.setName(mName.getText().toString());
                newTask.setDesc(mDesc.getText().toString());
                newTask.setDate(mDate.getText().toString());
                newTask.setFamily(mFamily.isChecked());

                List<? extends ChipInterface> tags = mTagInput.getSelectedChipList();
                for (ChipInterface chip : tags) {
                    for (Tag tag : mTags) {
                        if (tag.getName().equals(chip.getLabel())) {
                            newTask.addTag_id(tag.getId());
                        }
                    }
                }

                // If mTask was already set, the dialog was to edit it.
                // Make sure the other values carry over and aren't set to default.
                if (mTask != null) {
                    newTask.setId(mTask.getId());
                    newTask.setComplete(mTask.isComplete());
                    // If this is different, we need to change the access id to the right one.
                    if (newTask.isFamily() != mTask.isFamily()) {
                        Log.v(LOG_TAG, "onClick - access id has changed. Task is now " + (newTask.isFamily() ? "Family" : "Personal"));
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

                break;
            case Dialog.BUTTON_NEGATIVE:
                mListener.onDialogNegativeClick();
                break;
            case Dialog.BUTTON_NEUTRAL:
                AlertDialog.Builder conf_builder = new AlertDialog.Builder(getContext());
                conf_builder.setTitle("Are you sure?")
                        .setMessage("You are about to delete task " + mTask.getName() + ". Are you sure?") // Not using the edit field text because those changes haven't been saved.
                        .setPositiveButton("DELETE", (conf_dialog, conf_which) -> {
                            Log.v(LOG_TAG, "onCreateDialog, confirm delete positive click listener - deleting task: " + mTask.getName());
                            FirebaseFirestore db_task = FirebaseFirestore.getInstance();
                            db_task.collection(Task.COL_TAG)
                                    .document(mTask.getId())
                                    .delete();
                        }).setNegativeButton("CANCEL", (conf_dialog, conf_which) -> {

                });

                conf_builder.create().show();
                break;
            default:
                Log.w(LOG_TAG, "onClick - id is not recognized: " + which);
        }
    }

    public interface NewTaskDialogListener {
        void onDialogPositiveClick(Task task);
        void onDialogNegativeClick();
        void onFragmentReady();
        void deleteTask(Task task);
    }
}