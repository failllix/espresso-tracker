package com.thewildzoo.espressotracker.helper

import com.thewildzoo.espressotracker.model.BrewRecipe

interface BrewHistoryRecyclerViewAdapterInterface {
    fun deleteItem(position: Int)
    fun addItem(recipe: BrewRecipe)
}
