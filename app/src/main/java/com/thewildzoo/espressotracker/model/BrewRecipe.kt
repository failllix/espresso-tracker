package com.thewildzoo.espressotracker.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class BrewRecipe(
    @ColumnInfo(name = "coffee_in")
    val coffeeIn: Double,

    @ColumnInfo(name = "coffee_out")
    val coffeeOut: Double,

    @ColumnInfo(name = "planned_brew_ratio")
    val plannedBrewRatio: Double,

    @ColumnInfo(name = "actual_brew_ratio")
    val actualBrewRatio: Double,

    @ColumnInfo(name = "duration")
    val durationInMillis: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @ColumnInfo(name = "created_at")
    var createdAt: Long? = System.currentTimeMillis()
}
