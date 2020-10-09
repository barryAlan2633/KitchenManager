package com.barryalan.kitchenmanager13.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    version = 3, entities = [
        Recipe::class,
        Ingredient::class,
        Amount::class,
        RecipeIngredientRef::class
    ]
)
abstract class AppDatabase() : RoomDatabase() {

    abstract fun recipeIngredientsRefDao(): RecipeIngredientsRefDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = INSTANCE ?: synchronized(LOCK) {
                INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
        }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "app-database"
            ).fallbackToDestructiveMigration()
                .build()
    }

}