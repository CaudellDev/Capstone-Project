package com.caudelldevelopment.udacity.capstone.household.household.widget;

import android.appwidget.AppWidgetManager;
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

import java.util.List;

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

    public class FamilyRemoteViewsFactory implements RemoteViewsFactory, MyResultReceiver.Receiver {

        private Context mContext;
        private User   mUser;
        private List<Task> mTasks;

        private MyResultReceiver mUserResults;
        private MyResultReceiver mTaskResults;
        private HandlerThread mHandlerThread;

        public FamilyRemoteViewsFactory(Context context) {
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
                if (mUserResults == null) {
                    mUserResults = new MyResultReceiver(new Handler());
                    mUserResults.setReceiver(this);
                }

                UserIntentService.startUserFetch(mContext, mUserResults, fireUser.getUid());
            }
        }

        /**
         * This will check if a query has started. If it hasn't, start one.
         * Then check if the query is done. This will get called once the broadcast is sent.
         */
        private void doTaskQuery() {
            if (mTaskResults == null) {
                mHandlerThread = new HandlerThread("family_task_intent_service");
                mHandlerThread.start();

                mTaskResults = new MyResultReceiver(new Handler(mHandlerThread.getLooper()));
                mTaskResults.setReceiver(this);

                TaskIntentService.startAllTasksFetch(mContext, mTaskResults, mUser.getFamily(), true);
            }
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case UserIntentService.USER_SERVICE_RESULT_CODE:
                    User temp_user = resultData.getParcelable(User.DOC_TAG);
                    if (temp_user != null) {
                        mUser = temp_user;
                        doTaskQuery();
                    }
                    break;
                case TaskIntentService.FAMILY_TASK_SERVICE_RESULT_CODE:
                    List<Task> task_list = Task.convertParcelableArray(resultData.getParcelableArray(Task.COL_TAG));

                    if (task_list != null) {
                        mTasks = task_list;
                        updateWidget();
                    } else {
                        updateWidgetError(getString(R.string.widget_empty_text));
                    }

                    break;
            }
        }

        private void updateWidgetError(String message) {
            Intent intent = new Intent(mContext, TasksWidget.class);
            intent.setAction(FAM_EMPTY_LIST_TAG);
            intent.putExtra(FAM_WIDGET_ERR_MSG, message);
            sendBroadcast(intent);
        }

        @Override
        public void onDestroy() {
            mTasks = null;

            if (mHandlerThread != null && mHandlerThread.isAlive()) {
                mHandlerThread.quit();
            }
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
