package com.theeclecticdyslexic.batterychargeassistant.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.get
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.theeclecticdyslexic.batterychargeassistant.misc.Settings
import com.theeclecticdyslexic.batterychargeassistant.databinding.HttpRequestFragmentBinding
import com.theeclecticdyslexic.batterychargeassistant.misc.HTTPRequest
import org.w3c.dom.Text
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class HTTPRequestSettingsFragment : Fragment() {

    private var _binding: HttpRequestFragmentBinding? = null
    private val binding get() = _binding!!
    private val idPairs : MutableList<IDPair> = mutableListOf()

    private data class IDPair(val ssid: Int, val url: Int)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HttpRequestFragmentBinding.inflate(inflater, container, false)

        initOldTextInputs()
        initTextInputs()
        initAddButton()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        val entries = buildEntriesFromIds()
        Log.d("entries: ", entries.toString())
        Settings.HTTPRequests.store(requireContext(), entries)
    }

    private fun buildEntriesFromIds() : Array<HTTPRequest> {
        return idPairs.map {
            val ssid = binding.entryTable.findViewById<TextInputEditText>(it.ssid)
            val url = binding.entryTable.findViewById<TextInputEditText>(it.url)
            HTTPRequest(ssid.text.toString(), url.text.toString())
        }.toTypedArray()
    }

    private fun buildEntries() : Array<HTTPRequest> {
        val rows = binding.entryTable.children.drop(1) // first row contains only labels
        val entries = mutableListOf<HTTPRequest>()
        for (r in rows) {
            val row = r as TableRow
            val ssid = extractStringFromTextInputLayout(row[1] as TextInputLayout)
            val url = extractStringFromTextInputLayout(row[2] as TextInputLayout)
            val entry = HTTPRequest(ssid, url)
            Log.d("entry: ", entry.toString())
            entries.add(HTTPRequest(ssid, url))
        }
        return entries.toTypedArray()
    }

    private fun extractStringFromTextInputLayout(layout: TextInputLayout): String {
        val editor = layout[0] as TextInputEditText
        return editor.text.toString()
    }

    private fun initTextInputs(){
        val entries = Settings.HTTPRequests.retrieve(requireContext())

        binding.urlTextInput0.setText(entries[0].url)
        binding.ssidTextInput0.setText(entries[0].ssid)
        idPairs.add(IDPair(binding.ssidTextInput0.id, binding.urlTextInput0.id))


        for (entry in entries.drop(1)) {
            Log.i("trying to render entry:", entry.toString())
            // TODO build new pair of fields
        }
    }

    private fun initAddButton() {
        binding.addRowButton.setOnClickListener {
            addRow()
        }
    }

    private fun addRow() : TableRow {
        val context = requireContext()
        val row = TableRow(context)
        val btn = ImageButton(context)
        val ssid = TextInputEditText(context)
        val url = TextInputEditText(context)
        row.addView(btn)
        row.addView(ssid)
        row.addView(url)

        binding.entryTable.addView(row)
        return row
    }

    private fun initOldTextInputs() {
        val prefs = requireContext().getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
        val url = prefs.getString(
            Settings.HTTPRequestURL.javaClass.name,
            Settings.HTTPRequestURL.default
        )
        val ssids = prefs.getString(
            Settings.WhiteListedSSIDs.javaClass.name,
            Settings.HTTPRequestURL.default
        )

        binding.urlTextInput.setText(url)
        binding.ssidsTextInput.setText(ssids)

        val editor = prefs.edit()
        binding.urlTextInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editor.putString(Settings.HTTPRequestURL.javaClass.name, s.toString().trim())
                editor.apply()
            }
        })

        binding.ssidsTextInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editor.putString(Settings.WhiteListedSSIDs.javaClass.name, s.toString().trim())
                editor.apply()
            }
        })
    }
}