@file:Suppress("Annotator", "Annotator", "Annotator", "Annotator", "RedundantSuppression")

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.projetoappagua.R
import kotlin.random.Random

class NotificationReceiver : BroadcastReceiver(){
    @SuppressLint("NotificationPermission")
    override fun onReceive(context: Context, intent: Intent){

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "water_reminder_channel"
        val channelName = "Water Reminder Channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val  notificationBuilder = NotificationCompat.Builder(context, "default_channel_id")
            .setContentTitle("Lembrete de Hidratação")
            .setContentText("A hora da hidratação está próxima")
            .setSmallIcon(R.drawable.bebaagua)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(Random.nextInt(1000), notificationBuilder.build())


    }
 
    
    
    
    
    
    
}
