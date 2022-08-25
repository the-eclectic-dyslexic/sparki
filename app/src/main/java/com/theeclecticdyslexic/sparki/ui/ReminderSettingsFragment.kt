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
import com.theeclecticdyslexic.sparki.misc.Settings
import com.theeclecticdyslexic.sparki.databinding.FragmentReminderSettingsBinding
import com.theeclecticdyslexic.sparki.extensions.init
import com.theeclecticdyslexic.sparki.extensions.show

/**
 * Handles UI for settings specific to sending reminders upon charge target being reached
 */
class ReminderSettingsFragment : Fragment() {

    private var _binding : FragmentReminderSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReminderSettingsBinding.inflate(inflater, container, false)

        val context = requireContext()
        initSwitch(context)
        initIntervalPicker(context)

        return binding.root
    }

    private fun initSwitch(context: Context) {
        val setting = Settings.ReminderWillRepeat
        val listener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            binding.intervalPickerContainer.show(isChecked)
            setting.store(context, isChecked)
        }
        binding.reminderRepeats.init(context, setting, listener)
    }

    private fun initIntervalPicker(context: Context) {
        val reminderRepeats = Settings.ReminderWillRepeat.retrieve(context)
        binding.intervalPickerContainer.show(reminderRepeats)

        binding.intervalPicker.init(context, Settings.ReminderInterval)
    }
}