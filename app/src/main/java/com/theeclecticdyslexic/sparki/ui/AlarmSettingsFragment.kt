/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.theeclecticdyslexic.sparki.databinding.FragmentAlarmSettingsBinding
import com.theeclecticdyslexic.sparki.extensions.init
import com.theeclecticdyslexic.sparki.extensions.show
import com.theeclecticdyslexic.sparki.misc.Settings

/**
 * Handles UI specific to sounding an alarm upon charge target being reached
 */

// TODO fix significant code duplication across this fragment (shared with reminder settings)
class AlarmSettingsFragment : Fragment() {

    private var _binding : FragmentAlarmSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAlarmSettingsBinding.inflate(inflater, container, false)

        initAllSwitches()
        initNumberPicker()

        return binding.root
    }

    private fun initAllSwitches() {
        val context = requireContext()
        binding.enableVibration.init(context, Settings.AlarmVibrates)
        binding.alarmIgnoresSilent.init(context, Settings.AlarmIgnoresSilent)
        initAlarmTimeoutSwitch(context)
    }

    private fun initAlarmTimeoutSwitch(context: Context) {
        val listener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            binding.timeoutPickerContainer.show(isChecked)
            Settings.AlarmWillTimeout.store(context, isChecked)
        }

        binding.alarmTimeout.init(context, Settings.AlarmWillTimeout, listener)
    }

    private fun initNumberPicker() {
        val context = requireContext()
        val timeoutEnabled = Settings.AlarmWillTimeout.retrieve(context)
        binding.timeoutPickerContainer.show(timeoutEnabled)

        binding.timeoutPicker.init(context, Settings.AlarmTimeout)
    }
}