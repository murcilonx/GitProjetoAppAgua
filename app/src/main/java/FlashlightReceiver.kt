package com.example.projetoappagua

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper

class FlashlightReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId: String = cameraManager.cameraIdList[0] // Assuming the first camera has a flashlight
        val handler = Handler(Looper.getMainLooper())

        for (i in 1..10) {
            handler.postDelayed({
                try {
                    cameraManager.setTorchMode(cameraId, true)
                    handler.postDelayed({ cameraManager.setTorchMode(cameraId, false) }, 500) // Light off for 500ms
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }, (i * 1000).toLong()) // Flash every second
        }
    }
}