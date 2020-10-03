package com.barryalan.kitchenmanager13.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barryalan.kitchenmanager13.model.AppDatabase
import com.barryalan.kitchenmanager13.model.Recipe
import com.barryalan.kitchenmanager13.model.RecipeWithIngredients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailViewModel(application: Application) : BaseViewModel(application) {

    val recipeWithIngredientsLiveData = MutableLiveData<RecipeWithIngredients>()

    fun fetch(recipeID: Long) {
        retrieveRecipeWithIngredientsFromDB(recipeID)
    }

    private fun retrieveRecipeWithIngredientsFromDB(recipeID: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipeWithIngredientsDetails =
                AppDatabase(getApplication()).recipeIngredientsRefDao()
                    .getRecipeWithIngredients(recipeID)

            Log.d("debug:", recipeWithIngredientsDetails.toString())
            withContext(Dispatchers.Main) {
                recipeWithIngredientsLiveData.value = recipeWithIngredientsDetails
            }
        }
    }

    fun deleteRecipeWithIngredients(recipeID: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase(getApplication()).recipeIngredientsRefDao().deleteRecipeAndAssociations(recipeID)
        }
    }
}