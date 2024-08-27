import android.app.Service
import android.content.Intent
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

@Suppress("DEPRECATION")
class FlashlightService : Service() {

    private var isFlashing = false
    private lateinit var handler: Handler
    private var cameraId: String? = null
    private lateinit var cameraManager: CameraManager

    private val flashRunnable = object : Runnable {
        override fun run() {
            if (isFlashing) {
                try {
                    if (cameraId != null) {
                        cameraManager.setTorchMode(cameraId!!, true)
                        Handler().postDelayed({
                            cameraManager.setTorchMode(cameraId!!, false)
                            handler.postDelayed(this, 500) // Piscar a cada 500ms
                        }, 500) // Piscar por 500ms
                    }
                } catch (e: CameraAccessException) {
                    Log.e("FlashlightService", "Erro ao acessar a câmera", e)
                }
            } else {
                // Desligar a lanterna se não estiver piscando
                try {
                    if (cameraId != null) {
                        cameraManager.setTorchMode(cameraId!!, false)
                    }
                } catch (e: CameraAccessException) {
                    Log.e("FlashlightService", "Erro ao desligar a lanterna", e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler = Handler(Looper.getMainLooper())
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        // Tenta obter um cameraId válido
        try {
            val cameraIdList = cameraManager.cameraIdList
            if (cameraIdList.isNotEmpty()) {
                cameraId = cameraIdList.first()
            } else {
                Log.e("FlashlightService", "Nenhuma câmera encontrada")
            }
        } catch (e: CameraAccessException) {
            Log.e("FlashlightService", "Erro ao acessar a lista de câmeras", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                "START_FLASHLIGHT" -> {
                    if (!isFlashing) {
                        isFlashing = true
                        handler.post(flashRunnable)
                    }
                }
                "STOP_FLASHLIGHT" -> {
                    isFlashing = false
                    stopSelf() // Para o serviço se solicitado
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isFlashing = false
        handler.removeCallbacks(flashRunnable)
        try {
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId!!, false)
            }
        } catch (e: CameraAccessException) {
            Log.e("FlashlightService", "Erro ao desligar a lanterna", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
