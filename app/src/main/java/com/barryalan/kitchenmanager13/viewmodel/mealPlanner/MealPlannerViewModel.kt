package com.barryalan.kitchenmanager13.viewmodel.mealPlanner

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barryalan.kitchenmanager13.model.AppDatabase
import com.barryalan.kitchenmanager13.model.MealPlanWithRecipes
import com.barryalan.kitchenmanager13.model.Recipe
import com.barryalan.kitchenmanager13.viewmodel.shared.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MealPlannerViewModel(application: Application) : BaseViewModel(application) {

    val mealsLiveData = MutableLiveData<MealPlanWithRecipes>()
    val recipeLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh(date: String) {
        retrieveMealsFromDB(date)
    }

    private fun retrieveMealsFromDB(date: String) {
        loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val mealPlanWR: MealPlanWithRecipes? = AppDatabase(getApplication()).recipeIngredientsRefDao().getMealPlanWithRecipes(date)

            withContext(Dispatchers.Main) {
                if (mealPlanWR != null) {
                    mealPlanRetrieved(mealPlanWR)
                    Log.d("debug",mealPlanWR.toString() + " not null")

                } else {
                    mealPlanRetrieved(null)
                    Log.d("debug",mealPlanWR.toString() + " null")
                    //todo display toast saying "there are no meals planned for this day"

                }
            }
        }
    }

    private fun mealPlanRetrieved(mealList: MealPlanWithRecipes?) {
        mealsLiveData.value = mealList
        recipeLoadError.value = false
        loading.value = false
    }

    fun deleteMeal(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase(getApplication()).recipeIngredientsRefDao().deleteRecipe(recipe)
        }
    }

    fun updateMealAmount(amountID: Long, value: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase(getApplication()).recipeIngredientsRefDao().updateMealAmount(value,amountID)
        }
    }


}