package com.caudelldevelopment.udacity.capstone.household.household;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TagsActivity extends AppCompatActivity implements BaseEntryDialog.EntryDialogListener {

    private static final String LOG_TAG = TagsActivity.class.getSimpleName();

    private TagAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        Toolbar toolbar = findViewById(R.id.tabs_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            BaseEntryDialog dialog = BaseEntryDialog.getInstance(BaseEntryDialog.ENTRY_TAG);
            dialog.show(getSupportFragmentManager(), "base_entry_dialog");
        });

        RecyclerView mTagList = findViewById(R.id.tag_list_rv);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(this.getDrawable(R.drawable.list_divider));
        mTagList.addItemDecoration(itemDecoration);
        mTagList.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new TagAdapter();
        mTagList.setAdapter(mAdapter);

        Parcelable[] temp_arr = getIntent().getParcelableArrayExtra("all_tags");

        if (temp_arr != null && temp_arr.length > 0) {
            Tag[] task_arr = Arrays.copyOf(temp_arr, temp_arr.length, Tag[].class);
            mAdapter.data = new LinkedList<>(Arrays.asList(task_arr));
        } else {
            Log.w(LOG_TAG, "onCreate - List of tags could not be retrieved.");
            mAdapter.data = new LinkedList<>();
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // For some reason, without this manual finish on back
            // MainActivity gets recreated causing a NullPointerException
            // on mUser.
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEntrySave(String name) {
        Tag tag = new Tag(name);

        if (mAdapter.data.contains(tag)) {
            Snackbar.make(findViewById(R.id.tabs_app_bar_layout), R.string.duplicate_tag_msg, Snackbar.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Tags won't be modified or removed, so this should always be true.
        if (tag.getId() == null) {
            DocumentReference tagRef = db.collection(Tag.COL_TAG).document();
            tag.setId(tagRef.getId());
        }

        db.collection(Tag.COL_TAG)
            .document(tag.getId())
            .set(tag.toMap())
            .addOnSuccessListener(Void -> {
                mAdapter.data.add(tag);
                mAdapter.notifyDataSetChanged();

                Snackbar.make(findViewById(R.id.tag_list_layout), getString(R.string.add_tag_success_msg, tag.getName()), Snackbar.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Snackbar.make(findViewById(R.id.tag_list_layout), R.string.add_tag_failure_msg, Snackbar.LENGTH_LONG).show();
            });
    }

    @Override
    public void onFragmentReady() {
        // Do nothing, for wide layout only
    }

    @Override
    public void onEntryDialogClose() {
        // Do nothing, for wide layout only
    }

    private class TagViewHolder extends RecyclerView.ViewHolder {

        public TextView title;

        public TagViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tag_list_title);
        }
    }

    private class TagAdapter extends RecyclerView.Adapter<TagViewHolder> {

        public List<Tag> data;

        @Override
        public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_list_item, parent, false);
            return new TagViewHolder(item);
        }

        @Override
        public void onBindViewHolder(TagViewHolder holder, int position) {
            Tag curr = data.get(position);

            holder.title.setText(curr.getName());
        }

        @Override
        public int getItemCount() {
            return (data != null) ? data.size() : 0;
        }
    }
}
