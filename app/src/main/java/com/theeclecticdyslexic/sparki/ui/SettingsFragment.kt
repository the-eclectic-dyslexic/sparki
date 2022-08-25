/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.theeclecticdyslexic.sparki.*
import com.theeclecticdyslexic.sparki.background.ForegroundService
import com.theeclecticdyslexic.sparki.background.MainReceiver
import com.theeclecticdyslexic.sparki.databinding.FragmentSettingsBinding
import com.theeclecticdyslexic.sparki.extensions.init
import com.theeclecticdyslexic.sparki.extensions.show
import com.theeclecticdyslexic.sparki.misc.*

/**
 * Handles UI for allowing changing of basic app settings
 *
 * If the app is currently being affected by the battery optimizer,
 * will show the user a link to the battery optimizer
 *
 * Will handle the starting and stopping of the background service,
 * based on which settings are enabled
 *
 * Will handle pushing/updating of controls notification if setting change
 *
 * Will apply a theme change if setting is changed
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPrefs = requireContext().getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)

        initBatterTargetSeeker()
        initAllSwitches()
        initButtons()
        initSpinner()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        decideWhetherToShowOptimizerPitch()

        val context = requireContext()
        if (ForegroundService.needsToStart(context)) initService()
    }

    private fun initService() {
        val intent = Intent(requireContext(), ForegroundService::class.java).apply{
            action = ForegroundService::class.java.name
        }
        requireContext().startService(intent)
    }

    private fun dismantleService() {
        val intent = Intent(requireContext(), ForegroundService::class.java).apply{
            action = ForegroundService::class.java.name
        }
        requireContext().stopService(intent)
    }

    private fun initSpinner() {
        val context = requireContext()
        val values = Themes.values().map {it.toString()}
        val adapter = ArrayAdapter(context, R.layout.spinner_item_inline, values)
        adapter.setDropDownViewResource(R.layout.spinner_item)

        binding.themeSpinner.adapter = adapter
        val currentTheme = Settings.UITheme.retrieve(context)
        val position = Themes.idMap[currentTheme]?.ordinal ?: Themes.System.ordinal
        binding.themeSpinner.setSelection(position)
        binding.themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val name = parent.getItemAtPosition(position).toString()
                val theme = Themes.valueOf(name).id
                Settings.UITheme.store(context, theme)
                AppCompatDelegate.setDefaultNightMode(theme)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initButtons() {
        initNavButton(binding.buttonReminderSettings,    R.id.ReminderSettingFragment)
        initNavButton(binding.buttonAlarmSettings,       R.id.AlarmSettingsFragment)
        initNavButton(binding.buttonHttpRequestSettings, R.id.HTTPRequestSettingsFragment)

        initOptimizationSettingsButton()
    }
    private fun initNavButton(button: ImageButton, location: Int) {
        button.setOnClickListener{
            findNavController().navigate(location)
        }
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
                    Settings.ChargeTarget.store(requireContext(), chargeTarget)
                    updateInNotificationControls()
                }

                private fun updateUI() {
                    binding.chargePercentView.text =
                        String.format(
                            getString(R.string.change_target_battery_charge_level),
                            chargeTarget)
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
        val context = requireContext()
        initControlsSwitch(context)

        initSwitch(context, binding.enableAppSwitch, Settings.Enabled)

        initSwitch(context, binding.enableReminders, Settings.RemindersEnabled)
        initSwitch(context, binding.enableSoundAlarm, Settings.AlarmEnabled)
        initSwitch(context, binding.enableHttpRequests, Settings.HTTPRequestsEnabled)
    }

    private fun initSwitch(context: Context, switch: SwitchCompat, setting: Settings.BooleanSetting) {
        val listener = CompoundButton.OnCheckedChangeListener {
                _, isChecked ->
            makeNotificationPermissionPopup()
            setting.store(context, isChecked)
            when {
                ForegroundService.needsToStart(context) -> initService()
                ForegroundService.needsToStop(context) -> dismantleService()
            }
        }
        switch.init(context, setting, listener)
    }

    private fun initControlsSwitch(context: Context) {
        val setting = Settings.ControlsEnabled
        val listener = CompoundButton.OnCheckedChangeListener {
            _, isChecked ->
            makeNotificationPermissionPopup()
            val alreadyShown = shouldShowControls()

            setting.store(context, isChecked)

            val shouldShowNow = shouldShowControls()

            when {
                !alreadyShown && shouldShowNow -> NotificationHelper.pushControls(context)
                alreadyShown && !shouldShowNow -> NotificationHelper.cancelControls(context)
            }
        }
        binding.enableNotificationSwitch.init(context, setting, listener)
    }

    private fun makeNotificationPermissionPopup() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (!Permissions.postGranted(requireContext())) return

        val popupView = layoutInflater.inflate(R.layout.popup_notification_permission, null)
        val window = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true)

        window.showAtLocation(binding.root, Gravity.CENTER, 0, 0)

        val grant = popupView.findViewById<Button>(R.id.grant)
        grant.setOnClickListener {
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                Permissions.post,
                Permissions.REQUEST_POST_PERMISSION
            )
        }
        val dismiss = popupView.findViewById<Button>(R.id.dismiss)
        dismiss.setOnClickListener { window.dismiss() }
    }

    private fun shouldShowControls(): Boolean {
        val context = requireContext()
        val controlsEnabled = Settings.ControlsEnabled.retrieve(context)
        val plugged = Utils.isPlugged(context)

        return     ForegroundService.running
                && controlsEnabled
                && plugged
                && Utils.canComplete(context)
    }

    private fun decideWhetherToShowOptimizerPitch() {
        val context = requireContext()
        val mgr = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val optimized = !mgr.isIgnoringBatteryOptimizations(context.packageName)
        binding.optimizerPitch.show(optimized)
    }

    private fun initOptimizationSettingsButton() {
        binding.optimizerSettingsButton.setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        }
    }
}
