package com.thewildzoo.espressotracker

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.method.NumberKeyListener
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.* // ktlint-disable no-wildcard-imports
import kotlin.math.round

class SettingsFragment : PreferenceFragmentCompat() {

    private var coffeeInPref: EditTextPreference? = null
    private var coffeeOutPref: EditTextPreference? = null
    private var brewRatioPref: EditTextPreference? = null
    private var scaleFactorPref: EditTextPreference? = null
    private var thresholdPref: EditTextPreference? = null

    private var accelerometerRecalibrationPref: Preference? = null

    private val decimalFormat: DecimalFormat = DecimalFormat("#.#").apply {
        roundingMode = RoundingMode.CEILING
        roundingMode = RoundingMode.CEILING
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.decimalSeparator = '.'
        decimalFormatSymbols = symbols
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setDividerHeight(0)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        context?.resources?.let { it ->

            coffeeInPref = findPreference(it.getString(R.string.pref_default_coffee_in_key))
            coffeeOutPref = findPreference(it.getString(R.string.pref_default_coffee_out_key))
            brewRatioPref =
                findPreference(it.getString(R.string.pref_default_planned_brew_ratio_key))
            scaleFactorPref = findPreference(it.getString(R.string.scale_factor_key))
            thresholdPref = findPreference(it.getString(R.string.pref_threshold_key))

            accelerometerRecalibrationPref =
                findPreference(it.getString(R.string.pref_accelerometer_recalibration_key))

            coffeeInPref?.apply {
                summary = text.toString().toFloat().toString()
                setOnBindEditTextListener {
                    it.setRawInputType(TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                }

                setOnPreferenceChangeListener { preference, newValue ->
                    this.summary = newValue.toString().toFloat().toString()

                    brewRatioPref?.text?.toFloat()?.let { brewRatio ->
                        val newOut = brewRatio.times(newValue.toString().toFloat())

                        val formattedOut = decimalFormat.format(newOut)

                        coffeeOutPref?.text = formattedOut
                        coffeeOutPref?.summary = formattedOut
                    }

                    true
                }
            }

            coffeeOutPref?.apply {
                summary = text.toString().toFloat().toString()
                setOnBindEditTextListener {
                    it.setRawInputType(TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                }

                setOnPreferenceChangeListener { preference, newValue ->
                    this.summary = newValue.toString().toFloat().toString()

                    coffeeInPref?.text?.toFloat()?.let { coffeeIn ->
                        val newRatio = newValue.toString().toFloat().div(coffeeIn)

                        val formattedRatio = decimalFormat.format(newRatio)

                        brewRatioPref?.text = formattedRatio
                        brewRatioPref?.summary = formattedRatio
                    }

                    true
                }
            }

            brewRatioPref?.apply {
                summary = text.toString().toFloat().toString()
                setOnBindEditTextListener {
                    it.setRawInputType(TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                }

                setOnPreferenceChangeListener { preference, newValue ->
                    this.summary = newValue.toString().toFloat().toString()

                    coffeeInPref?.text?.toFloat()?.let { coffeeIn ->
                        val newOut = newValue.toString().toFloat().times(coffeeIn)

                        val formattedOut = decimalFormat.format(newOut)

                        coffeeOutPref?.text = formattedOut
                        coffeeOutPref?.summary = formattedOut
                    }

                    true
                }
            }

            scaleFactorPref?.apply {
                summary = round(text.toString().toFloat()).toInt().toString()
                setOnBindEditTextListener {
                    it.keyListener = object : NumberKeyListener() {
                        override fun getInputType(): Int {
                            return TYPE_CLASS_NUMBER
                        }

                        override fun getAcceptedChars(): CharArray {
                            val digits = "1234567890"
                            return digits.toCharArray()
                        }
                    }
                }

                setOnPreferenceChangeListener { preference, newValue ->
                    this.summary = round(newValue.toString().toFloat()).toInt().toString()
                    text = round(newValue.toString().toFloat()).toInt().toString()

                    true
                }
            }

            thresholdPref?.apply {
                summary = round(text.toString().toFloat()).toInt().toString()

                setOnBindEditTextListener {
                    it.keyListener = object : NumberKeyListener() {
                        override fun getInputType(): Int {
                            return TYPE_CLASS_NUMBER
                        }

                        override fun getAcceptedChars(): CharArray {
                            val digits = "1234567890"
                            return digits.toCharArray()
                        }
                    }
                }

                setOnPreferenceChangeListener { preference, newValue ->
                    this.summary = round(newValue.toString().toFloat()).toInt().toString()
                    text = round(newValue.toString().toFloat()).toInt().toString()

                    true
                }
            }

            accelerometerRecalibrationPref?.apply {
                setOnPreferenceClickListener {
                    val intent = Intent(this.context, SetupActivity::class.java)
                    startActivity(intent)
                    true
                }
            }
        }
    }
}
