package com.theeclecticdyslexic.batterychargeassistant.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.theeclecticdyslexic.batterychargeassistant.databinding.AlarmSettingsFragmentBinding
import com.theeclecticdyslexic.batterychargeassistant.misc.Settings

// TODO fix significant code duplication across this fragment (shared with settings and reminder settings)
class AlarmSettingsFragment : Fragment() {

    private var _binding : AlarmSettingsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        sharedPrefs = requireContext().getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
        _binding = AlarmSettingsFragmentBinding.inflate(inflater, container, false)

        initAllSwitches()
        initNumberPickers()

        return binding.root
    }

    private fun initAllSwitches() {
        initSwitch(binding.enableVibration, Settings.AlarmVibrates.javaClass.name)
        initSwitch(binding.alarmIgnoresSilent, Settings.AlarmIgnoresSilent.javaClass.name)
    }

    private fun initSwitch(switch: SwitchCompat, settingName: String) {
        switch.isChecked = sharedPrefs.getBoolean(settingName, switch.isChecked)

        watchSwitch(switch, settingName)
    }

    private fun watchSwitch(switch: SwitchCompat, settingName: String) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPrefs.edit()
            editor.putBoolean(settingName, isChecked)
            editor.apply()
        }
    }

    private fun initNumberPickers() {
        val timeout = Settings.AlarmTimeoutMinutes.retrieve(requireContext())

        val tens = timeout / 10
        val ones = timeout % 10

        initPicker(binding.intervalPickerTens, tens)
        initPicker(binding.intervalPickerOnes, ones)

        listenToNumberPickers()
    }

    private fun initPicker(picker: NumberPicker, numberToShow: Int) {
        picker.minValue = 0
        picker.maxValue = 9
        picker.wrapSelectorWheel = false

        picker.value = numberToShow
    }

    private fun listenToNumberPickers() {

        fun setTimeout(tens: Int, ones: Int) {
            val newValue = 10*tens + ones
            Settings.AlarmTimeoutMinutes.store(requireContext(), newValue)
        }

        binding.intervalPickerTens.setOnValueChangedListener {
                _, _, tens ->
            val ones = binding.intervalPickerOnes.value

            setTimeout(tens, ones)
        }

        binding.intervalPickerOnes.setOnValueChangedListener {
                _, _, ones ->
            val tens = binding.intervalPickerTens.value

            setTimeout(tens, ones)
        }
    }

}