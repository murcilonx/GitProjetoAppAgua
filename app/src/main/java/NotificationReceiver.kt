@file:Suppress("Annotator", "Annotator", "Annotator", "Annotator", "RedundantSuppression")

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.projetoappagua.R

class NotificationReceiver : BroadcastReceiver(){
    @SuppressLint("NotificationPermission")
    override fun onReceive(context: Context?, intent: Intent?){

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "water_reminder_channel"
        val channelName = "Water Reminder Channel"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val  notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Lembrete de Hidratação")
            .setContentText("A hora de beber água esta se aproximando!")
            .setSmallIcon(R.drawable.bebaagua)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, notificationBuilder.build())

    }
 
    
    
    
    
    
    
}
