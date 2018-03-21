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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by caude on 3/14/2018.
 */

public class FamilyWidgetRemoteViewsService extends RemoteViewsService {

    private static final String LOG_TAG = FamilyWidgetRemoteViewsService.class.getSimpleName();

    public static final String FAM_WIDGET_ERR_MSG = "household.widget.FamilyWidgetRemoteViewsService.FAM_WIDGET_ERR_MSG";
    public static final String FAM_EMPTY_LIST_TAG = "household.widget.FamilyWidgetRemoteViewsService.FAM_EMPTY_LIST_TAG";
    public static final String FAM_WIDGET_UPDATE  = "household.widget.FamilyWidgetRemoteViewsService.FAM_WIDGET_UPDATE";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FamilyRemoteViewsFactory(this);
    }

    public class FamilyRemoteViewsFactory implements RemoteViewsFactory {

        private Context mContext;
        private User   mUser;
        private List<Task> mTasks;
        private com.google.android.gms.tasks.Task<QuerySnapshot> mQuery;

        public FamilyRemoteViewsFactory(Context context) {
            mContext = context;
        }

        @Override
        public void onCreate() {
            doUserQuery();
        }

        @Override
        public void onDataSetChanged() {
            if (mUser != null) {
                doTaskQuery();
            } else {
                Log.v(LOG_TAG, "onDataSetChanged - mUser is null!!!!");
            }
        }

        private void updateWidget() {
            Intent intent = new Intent(mContext, TasksWidget.class);
            intent.setAction(FAM_WIDGET_UPDATE);
            intent.putExtra(TasksWidget.IS_PERSONAL, false);
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
                                updateWidgetError(getString(R.string.widget_err_user_fail));
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                mUser = User.fromDoc(documentSnapshot);
                                updateWidget();
                            }
                        });
            }
        }

        /**
         * This will check if a query has started. If it hasn't, start one.
         * Then check if the query is done. This will get called once the broadcast is sent.
         */
        private void doTaskQuery() {
            // Save the query, and when it completes, update the widget.
            if (mQuery == null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                mQuery = db.collection(Task.COL_TAG)
                        .whereEqualTo(Task.ACCESS_ID, mUser.getFamily())
                        .orderBy(Task.DATE_ID)
                        .startAt(getDate())
                        .endAt(getDateInWeek())
                        .get()
                        .addOnSuccessListener(task -> updateWidget())
                        .addOnFailureListener(e    -> updateWidgetError(""));
            }

            // Use the results we got and put them into the array. Once that's done, remove query.
            if (mQuery.isComplete() && mQuery.isSuccessful()) {
                mTasks = new LinkedList<>();
                List<DocumentSnapshot> docs = mQuery.getResult().getDocuments();
                for (DocumentSnapshot doc : docs) {
                    Task task = Task.fromDoc(doc, mUser);
                    mTasks.add(task);
                }

                if (mTasks.isEmpty()) {
                    updateWidgetError("No tasks in this list");
                }

                mQuery = null;
            }
        }

        private void updateWidgetError(String message) {
            Log.v(LOG_TAG, "updateWidgetError has started!!!");
            Intent intent = new Intent(mContext, TasksWidget.class);
            intent.setAction(FAM_EMPTY_LIST_TAG);
            intent.putExtra(FAM_WIDGET_ERR_MSG, message);
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
            return (mTasks == null) ? 0 : mTasks.size();
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
            return null;
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
