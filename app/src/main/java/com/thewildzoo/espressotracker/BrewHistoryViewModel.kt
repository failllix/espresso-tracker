package com.thewildzoo.espressotracker

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.thewildzoo.espressotracker.model.AppDatabase
import com.thewildzoo.espressotracker.model.BrewRecipe

class BrewHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val database = AppDatabase.getInstance(context)

    private val _recipes = MutableLiveData<List<BrewRecipe>>()
    val recipes: LiveData<List<BrewRecipe>> = _recipes

    suspend fun getRecipes() {
        _recipes.value = database.brewRecipeDao().getAllRecipes()
    }

    suspend fun deleteRecipe(recipe: BrewRecipe) {
        database.brewRecipeDao().delete(recipe)
    }

    suspend fun insertRecipe(recipe: BrewRecipe) {
        database.brewRecipeDao().saveRecipe(recipe)
    }
}
