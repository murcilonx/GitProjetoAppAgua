package com.example.projetoappagua

import android.app.Service
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.IBinder
import android.os.Handler
import android.os.Looper

class FlashlightService : Service() {

    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isFlashing = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        flashLight()

        return START_NOT_STICKY
    }

    private fun flashLight() {
        handler.post(object : Runnable {
            override fun run() {
                if (isFlashing) {
                    turnOffFlashlight()
                } else {
                    turnOnFlashlight()
                }
                isFlashing = !isFlashing
                handler.postDelayed(this, 500) // 500ms delay between flashes
            }
        })
    }

    private fun turnOnFlashlight() {
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, true)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun turnOffFlashlight() {
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, false)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        turnOffFlashlight()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
