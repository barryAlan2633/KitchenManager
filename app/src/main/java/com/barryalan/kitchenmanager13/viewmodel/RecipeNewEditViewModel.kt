package com.barryalan.kitchenmanager13.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barryalan.kitchenmanager13.model.AppDatabase
import com.barryalan.kitchenmanager13.model.RecipeWithIngredients
import kotlinx.coroutines.*

class RecipeNewEditViewModel(application: Application) : BaseViewModel(application) {

    val recipeToUpdateLiveData = MutableLiveData<RecipeWithIngredients>()

    fun saveRecipeWithIngredients(newRecipeWithIngredients: RecipeWithIngredients):Job {
        return viewModelScope.launch {
            AppDatabase(getApplication()).recipeIngredientsRefDao().insertRecipeWithIngredients(newRecipeWithIngredients)
        }
    }

    fun fetch(recipeID: Long) {
        retrieveRecipeWithIngredientsFromDB(recipeID)
    }

    private fun retrieveRecipeWithIngredientsFromDB(recipeID: Long){
        viewModelScope.launch(Dispatchers.IO) {
            val recipeToUpdate = AppDatabase(getApplication()).recipeIngredientsRefDao().getRecipeWithIngredients(recipeID)

            withContext(Dispatchers.Main){
                recipeToUpdateLiveData.value = recipeToUpdate
            }
        }
    }

    fun updateRecipeWithIngredients(updatedRecipeWithIngredients: RecipeWithIngredients): Job {
        return viewModelScope.launch(Dispatchers.IO) {

            //make sure that the recipeToUpdate has been fetched because we need its fields
            recipeToUpdateLiveData.value?.let{recipeWithIngredientsToUpdate ->

                viewModelScope.launch(Dispatchers.IO) {
                    //update recipeWithIngredients object where it differs from the previous instance
                    AppDatabase(getApplication()).recipeIngredientsRefDao().updateRecipeWithIngredients(updatedRecipeWithIngredients,recipeWithIngredientsToUpdate)
                }

            }
            if(recipeToUpdateLiveData.value == null){
                Log.d("Error:", "recipeToUpdate has not been retrieved")
            }
        }
    }
}
