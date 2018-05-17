package com.caudelldevelopment.udacity.capstone.household.household.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.caudelldevelopment.udacity.capstone.household.household.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TasksWidget extends AppWidgetProvider {

    private static final String LOG_TAG = TasksWidget.class.getSimpleName();

    public static final String PERS_CLICK_TAG = "household.widget.TasksWidget.PERS_CLICK_TAG";
    public static final String FAM_CLICK_TAG  = "household.widget.TasksWidget.FAM_CLICK_TAG";
    public static final String IS_PERSONAL = "persomal_boolean_service_intent_tag";

    private Intent mPersIntent;
    private Intent mFamIntent;

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tasks_widget);

        views.setTextViewText(R.id.widget_date_tv, getDateRange(context));
        views.setOnClickPendingIntent(R.id.widget_swap_btn_pers, getPendingSelfIntent(context, true));
        views.setOnClickPendingIntent(R.id.widget_swap_btn_fam,  getPendingSelfIntent(context, false));
        views.setRemoteAdapter(R.id.widget_task_list, getRemoteAdapterIntent(context, true)); // Default is personal

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private PendingIntent getPendingSelfIntent(Context context, boolean personal) {
        Intent intent = new Intent(context, getClass());

        if (personal) {
            intent.setAction(PERS_CLICK_TAG);
        } else {
            intent.setAction(FAM_CLICK_TAG);
        }

        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private String getDateRange(Context context) {
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();

        cal.add(Calendar.DATE, 7);
        Date in_week = cal.getTime();

        String today_str = new SimpleDateFormat("MM/dd", Locale.US).format(today);
        String week_str  = new SimpleDateFormat("MM/dd", Locale.US).format(in_week);

        return context.getString(R.string.dialog_date_range, today_str, week_str);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tasks_widget);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        String action = intent.getAction();
        Log.v(LOG_TAG, "onReceive - action: " + action);


        if (action != null) {
            switch (action) {
                case AppWidgetManager.ACTION_APPWIDGET_UPDATE:
                    int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context, getClass()));
                    manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_task_list);
                    break;
                case PERS_CLICK_TAG:
                    doButtonSwap(manager, views, context, false);
                    break;
                case FAM_CLICK_TAG:
                    doButtonSwap(manager, views, context, true);
                    break;
                case PersonalWidgetRemoteViewsService.PERS_EMPTY_LIST_TAG:
                case FamilyWidgetRemoteViewsService.FAM_EMPTY_LIST_TAG:
                    showEmptyList(manager, views, context);
                    break;
                case PersonalWidgetRemoteViewsService.PERS_WIDGET_UPDATE:
                case FamilyWidgetRemoteViewsService.FAM_WIDGET_UPDATE:
                    doWidgetListUpdate(manager, views, context);
                    break;
            }
        }
    }

    private void doButtonSwap(AppWidgetManager manager, RemoteViews views, Context context, boolean personal) {
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context, getClass()));

        if (personal) {
            views.setViewVisibility(R.id.widget_swap_btn_fam, View.GONE);
            views.setViewVisibility(R.id.widget_swap_btn_pers, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_swap_btn_pers, View.GONE);
            views.setViewVisibility(R.id.widget_swap_btn_fam, View.VISIBLE);
        }

        views.setRemoteAdapter(R.id.widget_task_list, getRemoteAdapterIntent(context, personal));

        // This doesn't update the list when I change the access for a task.
        // I have to click the button twice and then the task will show up.
        manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_task_list);
        manager.updateAppWidget(widgetIds, views);
    }

    private void showEmptyList(AppWidgetManager manager, RemoteViews views, Context context) {
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context, getClass()));

        views.setViewVisibility(R.id.widget_task_list, View.GONE);
        views.setViewVisibility(R.id.widget_empty_tv, View.VISIBLE);

        manager.updateAppWidget(widgetIds, views);
    }

    private void doWidgetListUpdate(AppWidgetManager manager, RemoteViews views, Context context) {
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context, getClass()));

        views.setViewVisibility(R.id.widget_empty_tv, View.GONE);
        views.setViewVisibility(R.id.widget_task_list, View.VISIBLE);

        manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_task_list);
    }

    private Intent getRemoteAdapterIntent(Context context, boolean personal) {
        // I tried this app using one RemoteViewsService, but I couldn't figure out how
        // to pass the personal boolean to it. If I could, I would have just let the
        // RemoteViewsService handle the logic and save code. This intent doesn't get sent
        // to the RemoteViewsService though, and I don't have an instance so I ended up
        // making one RemoteViewsService for Personal tasks and Family tasks.
        if (personal) {
            if (mPersIntent == null) {
                mPersIntent = new Intent(context, PersonalWidgetRemoteViewsService.class);
            }
            return mPersIntent;
        } else {
            if (mFamIntent == null) {
                mFamIntent = new Intent(context, FamilyWidgetRemoteViewsService.class);
            }
            return mFamIntent;
        }
    }

    @Override public void onEnabled(Context context) {}
    @Override public void onDisabled(Context context) {}
}

