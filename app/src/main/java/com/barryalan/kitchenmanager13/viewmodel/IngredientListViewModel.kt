package com.barryalan.kitchenmanager13.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barryalan.kitchenmanager13.model.AppDatabase
import com.barryalan.kitchenmanager13.model.Ingredient
import com.barryalan.kitchenmanager13.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IngredientListViewModel(application: Application) : BaseViewModel(application) {

    val ingredientsLiveData = MutableLiveData<List<Ingredient>>()
    val ingredientLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh() {
        retrieveIngredientsFromDB()
    }

    private fun retrieveIngredientsFromDB() {
        loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val ingredients = AppDatabase(getApplication()).recipeIngredientsRefDao().getAllIngredients()

            withContext(Dispatchers.Main) {
                ingredientsRetrieved(ingredients)
            }
        }
    }

    private fun ingredientsRetrieved(ingredientList: List<Ingredient>) {
        ingredientsLiveData.value = ingredientList
        ingredientLoadError.value = false
        loading.value = false
    }
}