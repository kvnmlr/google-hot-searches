package com.android;

import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class MyWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        /* check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
            return;
        }

        /* We can't trust the appWidgetIds param here, as we're using
           ACTION_APPWIDGET_UPDATE to trigger our own updates, and
           Widgets might've been removed/added since the alarm was last set. */
        final int[] currentIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, MyWidgetProvider.class));

        if (currentIds.length < 1) {
            return;
        }

        Intent iService = new Intent(context, WebShotService.class);
        context.startService(iService);
    }
}