package com.thewildzoo.espressotracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.thewildzoo.espressotracker.adapter.ViewPagerAdapter
import com.thewildzoo.espressotracker.helper.MainActivityViewPagerStateInterface
import com.thewildzoo.espressotracker.model.Constants

class MainActivity : AppCompatActivity(), MainActivityViewPagerStateInterface {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(resources.getString(R.string.pref_setup_complete_key), false)) {
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
        }

        viewPager = findViewById(R.id.main_view_pager)
        tabLayout = findViewById(R.id.main_tab_layout)

        val pagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle, Constants.tabs)

        viewPager.apply {
            adapter = pagerAdapter
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = resources.getString(Constants.tabTitles[position])
        }.attach()
    }

    override fun disableViewPager() {
        viewPager.isUserInputEnabled = false
    }

    override fun enableViewPager() {
        viewPager.isUserInputEnabled = true
    }
}
