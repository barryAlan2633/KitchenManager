package com.barryalan.kitchenmanager13.viewmodel.recipe

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barryalan.kitchenmanager13.model.AppDatabase
import com.barryalan.kitchenmanager13.model.Ingredient
import com.barryalan.kitchenmanager13.model.RecipeWithIngredients
import com.barryalan.kitchenmanager13.viewmodel.shared.BaseViewModel
import kotlinx.coroutines.*

class RecipeNewEditViewModel(application: Application) : BaseViewModel(application) {

    val recipeToUpdateLiveData = MutableLiveData<RecipeWithIngredients>()
    val ingredientListLiveData = MutableLiveData<List<Ingredient>>()

    @ExperimentalStdlibApi
    fun saveRecipeWithIngredients(newRecipeWithIngredients: RecipeWithIngredients):Job {
        return viewModelScope.launch {
            AppDatabase(getApplication()).recipeIngredientsRefDao().insertRecipeWithIngredients(newRecipeWithIngredients)
        }
    }

    fun fetchSelectedRecipeWI(recipeID: Long) {
        retrieveRecipeWithIngredientsFromDB(recipeID)
    }

    fun fetchIngredientList(){
        retrieveIngredientListFromDB()
    }

    private fun retrieveRecipeWithIngredientsFromDB(recipeID: Long){
        viewModelScope.launch(Dispatchers.IO) {
            val recipeToUpdate = AppDatabase(getApplication()).recipeIngredientsRefDao().getRecipeWithIngredients(recipeID)

            withContext(Dispatchers.Main){
                recipeToUpdateLiveData.value = recipeToUpdate
            }
        }
    }

    @ExperimentalStdlibApi
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


    private fun retrieveIngredientListFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            val ingredientList = AppDatabase(getApplication()).recipeIngredientsRefDao().getAllIngredients()

            withContext(Dispatchers.Main){
                ingredientListLiveData.value = ingredientList
            }
        }
    }
}
