package com.theeclecticdyslexic.batterychargeassistant.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import com.theeclecticdyslexic.batterychargeassistant.misc.Settings
import com.theeclecticdyslexic.batterychargeassistant.databinding.ReminderSettingsFragmentBinding

class ReminderSettingsFragment : Fragment() {

    private var _binding : ReminderSettingsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ReminderSettingsFragmentBinding.inflate(inflater, container, false)

        initNumberPickers()

        return binding.root
    }

    private fun initNumberPickers() {
        val prefs = requireContext().getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
        val reminderFrequency = prefs.getInt(
            Settings.ReminderFrequency.javaClass.name,
            Settings.ReminderFrequency.default
        )

        val tens = reminderFrequency / 10
        val ones = reminderFrequency % 10

        initPicker(binding.reminderIntervalPickerTens, tens)
        initPicker(binding.reminderIntervalPickerOnes, ones)

        listenToNumberPickers()
    }

    private fun initPicker(picker: NumberPicker, numberToShow: Int) {
        picker.minValue = 0
        picker.maxValue = 9
        picker.wrapSelectorWheel = false

        picker.value = numberToShow
    }

    private fun listenToNumberPickers() {
        val prefs = requireContext().getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
        val editor = prefs.edit()

        fun setReminderFrequency(tens: Int, ones: Int) {
            val newValue = 10*tens + ones
            editor.putInt(Settings.ReminderFrequency.javaClass.name, newValue)
            editor.apply()
        }

        binding.reminderIntervalPickerTens.setOnValueChangedListener {
            _, _, tens ->
            val ones = binding.reminderIntervalPickerOnes.value

            setReminderFrequency(tens, ones)
        }

        binding.reminderIntervalPickerOnes.setOnValueChangedListener {
            _, _, ones ->
            val tens = binding.reminderIntervalPickerTens.value

            setReminderFrequency(tens, ones)
        }
    }
}