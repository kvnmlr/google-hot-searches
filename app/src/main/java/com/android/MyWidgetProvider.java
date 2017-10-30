package com.android;

import android.app.AlarmManager;
import android.appwidget.AppWidgetProvider;
import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class MyWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_CLICK = "ACTION_CLICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        /** check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(context)) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            /** request permission via start activity for result */
            context.startActivity(intent);
            return;
        }

        // We can't trust the appWidgetIds param here, as we're using
        // ACTION_APPWIDGET_UPDATE to trigger our own updates, and
        // Widgets might've been removed/added since the alarm was last set.
        final int[] currentIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, MyWidgetProvider.class));

        if (currentIds.length < 1) {
            return;
        }

        // We attach the current Widget IDs to the alarm Intent to ensure its
        // broadcast is correctly routed to onUpdate() when our AppWidgetProvider
        // next receives it.
        Intent iWidget = new Intent(context, MyWidgetProvider.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, currentIds);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, iWidget, 0);

        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.RTC, System.currentTimeMillis() + 50000, pi);

        Intent iService = new Intent(context, WebShotService.class);
        context.startService(iService);
    }

    public final static int REQUEST_CODE = -1010101;
}