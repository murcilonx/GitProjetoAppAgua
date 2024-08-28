package com.example.projetoappagua

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper

class FlashlightReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // API 23 e superior
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                private var blinkCount = 0
                override fun run() {
                    if (blinkCount < 10) {
                        // Alterna o estado da lanterna
                        cameraManager.setTorchMode(cameraId, blinkCount % 2 == 0)
                        blinkCount++
                        handler.postDelayed(this, 500)
                    } else {
                        // Desliga a lanterna após 10 piscadas
                        cameraManager.setTorchMode(cameraId, false)
                    }
                }
            }
            handler.post(runnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
