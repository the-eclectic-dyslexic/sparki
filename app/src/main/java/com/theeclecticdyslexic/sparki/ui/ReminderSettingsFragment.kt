package com.theeclecticdyslexic.sparki.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import com.theeclecticdyslexic.sparki.misc.Settings
import com.theeclecticdyslexic.sparki.databinding.ReminderSettingsFragmentBinding


class ReminderSettingsFragment : Fragment() {

    private var _binding : ReminderSettingsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ReminderSettingsFragmentBinding.inflate(inflater, container, false)

        initNumberPickers()

        return binding.root
    }

    private fun initNumberPickers() {
        val reminderFrequency = Settings.ReminderFrequencyMinutes.retrieve(requireContext())

        val tens = reminderFrequency / 10
        val ones = reminderFrequency % 10

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

        fun setReminderFrequency(tens: Int, ones: Int) {
            val newValue = 10*tens + ones
            Settings.ReminderFrequencyMinutes.store(requireContext(), newValue)
        }

        binding.intervalPickerTens.setOnValueChangedListener {
            _, _, tens ->
            val ones = binding.intervalPickerOnes.value

            setReminderFrequency(tens, ones)
        }

        binding.intervalPickerOnes.setOnValueChangedListener {
            _, _, ones ->
            val tens = binding.intervalPickerTens.value

            setReminderFrequency(tens, ones)
        }
    }
}