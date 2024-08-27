package com.example.projetoappagua

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WaterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Exemplo de configuração
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val nextReminderTime = format.format(Calendar.getInstance().time)

            views.setTextViewText(R.id.widget_text, "Ingestão: 2500 ml") // Exemplo de ingestão
            views.setTextViewText(R.id.widget_reminder, "Próximo lembrete: $nextReminderTime")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
