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

/**
 * Created by caude on 2/21/2018.
 */

public class BaseEntryDialog extends DialogFragment {

    private static final String LOG_TAG = BaseEntryDialog.class.getSimpleName();

    private static final String ENTRY = "entry_key";
    public static final String ENTRY_FAMILY = "family_entry";
    public static final String ENTRY_TAG = "tag_entry";
    public static final String DIALOG_TAG = "new_family_dialog";

    private EntryDialogListener mListener;
    private EditText mName;
    private Button mPosConf;
    private Button mNegConf;

    public static BaseEntryDialog getInstance(String entry) {
        BaseEntryDialog dialog = new BaseEntryDialog();

        Bundle args = new Bundle();
        args.putString(ENTRY, entry);
        dialog.setArguments(args);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = null;

        if (getShowsDialog()) {
            rootView = super.onCreateView(inflater, container, savedInstanceState);
        } else {
            rootView = inflater.inflate(R.layout.dialog_frag_container, container, false);
            initViews(rootView);
        }

        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String entry = getArguments().getString(ENTRY);
        String title = "";
        DialogInterface.OnClickListener listener;

        // We can add to this if we need.
        switch(entry) {
            case ENTRY_FAMILY:
                title = getString(R.string.new_family_title);
                break;
            case ENTRY_TAG:
                title = getString(R.string.new_tag_title);
//                listener = (dialog, which) -> {
//                    String name = mName.getText().toString().trim();
//                    mListener.onEntrySave(name);
//                };
                break;
            default:
                listener = (dialog, which) -> {};
                Log.w(LOG_TAG, "Entry is not recognized!!!!");
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.base_entry_dialog, null, false);

        mName = rootView.findViewById(R.id.base_title_et);


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title)
                .setView(rootView)
                .setPositiveButton(R.string.dialog_confirm_pos, (dialog, which) -> {
                    String name = mName.getText().toString().trim();
                    mListener.onEntrySave(name);
                }).setNegativeButton(R.string.dialog_confirm_neg, (dialog, which) -> {

                });

        return builder.create();
    }

    public void initViews(View rootView) {
        mName = rootView.findViewById(R.id.base_title_et);

        if (!getShowsDialog()) {
            mPosConf = rootView.findViewById(R.id.dialog_container_pos_btn);
            mNegConf = rootView.findViewById(R.id.dialog_container_neg_btn);

            mPosConf.setOnClickListener(v -> {
                String name = mName.getText().toString().trim();
                mListener.onEntrySave(name);
            });

            mNegConf.setOnClickListener(v -> {});
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof EntryDialogListener) {
            mListener = (EntryDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement EntryDialogListener");
        }
    }

    public interface EntryDialogListener {
        void onEntrySave(String name);
    }
}
