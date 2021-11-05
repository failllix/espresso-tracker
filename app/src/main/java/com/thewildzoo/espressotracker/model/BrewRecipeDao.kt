package com.thewildzoo.espressotracker.model

import androidx.room.* // ktlint-disable no-wildcard-imports

@Dao
interface BrewRecipeDao {

    @Query("SELECT * FROM recipes ORDER BY id DESC")
    suspend fun getAllRecipes(): List<BrewRecipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecipe(recipe: BrewRecipe)

    @Delete
    suspend fun delete(recipe: BrewRecipe)
}
