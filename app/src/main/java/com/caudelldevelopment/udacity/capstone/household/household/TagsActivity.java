package com.caudelldevelopment.udacity.capstone.household.household;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caudelldevelopment.udacity.capstone.household.household.data.Tag;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.LinkedList;
import java.util.List;

public class TagsActivity extends AppCompatActivity implements BaseEntryDialog.EntryDialogListener {

    private static final String LOG_TAG = TagsActivity.class.getSimpleName();

    private RecyclerView mTagList;
    private TagAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            BaseEntryDialog dialog = BaseEntryDialog.getInstance(BaseEntryDialog.ENTRY_TAG);
            dialog.show(getSupportFragmentManager(), "base_entry_dialog");
        });

        mTagList = findViewById(R.id.tag_list_rv);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(this.getDrawable(R.drawable.list_divider));
        mTagList.addItemDecoration(itemDecoration);
        mTagList.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new TagAdapter();
        mTagList.setAdapter(mAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Tag.COL_TAG)
                .get()
                .addOnCompleteListener(task -> {
                    List<Tag> tags = new LinkedList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Tag curr = doc.toObject(Tag.class);
                        Log.v(LOG_TAG, "onCreate, onCompleteListener - curr tag: " + curr.getName());
                        tags.add(curr);
                    }

                    mAdapter.data = tags;
                    mAdapter.notifyDataSetChanged();
                });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onEntrySave(String name) {
        Tag tag = new Tag(name);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Tag.COL_TAG)
            .document(tag.getId())
            .set(tag.toMap())
            .addOnSuccessListener(Void -> {
                mAdapter.data.add(tag);
                mAdapter.notifyDataSetChanged();

                Snackbar.make(findViewById(R.id.tag_list_layout), "Tag " + name + " has been sucessfully added.", Snackbar.LENGTH_SHORT).show();
            });
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
