package com.theeclecticdyslexic.batterychargeassistant.ui

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.requestPermissions
import com.google.android.material.textfield.TextInputEditText
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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
        entries.drop(1).forEach(::addRow)
    }

    private fun initAddButton() =
        binding.addRowButton.setOnClickListener { addRow() }

    private fun addRow(entry: HTTPRequest = HTTPRequest.EMPTY_OBJECT) {
        val row = layoutInflater.inflate(R.layout.http_request_row, null) as TableRow
        val ssid = View.generateViewId()
        val url = View.generateViewId()

        row.initEditor("ssid", ssid, entry.ssid)
        row.initEditor("url", url, entry.url)

        val pair = IDPair(ssid, url)
        idPairs.add(pair)

        row.initButton(pair)

        binding.entryTable.addView(row)
    }

    private fun TableRow.initEditor(searchTag: String, id: Int, text: String) {
        val editor = this.findViewWithTag<TextInputEditText>(searchTag)
        editor.id = id
        editor.setText(text)
    }

    private fun TableRow.initButton(idPair: IDPair) {
        val button = this.findViewWithTag<ImageButton>("button")
        button.setOnClickListener {
            this.removeSelf(idPair)
        }
    }

    private fun TableRow.removeSelf(idPair: IDPair) {
        val index = idPairs.indexOf(idPair)
        idPairs.removeAt(index)

        val parent = this.parent as TableLayout
        parent.removeView(this)
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