package com.thewildzoo.espressotracker.model

import com.thewildzoo.espressotracker.R

object Constants {

    const val logBrewFragmentID: String = "log_brew_fragment"
    const val brewHistoryFragmentID: String = "brew_history_fragment"
    const val settingsFragmentID: String = "settings_fragment"

    val tabs = listOf(logBrewFragmentID, brewHistoryFragmentID, settingsFragmentID)
    val tabTitles = listOf(R.string.fragment_title_log_brew, R.string.fragment_title_brew_history, R.string.fragment_title_settings)
}
