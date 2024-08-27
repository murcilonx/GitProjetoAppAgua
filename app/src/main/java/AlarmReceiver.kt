package com.example.projetoappagua

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper

class AlarmReceiver : BroadcastReceiver() {

    private var isAlarmActive = true

    override fun onReceive(context: Context, intent: Intent) {

        //Verifica se o alarme esta desligado

        val action = intent.action
        if (action == "com.android.alarmclock.ALARM_ALERT" || action == "android.intent.action.ALARM_CHANGED") {
            isAlarmActive = true
            piscarLanternaContinuamente(context)
        }else if (action == "com.android.alarmclock.ALARM_DISMISS" || action == "android.intent.action.ALARM_DISMISS"){
            isAlarmActive = false
        }

    }

    private fun piscarLanternaContinuamente(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0] // Geralmente a primeira câmera é a traseira
        val handler = Handler(Looper.getMainLooper())
        val intervalo = 500L // Intervalo em milissegundos entre piscadas

        val runnable = object : Runnable {
            @SuppressLint("ObsoleteSdkInt")
            override fun run() {
                if (isAlarmActive) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Liga a lanterna
                            cameraManager.setTorchMode(cameraId, true)
                        }
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }

                    handler.postDelayed({
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                // Desliga a lanterna
                                cameraManager.setTorchMode(cameraId, false)
                            }
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }

                        // Continua piscando enquanto o alarme estiver ativo
                        handler.postDelayed(this, intervalo)
                    }, intervalo)
                }
            }
        }

        handler.post(runnable)
    }
}