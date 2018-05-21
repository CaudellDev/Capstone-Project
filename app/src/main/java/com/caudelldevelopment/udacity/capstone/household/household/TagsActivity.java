package com.caudelldevelopment.udacity.capstone.household.household;

import android.os.Bundle;
import android.os.Handler;
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
import com.caudelldevelopment.udacity.capstone.household.household.service.MyResultReceiver;
import com.caudelldevelopment.udacity.capstone.household.household.service.TagIntentService;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TagsActivity extends AppCompatActivity implements BaseEntryDialog.EntryDialogListener, MyResultReceiver.Receiver {

    private static final String LOG_TAG = TagsActivity.class.getSimpleName();

    private TagAdapter mAdapter;
    private MyResultReceiver mResults;

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
            dialog.show(getSupportFragmentManager(), BaseEntryDialog.DIALOG_TAG);
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

        if (mResults == null) {
            mResults = new MyResultReceiver(new Handler());
            mResults.setReceiver(this);
        }

        TagIntentService.startTagWrite(this, mResults, name);
    }

    @Override
    public void onFragmentReady() {
        // Do nothing, for wide layout only
    }

    @Override
    public void onEntryDialogClose() {
        // Do nothing, for wide layout only
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == TagIntentService.TAG_WRITE_SERVICE_RESULT_CODE) {
            Tag new_tag = resultData.getParcelable(Tag.DOC_TAG);

            if (new_tag != null) {
                mAdapter.data.add(new_tag);
                mAdapter.notifyDataSetChanged();
            }
        }
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
