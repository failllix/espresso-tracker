package com.thewildzoo.espressotracker.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.thewildzoo.espressotracker.BrewHistoryFragment
import com.thewildzoo.espressotracker.BrewLoggingFragment
import com.thewildzoo.espressotracker.SettingsFragment
import com.thewildzoo.espressotracker.model.Constants

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, val fragmentIds: List<String>) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = fragmentIds.size

    override fun createFragment(position: Int): Fragment {
        when (fragmentIds[position]) {
            Constants.logBrewFragmentID -> {
                return BrewLoggingFragment.newInstance()
            }

            Constants.brewHistoryFragmentID -> {
                return BrewHistoryFragment.newInstance()
            }

            Constants.settingsFragmentID -> {
                return SettingsFragment()
            }

            else -> {
                return Fragment()

            }
        }
    }
}
