package com.theeclecticdyslexic.batterychargeassistant.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.theeclecticdyslexic.batterychargeassistant.*
import com.theeclecticdyslexic.batterychargeassistant.background.BackgroundService
import com.theeclecticdyslexic.batterychargeassistant.background.MainReceiver
import com.theeclecticdyslexic.batterychargeassistant.databinding.SettingsFragmentBinding
import com.theeclecticdyslexic.batterychargeassistant.misc.NotificationHelper
import com.theeclecticdyslexic.batterychargeassistant.misc.Settings
import com.theeclecticdyslexic.batterychargeassistant.misc.Utils

class SettingsFragment : Fragment() {

    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        sharedPrefs = requireContext().getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
        val context = requireContext()

        initBatterTargetSeeker()
        initAllSwitches()
        initButtons()

        if(BackgroundService.needsToStart(context)){
            initService()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        decideWhetherToShowOptimizerPitch()
    }

    private fun initService() {
        val intent = Intent(requireContext(), BackgroundService::class.java).apply{
            action = BackgroundService::class.java.name
        }
        requireContext().startService(intent)
    }

    private fun dismantleService() {
        val intent = Intent(requireContext(), BackgroundService::class.java).apply{
            action = BackgroundService::class.java.name
        }
        requireContext().stopService(intent)
    }

    private fun initButtons() {
        binding.buttonHttpRequestSettings.setOnClickListener{
            findNavController().navigate(R.id.HTTPRequestSettingsFragment)
        }
        binding.buttonAlarmSettings.setOnClickListener{
            findNavController().navigate(R.id.AlarmSettingsFragment)
        }
        binding.buttonReminderSettings.setOnClickListener{
            findNavController().navigate(R.id.ReminderSettingFragment)
        }
        initOptimizationSettingsButton()
    }

    private fun initBatterTargetSeeker() {
        val chargeTarget = sharedPrefs.getInt(Settings.ChargeTarget.javaClass.name, 80)
        binding.chargeSeekBar.progress = chargeTarget
        binding.chargePercentView.text = String.format(getString(R.string.change_target_battery_charge_level), chargeTarget)

        watchBatteryTargetSeeker()
    }

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
                    updateUI()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    updateSettings()
                    updateInNotificationControls()
                }

                private fun updateUI() {
                    binding.chargePercentView.text = String.format(
                        getString(R.string.change_target_battery_charge_level),
                        chargeTarget
                    )
                }

                private fun updateSettings() {
                    val editor = sharedPrefs.edit()
                    editor.putInt(Settings.ChargeTarget.javaClass.name, chargeTarget)
                    editor.apply()
                }

                private fun updateInNotificationControls() {
                    val context = requireContext()
                    val controlsEnabled = Settings.ControlsEnabled.retrieve(context)

                    val needToUpdateControls =
                               controlsEnabled
                            && Utils.isPlugged(context)
                            && MainReceiver.chargeReceiverRunning
                    if (needToUpdateControls) NotificationHelper.pushControls(context)
                }

                // intentionally unimplemented
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    private fun initAllSwitches() {
        initSwitch(binding.enableAppSwitch, Settings.Enabled.javaClass.name)
        initSwitch(binding.enableNotificationSwitch, Settings.ControlsEnabled.javaClass.name)
        initSwitch(binding.enableReminders, Settings.ReminderEnabled.javaClass.name)
        initSwitch(binding.enableSoundAlarm, Settings.AlarmEnabled.javaClass.name)
        initSwitch(binding.enableHttpRequests, Settings.HTTPRequestEnabled.javaClass.name)
    }

    private fun initSwitch(switch: SwitchCompat, settingName: String) {
        switch.isChecked = sharedPrefs.getBoolean(settingName, switch.isChecked)

        watchSwitch(switch, settingName)
    }

    private fun watchSwitch(switch: SwitchCompat, settingName: String) {
        switch.setOnCheckedChangeListener { _, isChecked ->

            val alreadyShown = shouldShow()

            val context = requireContext()
            val editor = sharedPrefs.edit()
            editor.putBoolean(settingName, isChecked)
            editor.apply()

            when {
                BackgroundService.needsToStart(context) -> initService()
                BackgroundService.needsToStop(context) -> dismantleService()
            }

            when {
                !alreadyShown && shouldShow() -> NotificationHelper.pushControls(context)
                alreadyShown && !shouldShow() -> NotificationHelper.cancelControls(context)
            }
        }
    }

    private fun shouldShow(): Boolean {
        val context = requireContext()
        val controlsEnabled = Settings.ControlsEnabled.retrieve(context)
        val plugged = Utils.isPlugged(context)

        return     BackgroundService.running
                && controlsEnabled
                && plugged
                && Utils.canComplete(context)
    }

    private fun decideWhetherToShowOptimizerPitch() {
        val context = requireContext()
        val mgr = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val notOptimized = mgr.isIgnoringBatteryOptimizations(context.packageName)
        binding.optimizerPitch.visibility =
            if (notOptimized) View.GONE
            else              View.VISIBLE
    }

    private fun initOptimizationSettingsButton() {
        binding.optimizerSettingsButton.setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        }
    }
}