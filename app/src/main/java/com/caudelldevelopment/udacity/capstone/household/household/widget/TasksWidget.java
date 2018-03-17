package com.caudelldevelopment.udacity.capstone.household.household.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.caudelldevelopment.udacity.capstone.household.household.R;

import java.util.Date;

public class TasksWidget extends AppWidgetProvider {

    private static final String LOG_TAG = TasksWidget.class.getSimpleName();

    public static final String PERS_CLICK_TAG = "personal_button_clicked";
    public static final String FAM_CLICK_TAG = "family_button_clicked";
    public static final String SWAP_CLICK_TAG = "swap_button_clicked";
    public static final String WIDGET_DATA_UPDATED = "app_widget_data_updated_intent_tag";
    public static final String IS_PERSONAL = "persomal_boolean_service_intent_tag";

//    private boolean personal;

    public TasksWidget() {
//        personal = true;
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tasks_widget);

        views.setTextViewText(R.id.widget_date_tv, "11/02-11/08");
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

//    private String getDateRange() {
//        String result;
//
//        Date today = System.n
//
//        return result;
//    }

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

        Log.v(LOG_TAG, "onReceive - intent.getAction: " + intent.getAction());


        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(context, getClass()));

        if (PERS_CLICK_TAG.equals(intent.getAction())) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tasks_widget);

            views.setViewVisibility(R.id.widget_swap_btn_pers, View.GONE);
            views.setViewVisibility(R.id.widget_swap_btn_fam, View.VISIBLE);
            views.setRemoteAdapter(R.id.widget_task_list, getRemoteAdapterIntent(context, false));

            manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_task_list);
            manager.updateAppWidget(widgetIds, views);
        } else if (FAM_CLICK_TAG.equals(intent.getAction())) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.tasks_widget);

            views.setViewVisibility(R.id.widget_swap_btn_fam, View.GONE);
            views.setViewVisibility(R.id.widget_swap_btn_pers, View.VISIBLE);
            views.setRemoteAdapter(R.id.widget_task_list, getRemoteAdapterIntent(context, true));

            manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_task_list);
            manager.updateAppWidget(widgetIds, views);
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_task_list);
        }
    }

    private Intent getRemoteAdapterIntent(Context context, boolean personal) {
        Intent intent;

        // I tried this app using one RemoteViewsService, but I couldn't figure out how
        // to pass the personal boolean to it. If I could, I would have just let the
        // RemoteViewsService handle the logic and save code. This intent doesn't get sent
        // to the RemoteViewsService though, and I don't have an instance so I ended up
        // making one RemoteViewsService for Personal tasks and Family tasks.
        if (personal) {
            intent = new Intent(context, PersonalWidgetRemoteViewsService.class);
        } else {
            intent = new Intent(context, FamilyWidgetRemoteViewsService.class);
        }

        return intent;
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

