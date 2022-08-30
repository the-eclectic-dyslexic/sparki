package com.theeclecticdyslexic.batterychargeassistant

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.theeclecticdyslexic.batterychargeassistant.databinding.SettingsFragmentBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SettingsFragment : Fragment() {

    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        sharedPrefs = requireContext().getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)

        initBatterTargetSeeker()
        initAllSwitches()

        return binding.root
    }

    private fun initBatterTargetSeeker() {
        val chargeTarget = sharedPrefs.getInt(Settings.ChargeTarget.javaClass.name, 80)
        binding.chargeSeekBar.progress = chargeTarget
        binding.chargePercentView.text = String.format(getString(R.string.change_target_battery_charge_level), chargeTarget)

        bindBatteryTargetSeeker()
    }

    // TODO if plugged in update charge targets immediately upon the charge limit changing
    private fun bindBatteryTargetSeeker() {
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

    private fun initAllSwitches() {
        // TODO if plugged in when setting changed for top two options, make take effect immediately
        initSwitch(binding.enableAppSwitch, Settings.Enabled.javaClass.name)
        initSwitch(binding.enableNotificationSwitch, Settings.ControlsEnabled.javaClass.name)

        initSwitch(binding.enableReminders, Settings.ReminderEnabled.javaClass.name)
        initSwitch(binding.enableSoundAlarm, Settings.AlarmEnabled.javaClass.name)
        initSwitch(binding.enableHttpRequests, Settings.HTTPRequestEnabled.javaClass.name)
    }

    private fun initSwitch(switch: SwitchCompat, settingName: String) {
        val checked = sharedPrefs.getBoolean(settingName, switch.isChecked)
        switch.isChecked = checked

        bindSwitch(switch, settingName)
    }

    private fun bindSwitch(switch: SwitchCompat, settingName: String) {
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