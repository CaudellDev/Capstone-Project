package com.caudelldevelopment.udacity.capstone.household.household;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.caudelldevelopment.udacity.capstone.household.household.data.Family;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.LinkedList;
import java.util.List;

public class FamilyActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = FamilyActivity.class.getSimpleName();

    public static final String USER_EXTRA = "user_extra";
    public static final String LEFT_FAMILY = "left_fammily";

    private Family mFamily;
    private List<User> mMembers;

    private MembersAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        User mUser = intent.getParcelableExtra(USER_EXTRA);

        if (mUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection(Family.COL_TAG)
                    .document(mUser.getFamily())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        mFamily = Family.fromDoc(documentSnapshot);
                        updateMembersList();
                    });

            db.collection(User.COL_TAG)
                    .whereEqualTo(User.FAMILY_ID, mUser.getFamily())
                    .addSnapshotListener((documentSnapshots, e) -> {
                        List<User> temp_list = new LinkedList<>();
                        for (DocumentSnapshot doc : documentSnapshots.getDocuments()) {
                            User member = User.fromDoc(doc);
                            temp_list.add(member);
                        }

                        mMembers = temp_list;
                        updateMembersList();
                    });
        }

        RecyclerView mMembersList = findViewById(R.id.family_members_list);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(this.getDrawable(R.drawable.list_divider));
        mMembersList.addItemDecoration(itemDecoration);
        mMembersList.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new MembersAdapter();
        mMembersList.setAdapter(mAdapter);

        Button leaveFamily = findViewById(R.id.leave_family_btn);
        leaveFamily.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void updateMembersList() {
        if (mAdapter != null) {
            mAdapter.data = mMembers;
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.leave_family_btn) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.leave_family_conf_msg, mFamily.getName()))
                    .setPositiveButton(R.string.leave_family_conf_btn, (dialog, which) -> leaveFamily())
                    .setNegativeButton(R.string.dismiss_text, (dialog, which) -> {});
            builder.show();
        }
    }

    private void leaveFamily() {
        Intent intent = new Intent();
        intent.putExtra(LEFT_FAMILY, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    class MembersViewHolder extends RecyclerView.ViewHolder {

        TextView memberName;

        public MembersViewHolder(View itemView) {
            super(itemView);

            memberName = itemView.findViewById(R.id.member_name_tv);
        }
    }

    class FamilyViewHolder extends RecyclerView.ViewHolder {

        TextView familyName;

        public FamilyViewHolder(View itemView) {
            super(itemView);

            familyName = itemView.findViewById(R.id.family_name_tv);
        }
    }

    protected class MembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int FAM_VIEW_TYPE = 1;
        private final int MEM_VIEW_TYPE = 2;

        protected List<User> data;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder result;
            View item;

            if (viewType == FAM_VIEW_TYPE) {
                item = LayoutInflater.from(parent.getContext()).inflate(R.layout.family_name_item, parent, false);
                result = new FamilyViewHolder(item);
            } else {
                item = LayoutInflater.from(parent.getContext()).inflate(R.layout.family_member_item, parent, false);
                result = new MembersViewHolder(item);
            }

            return result;
        }

        @SuppressLint("NewApi")
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == FAM_VIEW_TYPE && mFamily != null) {
                FamilyViewHolder famVH = (FamilyViewHolder) holder;
                famVH.familyName.setText(mFamily.getName());
            }

            if (holder.getItemViewType() == MEM_VIEW_TYPE && data != null) {
                MembersViewHolder memVH = (MembersViewHolder) holder;
                User curr = data.get(position - 1);
                memVH.memberName.setText(curr.getName());
            }
        }

        @Override
        public int getItemCount() {
            return (data == null) ? 1 : data.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return FAM_VIEW_TYPE;
            else
                return MEM_VIEW_TYPE;
        }


    }
}
