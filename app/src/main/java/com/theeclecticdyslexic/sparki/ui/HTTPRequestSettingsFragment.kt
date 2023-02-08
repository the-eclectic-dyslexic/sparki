/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.theeclecticdyslexic.sparki.R
import com.theeclecticdyslexic.sparki.databinding.FragmentHttpRequestBinding
import com.theeclecticdyslexic.sparki.extensions.show
import com.theeclecticdyslexic.sparki.misc.*

/**
 * Handles UI specific to sending http requests upon charge target being reached
 */

class HTTPRequestSettingsFragment : Fragment() {

    private var _binding: FragmentHttpRequestBinding? = null
    private val binding get() = _binding!!
    private val idPairs : MutableList<IDPair> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHttpRequestBinding.inflate(inflater, container, false)

        initTextInputs()
        initAddButton()
        initLocationPermissionButton()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            initLocationPermissionButtonWorkaround()

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        val entries = buildEntriesFromIds()
        Settings.HTTPRequestList.store(requireContext(), entries)
    }

    override fun onResume() {
        super.onResume()
        handleShowLocationPermissionPitch()
        handleShowLocationWorkAroundPitch()
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
        val entries = Settings.HTTPRequestList.retrieve(requireContext())

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
        val row = layoutInflater.inflate(R.layout.tablerow_http_request, null) as TableRow
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
        editor.backgroundTintList = binding.firstSsid.backgroundTintList
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

    private fun handleShowLocationPermissionPitch() {
        val context = requireContext()

        val granted = Permissions.locationGranted(context)

        binding.locationPitch.show(!granted)
    }

    private fun handleShowLocationWorkAroundPitch() {
        val context = requireContext()

        val partiallyGranted = Permissions.onlyForegroundGranted(context)
        binding.locationWorkaroundPitch.show(partiallyGranted)
    }

    private fun initLocationPermissionButton() {
        binding.locationPermissionButton.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(
                    this.requireActivity(),
                    Permissions.location + Permissions.backgroundLocation,
                    Permissions.REQUEST_LOCATION_AND_WIFI_PERMISSION
                )
            } else {
                requestPermissions(
                    this.requireActivity(),
                    Permissions.location,
                    Permissions.REQUEST_LOCATION_AND_WIFI_PERMISSION
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun initLocationPermissionButtonWorkaround() {
        binding.locationPermissionButtonWorkaround.setOnClickListener {
            requestPermissions(
                this.requireActivity(),
                Permissions.backgroundLocation,
                Permissions.REQUEST_BACKGROUND_LOCATION_WORKAROUND
            )
        }
    }

    private data class IDPair(val ssid: Int, val url: Int)
}