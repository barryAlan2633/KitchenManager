package com.barryalan.kitchenmanager13.viewmodel.ingredient

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barryalan.kitchenmanager13.model.AppDatabase
import com.barryalan.kitchenmanager13.model.Ingredient
import com.barryalan.kitchenmanager13.viewmodel.shared.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IngredientDetailViewModel(application: Application) : BaseViewModel(application) {

    val selectedIngredientLiveData = MutableLiveData<Ingredient>()

    fun fetch(ingredientID: Long) {
        retrieveIngredientFromDB(ingredientID)
    }

    private fun retrieveIngredientFromDB(ingredientID: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val ingredient =
                AppDatabase(getApplication()).recipeIngredientsRefDao().getIngredient(ingredientID)

            withContext(Dispatchers.Main) {
                selectedIngredientLiveData.value = ingredient
            }
        }
    }

    fun updateIngredient(updatedIngredient: Ingredient): Job {
        return viewModelScope.launch(Dispatchers.IO) {

            //make sure the selected ingredient has been fetched
            selectedIngredientLiveData.value?.let { selectedIngredient ->

                //Make sure the updated ingredient has the same ID as the old one allowing update
                updatedIngredient.ID = selectedIngredient.ID

                //update the ingredient
                AppDatabase(getApplication()).recipeIngredientsRefDao().updateIngredient(updatedIngredient)
            }

            if (selectedIngredientLiveData.value == null) {
                Log.d("Error: ", "Selected ingredient has not been retrieved")
            }
        }
    }

}
