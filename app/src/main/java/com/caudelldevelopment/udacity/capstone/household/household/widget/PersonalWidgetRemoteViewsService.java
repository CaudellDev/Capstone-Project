package com.caudelldevelopment.udacity.capstone.household.household.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.caudelldevelopment.udacity.capstone.household.household.R;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by caude on 3/4/2018.
 */

public class PersonalWidgetRemoteViewsService extends RemoteViewsService {

    private static final String LOG_TAG = PersonalWidgetRemoteViewsService.class.getSimpleName();

    public static final String PERS_WIDGET_ERR_MSG = "household.widget.PersonalWidgetRemoteViewsService.PERS_WIDGET_ERR_MSG";
    public static final String PERS_EMPTY_LIST_TAG = "household.widget.PersonalWidgetRemoteViewsService.PERS_EMPTY_LIST_TAG";
    public static final String PERS_WIDGET_UPDATE  = "household.widget.PersonalWidgetRemoteViewsService.PERS_WIDGET_UPDATE";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PersonalRemoteViewsFactory(this);
    }

    public class PersonalRemoteViewsFactory implements RemoteViewsFactory {

        private Context mContext;
        private User    mUser;
        private List<Task>  mTasks;
        private com.google.android.gms.tasks.Task<QuerySnapshot> mQuery;

        public PersonalRemoteViewsFactory(Context context) {
            mContext = context;
        }

        @Override
        public void onCreate() {
            Log.v(LOG_TAG, "onCreate has started!!!");
            doUserQuery();
        }


        @Override
        public void onDataSetChanged() {
            if (mUser != null) {
                doTaskQuery();
            } else {
                Log.w(LOG_TAG, "onDataSetChanged - mUser is null!!!!");
            }
        }

        private void updateWidget() {
            Intent intent = new Intent(mContext, TasksWidget.class);
            intent.setAction(PERS_WIDGET_UPDATE);
            intent.putExtra(TasksWidget.IS_PERSONAL, true);
            sendBroadcast(intent);
        }

        private void doUserQuery() {
            FirebaseUser fireUser = FirebaseAuth.getInstance().getCurrentUser();

            if (fireUser != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection(User.COL_TAG)
                        .document(fireUser.getUid())
                        .addSnapshotListener((documentSnapshot, e) -> {
                            if (e != null) {
                                Log.w(LOG_TAG, "doUserQuery, User SnapshotListener - Firebase Exception: " + e.getMessage());
                                e.printStackTrace();
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                mUser = User.fromDoc(documentSnapshot);
                                Log.v(LOG_TAG, "doUserQuery, User SnapshotListener - user: " + mUser.getId() + ", " + mUser.getName());

                                updateWidget();
                            }
                        });
            }
        }

        private void doTaskQuery() {
            if (mQuery == null) {
                Log.v(LOG_TAG, "doTaskQuery - mQuery is null. Starting data fetch.");


                // Use the user to finally get the list of tasks
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                mQuery = db.collection(Task.COL_TAG)
                        .whereEqualTo(Task.ACCESS_ID, mUser.getId())
                        .orderBy(Task.DATE_ID)
                        .startAt(getDate())
                        .endAt(getDateInWeek())
                        .get()
                        .addOnSuccessListener(task -> updateWidget())
                        .addOnFailureListener(e    -> updateWidgetError(""));
            }

            if (mQuery.isComplete() && mQuery.isSuccessful()) {
                Log.v(LOG_TAG, "doTaskQuery - mQuery is done and successful.");

                mTasks = new LinkedList<>();
                List<DocumentSnapshot> docs = mQuery.getResult().getDocuments();
                for (DocumentSnapshot doc : docs) {
                    Task task = Task.fromDoc(doc, mUser);
                    mTasks.add(task);
                }

                Log.v(LOG_TAG, "doTaskQuery - task count: " + mTasks.size());
                if (mTasks.isEmpty()) {
                    updateWidgetError("No tasks");
                }

                mQuery = null;
            }
        }

        private void updateWidgetError(String message) {
            Log.v(LOG_TAG, "updateWidgetError has started!!!");
            Intent intent = new Intent(mContext, TasksWidget.class);
            intent.setAction(PERS_EMPTY_LIST_TAG);
            intent.putExtra(PERS_WIDGET_ERR_MSG, message);
            sendBroadcast(intent);
        }

        private String getDate() {
            Calendar cal = Calendar.getInstance();
            Date today = cal.getTime();

            return new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(today);
        }

        private String getDateInWeek() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 7);
            Date in_week = cal.getTime();

            return new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(in_week);
        }

        @Override
        public void onDestroy() {
            mTasks = null;
        }

        @Override
        public int getCount() {
//                return 3;
            Log.v(LOG_TAG, "getCount - count: " + (mTasks == null ? 0 : mTasks.size()));
            return mTasks == null ? 0 : mTasks.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {

            if (mTasks == null) return null;

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_task_item);

            Task curr = mTasks.get(position);
            views.setTextViewText(R.id.widget_task_name, curr.getName());

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.widget_task_item);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
