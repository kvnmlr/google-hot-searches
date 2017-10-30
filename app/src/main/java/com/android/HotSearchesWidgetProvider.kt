package com.android

import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class HotSearchesWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        /* check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
            context.startActivity(intent)
            return
        }

        /* We can't trust the appWidgetIds param here, as we're using
           ACTION_APPWIDGET_UPDATE to trigger our own updates, and
           Widgets might've been removed/added since the alarm was last set. */
        val currentIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, HotSearchesWidgetProvider::class.java))

        if (currentIds.isEmpty()) {
            return
        }

        val iService = Intent(context, WebShotService::class.java)
        context.startService(iService)
    }
}