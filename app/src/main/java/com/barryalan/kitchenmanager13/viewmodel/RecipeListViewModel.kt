package com.barryalan.kitchenmanager13.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barryalan.kitchenmanager13.model.AppDatabase
import com.barryalan.kitchenmanager13.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RecipeListViewModel(application: Application) : BaseViewModel(application) {

    val recipesLiveData = MutableLiveData<List<Recipe>>()
    val recipeLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh() {
        retrieveRecipesFromDB()
    }

    private fun retrieveRecipesFromDB() {
        loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = AppDatabase(getApplication()).recipeIngredientsRefDao().getAllRecipes()

            withContext(Dispatchers.Main) {
                recipesRetrieved(recipes)
            }
        }
    }



    private fun recipesRetrieved(recipeList: List<Recipe>) {
        recipesLiveData.value = recipeList
        recipeLoadError.value = false
        loading.value = false
    }
    fun deleteRecipe(recipe: Recipe){
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase(getApplication()).recipeIngredientsRefDao().deleteRecipe(recipe)
        }
    }

    fun deleteRecipeAndAssociations(recipeID:Long) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase(getApplication()).recipeIngredientsRefDao().deleteRecipeAndAssociations(recipeID)
        }
    }

}