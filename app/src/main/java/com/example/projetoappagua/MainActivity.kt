@file:Suppress("FunctionName", "unused", "RedundantSuppression")

package com.example.projetoappagua

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.example.projetoappagua.model.CalcularIngestaoDiaria
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Suppress("PrivatePropertyName")
class MainActivity : AppCompatActivity() {

    private lateinit var edit_peso: EditText
    private lateinit var edit_idade: EditText
    private lateinit var bt_calcular: Button
    private lateinit var txt_resultado_ml: TextView
    private lateinit var ic_redefinir: ImageView
    private lateinit var bt_lembrete: Button
    private lateinit var bt_alarme: Button
    private lateinit var text_hora: TextView
    private lateinit var text_minutos: TextView

    private lateinit var calcular_Ingestao_Diaria: CalcularIngestaoDiaria
    private var resultadoMl = 0.0

    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var calendario: Calendar
    private var horaAtual = 0
    private var minutosAtuais = 0

    @SuppressLint("SetTextI18n", "DefaultLocale", "QueryPermissionsNeeded", "ScheduleExactAlarm")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        IniciarComponentes()
        calcular_Ingestao_Diaria = CalcularIngestaoDiaria()

        createNotificationChannel()

        // Verificar permissões para notificações
        if (!checkNotificationPermission()) {
            requestNotificationPermission()
        }

        bt_calcular.setOnClickListener {
            if (edit_peso.text.toString().isEmpty()) {
                Toast.makeText(this, R.string.toast_informe_peso, Toast.LENGTH_SHORT).show()
            } else if (edit_idade.text.toString().isEmpty()) {
                Toast.makeText(this, R.string.toast_informe_idade, Toast.LENGTH_SHORT).show()
            } else {
                val peso = edit_peso.text.toString().toDouble()
                val idade = edit_idade.text.toString().toInt()
                calcular_Ingestao_Diaria.CalcularTotalMl(peso, idade)
                resultadoMl = calcular_Ingestao_Diaria.ResultadoMl()
                val formatar = NumberFormat.getNumberInstance(Locale("pt", "BR"))
                formatar.isGroupingUsed = false
                txt_resultado_ml.text = formatar.format(resultadoMl) + " ml"

                // Salvar a ingestão diária
                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putFloat("daily_intake", resultadoMl.toFloat())
                editor.apply()
            }
        }

        ic_redefinir.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(R.string.dialog_titulo)
                .setMessage(R.string.dialog_desc)
                .setPositiveButton("OK") { dialogInterface, i ->
                    edit_peso.setText("")
                    edit_idade.setText("")
                    txt_resultado_ml.text = ""

                    val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.remove("daily_intake")
                    editor.remove("reminder_time")
                    editor.apply()
                }
                .setNegativeButton("Cancelar") { dialogInterface, i -> }
            val dialog = alertDialog.create()
            dialog.show()
        }

        bt_lembrete.setOnClickListener {
            calendario = Calendar.getInstance()
            horaAtual = calendario.get(Calendar.HOUR_OF_DAY)
            minutosAtuais = calendario.get(Calendar.MINUTE)
            timePickerDialog = TimePickerDialog(this, { timePicker: TimePicker, hourOfDay: Int, minutes: Int ->
                val selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minutes)
                    set(Calendar.SECOND, 0)
                }

                val currentTime = Calendar.getInstance().timeInMillis

                if (selectedTime.timeInMillis <= currentTime) {
                    Toast.makeText(this, "O horário definido já passou.", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                text_hora.text = String.format("%02d", hourOfDay)
                text_minutos.text = String.format("%02d", minutes)

                try {
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this, FlashlightReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, selectedTime.timeInMillis, pendingIntent)

                    // Cancelar notificações antigas, se houver
                    cancelNotifications()

                    scheduleNotification(selectedTime.timeInMillis - 30 * 60 * 1000, 1)
                    scheduleNotification(selectedTime.timeInMillis - 15 * 60 * 1000, 2)
                    scheduleNotification(selectedTime.timeInMillis - 10 * 60 * 1000, 3)
                    scheduleNotification(selectedTime.timeInMillis - 5 * 60 * 1000, 4)

                    // Salvar o horário do lembrete
                    val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.putString("reminder_time", "${String.format("%02d", hourOfDay)}:${String.format("%02d", minutes)}")
                    editor.apply()
                } catch (e: SecurityException) {
                    Toast.makeText(this, "Permissão necessária para configurar alarmes.", Toast.LENGTH_SHORT).show()
                }
            }, horaAtual, minutosAtuais, true)
            timePickerDialog.show()
        }

        bt_alarme.setOnClickListener {
            if (text_hora.text.toString().isNotEmpty() && text_minutos.text.toString().isNotEmpty()) {
                val intent = Intent(AlarmClock.ACTION_SET_ALARM)
                intent.putExtra(AlarmClock.EXTRA_HOUR, text_hora.text.toString().toInt())
                intent.putExtra(AlarmClock.EXTRA_MINUTES, text_minutos.text.toString().toInt())
                intent.putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.alarme_mensagem))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(timeInMillis: Long, notificationId: Int) {
        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("notification_id", notificationId)
        val pendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

    private fun cancelNotifications() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancelAll()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "hydration_channel_id"
            val channelName = "Hydration Notifications"
            val channelDescription = "Channel for hydration reminders"
            val channelImportance = NotificationManager.IMPORTANCE_HIGH

            val notificationChannel = NotificationChannel(channelId, channelName, channelImportance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
        }
    }

    @SuppressLint("CutPasteId")
    private fun IniciarComponentes() {
        edit_peso = findViewById(R.id.edit_peso)
        edit_idade = findViewById(R.id.edit_idade)
        bt_calcular = findViewById(R.id.bt_calcular)
        txt_resultado_ml = findViewById(R.id.txt_resultado_ml)
        ic_redefinir = findViewById(R.id.ic_redefinir)
        bt_lembrete = findViewById(R.id.bt_definir_lembretee)
        bt_alarme = findViewById(R.id.bt_definir_alarme)
        text_hora = findViewById(R.id.text_hora)
        text_minutos = findViewById(R.id.text_minutos)
    }
}
