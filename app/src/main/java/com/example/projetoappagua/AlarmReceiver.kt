package com.example.projetoappagua

import FlashlightService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

@Suppress("unused")
class AlarmReceiver : BroadcastReceiver() {

    private var isAlarmActive = true

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val serviceIntent = Intent(context, FlashlightService::class.java)
        serviceIntent.action = "START_FLASHLIGHT"
        context.startService(serviceIntent)

        when (action) {
            "com.example.projetoappagua.START_FLASHLIGHT" -> {
                serviceIntent.putExtra("ACTION", "START")
                context.startService(serviceIntent)
            }
            "com.example.projetoappagua.STOP_FLASHLIGHT" -> {
                serviceIntent.putExtra("ACTION", "STOP")
                context.startService(serviceIntent)
            }
        }
    }





}
