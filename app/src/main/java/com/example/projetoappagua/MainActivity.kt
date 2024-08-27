@file:Suppress("FunctionName")

package com.example.projetoappagua

import NotificationReceiver
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.projetoappagua.model.CalcularIngestaoDiaria
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random


@Suppress(
    "PrivatePropertyName"
)
class MainActivity : AppCompatActivity() {

    private lateinit var edit_peso: EditText
    private lateinit var edit_idade: EditText
    private lateinit var bt_calcular: Button
    private lateinit var txt_resultado_ml: TextView
    private lateinit var ic_redefinir_dados: ImageView
    private lateinit var bt_lembrete: Button
    private lateinit var bt_alarme: Button
    private lateinit var txt_hora: TextView
    private lateinit var txt_minutos: TextView

    private lateinit var calcular_Ingestao_Diaria: CalcularIngestaoDiaria
    private var resultadoMl = 0.0

    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var calendario: Calendar
    private var horaAtual = 0
    private var minutosAtuais = 0


    @SuppressLint("SetTextI18n", "DefaultLocale", "QueryPermissionsNeeded")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        //supportActionBar!!.hide() -> crash do aplicativo

        IniciarComponentes()
        calcular_Ingestao_Diaria = CalcularIngestaoDiaria()

        bt_calcular.setOnClickListener{
            if(edit_peso.text.toString().isEmpty()){
                Toast.makeText(this,R.string.toast_informe_peso,Toast.LENGTH_SHORT).show()
            }else if (edit_idade.text.toString().isEmpty()){
                Toast.makeText(this,R.string.toast_informe_idade,Toast.LENGTH_SHORT).show()

            }else{
                val peso = edit_peso.text.toString().toDouble()
                val idade = edit_idade.text.toString().toInt()
                calcular_Ingestao_Diaria.CalcularTotalMl(peso,idade)
                resultadoMl = calcular_Ingestao_Diaria.ResultadoMl()
                val formatar = NumberFormat.getNumberInstance(Locale("pt","BR"))
                formatar.isGroupingUsed = false
                txt_resultado_ml.text = formatar.format(resultadoMl) + " " + "ml"
            }

        }

        ic_redefinir_dados.setOnClickListener{

            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(R.string.dialog_titulo)
                .setMessage(R.string.dialog_desc)
                    .setPositiveButton("OK") { dialogInterface, i ->
                        edit_peso.setText("")
                        edit_idade.setText("")
                        txt_resultado_ml.text = ""

                    }

            alertDialog.setNegativeButton("Cancelar") { dialogInterface, i ->

            }

            val dialog = alertDialog.create()
            dialog.show()


        }

        bt_lembrete.setOnClickListener{
            calendario = Calendar.getInstance()
            horaAtual = calendario.get(Calendar.HOUR_OF_DAY)
            minutosAtuais = calendario.get(Calendar.MINUTE)
            timePickerDialog = TimePickerDialog(this,{timePicker:TimePicker, hourOfDay: Int, minutes: Int ->
                txt_hora.text = String.format("%02d" , hourOfDay)
                txt_minutos.text = String.format("%02d", minutes)

                val currentTime = Calendar.getInstance()
                currentTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                currentTime.set(Calendar.MINUTE, minutes)
                currentTime.set(Calendar.SECOND, 0)

                val alarmTimeMillis = currentTime.timeInMillis
                val delayMillis = alarmTimeMillis - System.currentTimeMillis()


            //Agendar notificações

            if (delayMillis > 0){

                val notificationTimes = listOf(5*60*1000L, 10*60*1000L, 15*60*1000L, 30*60*1000L)
                for (delay in notificationTimes){
                    scheduleNotification(this, alarmTimeMillis-delay)
                }

            } else{
                Toast.makeText(this, "A hora definida ja passou", Toast.LENGTH_SHORT).show()
            }

            },horaAtual,minutosAtuais,true)
            timePickerDialog.show()

        }


        bt_alarme.setOnClickListener{

            if (txt_hora.text.toString().isNotEmpty() && txt_minutos.text.toString().isNotEmpty()){
                val intent = Intent(AlarmClock.ACTION_SET_ALARM)
                intent.putExtra(AlarmClock.EXTRA_HOUR, txt_hora.text.toString().toInt())
                intent.putExtra(AlarmClock.EXTRA_MINUTES, txt_minutos.text.toString().toInt())
                intent.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarme_mensagem))
                startActivity(intent)

                    if (intent.resolveActivity(packageManager) != null){
                        startActivity(intent)
                    }

            }

        }

    }

        @SuppressLint("ScheduleExactAlarm", "SuspiciousIndentation")
        fun scheduleNotification (context: Context, triggerTimeMillis: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt(1000)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channelId = "default_channel_id"
                val channelName = "Default Channel"
                val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)

        }

        val notificationBuilder = NotificationCompat.Builder(context, "default_channel_id")
            .setContentTitle("Lembrete de Hidratação")
            .setContentText("A hora da hidratação está próxima")
            .setSmallIcon(R.drawable.bebaagua)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val activityIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            notificationBuilder.setContentIntent(pendingIntent)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        val pendingNotificationIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingNotificationIntent)
    }

    private fun checkPermissions(){
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            val notificationPermission = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            if (notificationPermission != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissão de notificações não concedida", Toast.LENGTH_SHORT).show()
            }

        }
    }


    @SuppressLint("CutPasteId")
    fun IniciarComponentes(){
        edit_peso = findViewById(R.id.edit_peso)
        edit_idade = findViewById(R.id.edit_idade)
        bt_calcular = findViewById(R.id.bt_calcular)
        txt_resultado_ml = findViewById(R.id.txt_resultado_ml)
        ic_redefinir_dados = findViewById(R.id.ic_redefinir)
        bt_lembrete = findViewById(R.id.bt_definir_lembretee)
        bt_alarme = findViewById(R.id.bt_definir_alarme)
        txt_hora = findViewById(R.id.text_hora)
        txt_minutos = findViewById(R.id.text_minutos)



    }



}