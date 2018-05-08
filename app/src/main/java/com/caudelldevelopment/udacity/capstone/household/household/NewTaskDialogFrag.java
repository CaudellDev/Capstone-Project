package com.caudelldevelopment.udacity.capstone.household.household;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
    private EditText mDesc;
    private ChipsInput mTagInput;

    private Button mPosConf;
    private Button mNegConf;
    private Button mNeuConf;

    private ImageButton mDatePicker;

    public static NewTaskDialogFrag newInstance(boolean family, @Nullable List<Tag> all_tags, User user, @Nullable Task task) {

        Bundle args = new Bundle();

        args.putBoolean(Family.DOC_TAG, family);
        args.putParcelable(User.DOC_TAG, user);

        if (all_tags != null) {
            Parcelable[] tag_arr = new Parcelable[all_tags.size()];
            tag_arr = all_tags.toArray(tag_arr);
            args.putParcelableArray("all_tags", tag_arr);
        }

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
        super.onDestroy();
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
        View rootView;

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
                .setPositiveButton(R.string.save_text, this)
                .setNegativeButton(R.string.dismiss_text, this);

        // If the user selected a task, this will nto be null and will allow them to delete the task.
        if (mTask != null) {
            builder.setNeutralButton(R.string.delete_text, this);
        }

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> updateSaveButton());
        return dialog;
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
        } else {
            List<Tag> temp_list = mListener.getAllTags();
            if (temp_list != null) {
                mTags = temp_list;
            }
        }

        if (mTags != null) {
            for (Tag tag : mTags) {
                Chip tag_chip = new Chip(tag.getName(), "");
                mChips.add(tag_chip);
            }

            mTagInput.setFilterableList(mChips);
        }

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

        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateSaveButton();
            }
        });

        mDate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                updateSaveButton();
            }
        });

        // Fill the views with the values of the Task
        if (args.containsKey(Task.TAG)) {
            mTask = (Task) args.get(Task.TAG);
        }

        updateViews();

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
                for (String tag_id : mTask.getTag_ids().keySet()) {
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

        updateSaveButton();
    }

    private void updateSaveButton() {
        boolean name_filled = !mName.getText().toString().trim().isEmpty();
        boolean date_filled = !mDate.getText().toString().trim().isEmpty();

        if (getShowsDialog()) {
            AlertDialog dialog = (AlertDialog) getDialog();
            if (dialog != null) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(name_filled && date_filled);
            }
        } else {
            mPosConf.setEnabled(name_filled && date_filled);
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

        // We only need this when starting the animation in wide layout.
        if (!getShowsDialog()) {
            mListener.onFragmentReady();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.dialog_date_imgbtn) {

            DatePickerDialog pickerDialog;

            DatePickerDialog.OnDateSetListener listener = (datePicker, year, month, dayOfMonth) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(cal.getTime());
                mDate.setText(date);
            };

            Calendar cal = Calendar.getInstance();
            int year;
            int month;
            int dayOfMonth;

            // Check if the date was already selected. We'll open the dialog at that date.
            if (!mDate.getText().toString().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                try {
                    Date date = dateFormat.parse(mDate.getText().toString());
                    cal.setTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

            pickerDialog = new DatePickerDialog(getContext(), listener, year, month, dayOfMonth);
            pickerDialog.show();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Bundle args = getArguments();
        User user = args.getParcelable("user");

        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                Task newTask = new Task();
                newTask.setName(mName.getText().toString().trim());
                newTask.setDesc(mDesc.getText().toString().trim());
                newTask.setDate(mDate.getText().toString());
                newTask.setFamily(mFamily.isChecked());

                List<? extends ChipInterface> tags = mTagInput.getSelectedChipList();
                for (ChipInterface chip : tags) {
                    for (Tag tag : mTags) {
                        if (tag.getName().equals(chip.getLabel())) {
                            newTask.addTag_id(tag.getId(), tag.getName());
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
                mListener.onNewTaskDialogClose();
                break;
            case Dialog.BUTTON_NEUTRAL:
                AlertDialog.Builder conf_builder = new AlertDialog.Builder(getContext());
                conf_builder.setTitle(R.string.are_you_sure_title)
                        .setMessage(getString(R.string.are_you_sure_msg, mTask.getName())) // Not using the edit field text because those changes haven't been saved.
                        .setPositiveButton(R.string.delete_text, (conf_dialog, conf_which) -> {
                            FirebaseFirestore db_task = FirebaseFirestore.getInstance();
                            db_task.collection(Task.COL_TAG)
                                    .document(mTask.getId())
                                    .delete();

                        }).setNegativeButton(R.string.cancel_text, (conf_dialog, conf_which) -> {

                });

                conf_builder.create().show();
                break;
        }
    }

    public interface NewTaskDialogListener {
        void onDialogPositiveClick(Task task);
        void onNewTaskDialogClose();
        void onFragmentReady();
        List<Tag> getAllTags();
    }
}