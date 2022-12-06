package com.theeclecticdyslexic.batterychargeassistant.ui

import android.app.ActionBar.LayoutParams
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TableRow
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.requestPermissions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.theeclecticdyslexic.batterychargeassistant.R
import com.theeclecticdyslexic.batterychargeassistant.databinding.HttpRequestFragmentBinding
import com.theeclecticdyslexic.batterychargeassistant.misc.*
import kotlin.math.roundToInt

class HTTPRequestSettingsFragment : Fragment() {

    private var _binding: HttpRequestFragmentBinding? = null
    private val binding get() = _binding!!
    private val idPairs : MutableList<IDPair> = mutableListOf()

    companion object {
        private const val REQUEST_LOCATION_AND_WIFI_PERMISSION = 1
        private const val REQUEST_BACKGROUND_LOCATION = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HttpRequestFragmentBinding.inflate(inflater, container, false)

        initTextInputs()
        initAddButton()
        initLocationPermissionButton()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            initBackgroundLocationPermissionButton()
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        val entries = buildEntriesFromIds()
        Settings.HTTPRequests.store(requireContext(), entries)
    }

    override fun onResume() {
        super.onResume()
        decideWhetherToShowLocationPermissionPitch()
    }

    private fun buildEntriesFromIds() : Array<HTTPRequest> {
        //TODO add sanitization regex for ssid and urls

        return idPairs.map {
            val ssid = binding.entryTable.findViewById<TextInputEditText>(it.ssid)
            val url = binding.entryTable.findViewById<TextInputEditText>(it.url)

            val saneSSID = Utils.sanitizeSSID(ssid.text.toString())

            HTTPRequest(saneSSID, url.text.toString())
        }.toTypedArray()
    }

    private fun initTextInputs(){
        val entries = Settings.HTTPRequests.retrieve(requireContext())

        // first entry
        val first = entries.first()
        binding.firstUrl.setText(first.url)
        binding.firstSsid.setText(first.ssid)
        idPairs.add(IDPair(binding.firstSsid.id, binding.firstUrl.id))

        // remaining entries
        entries.drop(1).forEach(this::addRow)
    }

    private fun initAddButton() =
        binding.addRowButton.setOnClickListener { addRow() }

    private fun addRow(entry: HTTPRequest = HTTPRequest.EMPTY_OBJECT) : TableRow {
        val row = TableRow(context)

        val (ssid, ssidContainer) = createTextInput(entry.ssid, R.style.Theme_BatteryChargeAssistant_SSIDEditor)
        val (url,  urlContainer)  = createTextInput(entry.url, R.style.Theme_BatteryChargeAssistant_URLEditor)

        val pair = IDPair(ssid, url)
        idPairs.add(pair)

        val btn = createButton(pair)

        row.addView(btn)
        row.addView(ssidContainer)
        row.addView(urlContainer)

        binding.entryTable.addView(row)
        return row
    }

    private fun createButton(pair: IDPair): ImageButton {
        val margin = dp(16)
        val btnWrapper = ContextThemeWrapper(context, R.style.Theme_BatteryChargeAssistant_RemoveRowButton)
        val btnParams = TableRow.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            0f)
        btnParams.setMargins(margin, margin, 0, margin)
        val btn = ImageButton(btnWrapper)
        btn.layoutParams = btnParams

        btn.setOnClickListener {
            removeThisRow(it, pair)
        }

        return btn
    }

    private fun removeThisRow(view: View, idPair: IDPair) {
        val index = idPairs.indexOf(idPair)
        idPairs.removeAt(index)

        val parent = view.parent as View
        val grandparent = parent.parent as ViewGroup
        grandparent.removeView(parent)
    }

    private fun createTextInput(initText: String, style: Int) : Pair<Int, TextInputLayout>{
        val margin = dp(16)

        val containerParams = TableRow.LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT,
        1f)
        val containerWrapper = ContextThemeWrapper(context, R.style.Theme_BatteryChargeAssistant_EditorContainer)
        val container = TextInputLayout(containerWrapper)
        container.layoutParams = containerParams

        val editorParams = LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        editorParams.setMargins(margin, margin, margin, margin)
        val editorWrapper = ContextThemeWrapper(context, R.style.Theme_BatteryChargeAssistant_Editor)
        val editor = TextInputEditText(editorWrapper)
        editor.layoutParams = editorParams
        editor.setText(initText)
        editor.id = View.generateViewId()

        container.addView(editor)

        return Pair(editor.id, container)
    }

    private fun decideWhetherToShowLocationPermissionPitch() {
        val context = requireContext()

        val granted = Permissions.locationGranted(context)

        binding.locationPitch.visibility =
            when{
                granted -> View.GONE
                else -> View.VISIBLE
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val backGroundGranted = context.checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

            binding.locationPitchAgain.visibility =
                when{
                    !granted || backGroundGranted -> View.GONE
                    else -> View.VISIBLE
                }
        }
    }

    private fun initLocationPermissionButton() {
        binding.locationPermissionButton.setOnClickListener {
            requestPermissions(
                this.requireActivity(),
                Permissions.location,
                REQUEST_LOCATION_AND_WIFI_PERMISSION
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun initBackgroundLocationPermissionButton() {
        binding.backgroundLocationPermissionButton.setOnClickListener {
            requestPermissions(
                this.requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_BACKGROUND_LOCATION
            )
        }
    }

    private fun dp(i: Int): Int {
        val factor = requireContext().resources.displayMetrics.density
        return (i*factor).roundToInt()
    }

    private data class IDPair(val ssid: Int, val url: Int)
}