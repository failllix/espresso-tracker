package com.thewildzoo.espressotracker.brewlogging

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.thewildzoo.espressotracker.R
import com.thewildzoo.espressotracker.model.AppDatabase
import com.thewildzoo.espressotracker.model.BrewRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.* // ktlint-disable no-wildcard-imports

class BrewingLogViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val resources: Resources = application.resources

    private val decimalFormat: DecimalFormat = DecimalFormat("#.#").apply {
        roundingMode = RoundingMode.CEILING
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.decimalSeparator = '.'
        decimalFormatSymbols = symbols
    }

    private val _coffeeIn = MutableLiveData(
        sharedPreferences.getString(
            resources.getString(R.string.pref_default_coffee_in_key),
            resources.getInteger(R.integer.coffee_in_default).toString()
        )
        !!.toDouble()
    )
    val coffeeIn: LiveData<Double> = _coffeeIn

    private val _coffeeOut = MutableLiveData(
        sharedPreferences.getString(
            resources.getString(R.string.pref_default_coffee_out_key),
            resources.getInteger(R.integer.coffee_out_default).toString()
        )
        !!.toDouble()
    )
    val coffeeOut: LiveData<Double> = _coffeeOut

    private val _plannedBrewRatio = MutableLiveData(
        sharedPreferences.getString(
            resources.getString(R.string.pref_default_planned_brew_ratio_key),
            resources.getInteger(R.integer.planned_brew_ratio_default).toString()
        )
        !!.toDouble()
    )
    val plannedBrewRatio: LiveData<Double> = _plannedBrewRatio

    private val _duration = MutableLiveData<Long>(0)
    val duration: LiveData<Long> = _duration

    suspend fun saveRecipe() {
        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            val recipe = BrewRecipe(
                coffeeIn.value!!,
                coffeeOut.value!!,
                plannedBrewRatio.value!!,
                decimalFormat.format((coffeeOut.value!! / coffeeIn.value!!)).toDouble(),
                duration.value!!
            )

            database.brewRecipeDao().saveRecipe(recipe)
        }

        resetValues()
    }

    fun resetValues() {
        _coffeeIn.value = sharedPreferences.getString(
            resources.getString(R.string.pref_default_coffee_in_key),
            resources.getInteger(R.integer.coffee_in_default).toString()
        )!!.toDouble()

        _coffeeOut.value = sharedPreferences.getString(
            resources.getString(R.string.pref_default_coffee_out_key),
            resources.getInteger(R.integer.coffee_out_default).toString()
        )!!.toDouble()

        _plannedBrewRatio.value = sharedPreferences.getString(
            resources.getString(R.string.pref_default_planned_brew_ratio_key),
            resources.getInteger(R.integer.planned_brew_ratio_default).toString()
        )!!.toDouble()

        _duration.value = 0
    }

    fun changeCoffeeInBy(change: Double) {
        val newValue = decimalFormat.format(_coffeeIn.value?.plus(change)).toDouble()
        _coffeeIn.value = if (newValue > 0) newValue else 0.0
        _coffeeOut.value = decimalFormat.format(_plannedBrewRatio.value?.times(newValue)).toDouble()
    }

    fun changeCoffeeOutBy(change: Double) {
        val newValue = decimalFormat.format(_coffeeOut.value?.plus(change)).toDouble()
        _coffeeOut.value = if (newValue > 0) newValue else 0.0
    }

    fun changePlannedBrewRatioBy(change: Double) {
        val newValue = decimalFormat.format(_plannedBrewRatio.value?.plus(change)).toDouble()
        _plannedBrewRatio.value = if (newValue > 1) newValue else 1.0
        _coffeeOut.value = decimalFormat.format(_coffeeIn.value?.times(newValue)).toDouble()
    }

    fun updateDuration(newDuration: Long) {
        _duration.value = newDuration
    }
}
