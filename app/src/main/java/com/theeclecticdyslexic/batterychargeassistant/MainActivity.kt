package com.theeclecticdyslexic.batterychargeassistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.theeclecticdyslexic.batterychargeassistant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val CHANNEL_ID = "the notification channel"
        const val NOTIFICATION_ID = 0
    }

    override fun onStart() {
        super.onStart()

        Settings.initSharedPreferences(this)

        // TODO move this to a new class
        initNotificationManager()
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, buildOngoingNotification())
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun initNotificationManager(): NotificationManager {
        val name = getString(R.string.app_name)
        val descriptionText = "Status Notification"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel =
            NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
        // Register the channel with the system
        val manager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return manager
    }
    private fun buildOngoingNotification() : Notification {
        val dismissIntent = Intent(this, NotificationHandler::class.java).apply {

        }

        val pendingDismissIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, 0)

        val chargeLimit = getSharedPreferences(Settings.javaClass.name, MODE_PRIVATE)
            .getInt(Settings.ChargeTarget.javaClass.name, Settings.ChargeTarget.default)

        return NotificationCompat.Builder(this, NotificationCompat.CATEGORY_STATUS)
            .setOngoing(true)
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_edit_24)
            .setContentTitle(getString(R.string.app_name) + " is on")
            .setContentText("current charge limit $chargeLimit%")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .addAction(R.drawable.ic_baseline_cancel_24, "Cancel", pendingDismissIntent)
            .build()
    }
}