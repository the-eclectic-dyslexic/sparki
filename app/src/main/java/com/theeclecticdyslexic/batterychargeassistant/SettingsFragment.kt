package com.theeclecticdyslexic.batterychargeassistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.util.Log.INFO
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.theeclecticdyslexic.batterychargeassistant.databinding.SettingsFragmentBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SettingsFragment : Fragment() {

    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {

    }

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        sharedPrefs = requireContext().getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)

        initBatterTargetSeeker()
        initAllSwitches()
        initButtons()

        return binding.root
    }



    private fun initButtons() {
        binding.buttonHttpRequestSettings.setOnClickListener{
                _ -> findNavController().navigate(R.id.HTTPRequestSettingsFragment)
        }
        binding.buttonReminderSettings.setOnClickListener{
                _ -> findNavController().navigate(R.id.ReminderSettingFragment)
        }
    }

    private fun initBatterTargetSeeker() {
        val chargeTarget = sharedPrefs.getInt(Settings.ChargeTarget.javaClass.name, 80)
        binding.chargeSeekBar.progress = chargeTarget
        binding.chargePercentView.text = String.format(getString(R.string.change_target_battery_charge_level), chargeTarget)

        watchBatteryTargetSeeker()
    }

    // TODO if plugged in update charge targets immediately upon the charge limit changing
    private fun watchBatteryTargetSeeker() {
        binding.chargeSeekBar.setOnSeekBarChangeListener(

            object : SeekBar.OnSeekBarChangeListener {
                private var chargeTarget = 0

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    chargeTarget = progress
                    binding.chargePercentView.text = String.format(
                        getString(R.string.change_target_battery_charge_level),
                        chargeTarget
                    )
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val editor = sharedPrefs.edit()
                    editor.putInt(Settings.ChargeTarget.javaClass.name, chargeTarget)
                    editor.apply()
                }
            }
        )
    }

    // TODO if plugged in when setting changed for top two options, make take effect immediately
    // TODO special listener on http requests to suggest using advanced settings
    private fun initAllSwitches() {
        initSwitch(binding.enableAppSwitch, Settings.Enabled.javaClass.name)
        initSwitch(binding.enableNotificationSwitch, Settings.ControlsEnabled.javaClass.name)

        initSwitch(binding.enableReminders, Settings.ReminderEnabled.javaClass.name)
        initSwitch(binding.enableSoundAlarm, Settings.AlarmEnabled.javaClass.name)
        initSwitch(binding.enableHttpRequests, Settings.HTTPRequestEnabled.javaClass.name)
    }

    private fun initSwitch(switch: SwitchCompat, settingName: String) {
        val checked = sharedPrefs.getBoolean(settingName, switch.isChecked)
        switch.isChecked = checked

        watchSwitch(switch, settingName)
    }

    private fun watchSwitch(switch: SwitchCompat, settingName: String) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPrefs.edit()
            editor.putBoolean(settingName, isChecked)
            editor.apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}