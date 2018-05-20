package com.caudelldevelopment.udacity.capstone.household.household.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.caudelldevelopment.udacity.capstone.household.household.R;
import com.caudelldevelopment.udacity.capstone.household.household.data.Task;
import com.caudelldevelopment.udacity.capstone.household.household.data.User;
import com.caudelldevelopment.udacity.capstone.household.household.service.MyResultReceiver;
import com.caudelldevelopment.udacity.capstone.household.household.service.TaskIntentService;
import com.caudelldevelopment.udacity.capstone.household.household.service.UserIntentService;
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

public class PersonalWidgetRemoteViewsService extends RemoteViewsService {

    private static final String LOG_TAG = PersonalWidgetRemoteViewsService.class.getSimpleName();

    public static final String PERS_WIDGET_ERR_MSG = "household.widget.PersonalWidgetRemoteViewsService.PERS_WIDGET_ERR_MSG";
    public static final String PERS_EMPTY_LIST_TAG = "household.widget.PersonalWidgetRemoteViewsService.PERS_EMPTY_LIST_TAG";
    public static final String PERS_WIDGET_UPDATE  = "household.widget.PersonalWidgetRemoteViewsService.PERS_WIDGET_UPDATE";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PersonalRemoteViewsFactory(this);
    }

    public class PersonalRemoteViewsFactory implements RemoteViewsFactory, MyResultReceiver.Receiver {

        private Context mContext;
        private User    mUser;
        private List<Task>  mTasks;

        private MyResultReceiver mUserResults;
        private MyResultReceiver mTaskResults;
        private HandlerThread mHandlerThread;

        PersonalRemoteViewsFactory(Context context) {
            mContext = context;
        }

        @Override
        public void onCreate() {
            Log.v(LOG_TAG, "onCreate has been started!!!!");
            doUserQuery();
        }

        @Override
        public void onDataSetChanged() {
            if (mUser != null) {
                doTaskQuery();
            } else {
                doUserQuery();
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
                if (mUserResults == null) {
                    mUserResults = new MyResultReceiver(new Handler());
                    mUserResults.setReceiver(this);
                }

                UserIntentService.startUserFetch(mContext, mUserResults, fireUser.getUid());
            }
        }

        private void doTaskQuery() {
            if (mTasks == null || mTasks.isEmpty()) {
                mHandlerThread = new HandlerThread("personal_task_intent_service");
                mHandlerThread.start();

                mTaskResults = new MyResultReceiver(new Handler(mHandlerThread.getLooper()));
                mTaskResults.setReceiver(this);

                TaskIntentService.startAllTasksFetch(mContext, mTaskResults, mUser.getId(), true);
            }
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            Log.v(LOG_TAG, "onReceiveResult - resultCode: " + resultCode);
            switch (resultCode) {
                case UserIntentService.USER_SERVICE_RESULT_CODE:
                    User temp_user = resultData.getParcelable(User.DOC_TAG);
                    if (temp_user != null) {
                        mUser = temp_user;
                        doTaskQuery();
                    }
                    break;
                case TaskIntentService.PERSONAL_TASK_SERVICE_RESULT_CODE:
                    // We completed the fetch, so set this to null so we can start another one.
                    // This causes the widget to update constantly, but without it the list shows empty
                    // after switching lists.
//                    mHandlerThread.quitSafely();
//                    mTaskResults.setReceiver(null);
//                    mTaskResults = null;

                    List<Task> task_list = Task.convertParcelableArray(resultData.getParcelableArray(Task.COL_TAG));

                    if (task_list == null || task_list.isEmpty()) {
                        updateWidgetError(getString(R.string.widget_empty_text));
                    } else  {
                        mTasks = task_list;
                        updateWidget();
                    }

                    break;
            }
        }

        private void updateWidgetError(String message) {
            Intent intent = new Intent(mContext, TasksWidget.class);
            intent.setAction(PERS_EMPTY_LIST_TAG);
            intent.putExtra(PERS_WIDGET_ERR_MSG, message);
            sendBroadcast(intent);
        }

        @Override
        public void onDestroy() {
            mTasks = null;

            // This probably will have issues if this doesn't get stopped. Not sure if it happens automatically.
            if (mHandlerThread != null && mHandlerThread.isAlive()) {
                mHandlerThread.quit();
            }
        }

        @Override
        public int getCount() {
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
