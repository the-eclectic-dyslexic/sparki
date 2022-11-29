package com.theeclecticdyslexic.batterychargeassistant.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.theeclecticdyslexic.batterychargeassistant.misc.Settings
import com.theeclecticdyslexic.batterychargeassistant.databinding.HttpRequestFragmentBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class HTTPRequestSettingsFragment : Fragment() {

    private var _binding: HttpRequestFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HttpRequestFragmentBinding.inflate(inflater, container, false)

        initTextInputs()

        return binding.root
    }

    private fun initTextInputs() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}