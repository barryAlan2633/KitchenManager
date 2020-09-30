package com.barryalan.kitchenmanager13.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barryalan.kitchenmanager13.model.AppDatabase
import com.barryalan.kitchenmanager13.model.IngredientWithRecipes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IngredientListViewModel(application: Application) : BaseViewModel(application) {

    val ingredientWithRecipesListLiveData = MutableLiveData<List<IngredientWithRecipes>>()
    val ingredientLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh() {
        retrieveIngredientsFromDB()
    }

    private fun retrieveIngredientsFromDB() {
        loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val ingredients = AppDatabase(getApplication()).recipeIngredientsRefDao().getAllIngredientWithRecipes()

            withContext(Dispatchers.Main) {
                ingredientsRetrieved(ingredients)
            }
        }
    }

    private fun ingredientsRetrieved(ingredientWithRecipesList: List<IngredientWithRecipes>) {
        ingredientWithRecipesListLiveData.value = ingredientWithRecipesList
        ingredientLoadError.value = false
        loading.value = false
    }
}