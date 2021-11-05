package com.thewildzoo.espressotracker.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [BrewRecipe::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun brewRecipeDao(): BrewRecipeDao

    companion object {

        private var database: AppDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recipes ADD COLUMN created_at INTEGER DEFAULT NULL"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {

            if (database == null) {
                database = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }

            return database!!
        }
    }
}
