package com.barryalan.kitchenmanager13.viewmodel.mealPlanner

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barryalan.kitchenmanager13.model.AppDatabase
import com.barryalan.kitchenmanager13.model.MealPlan
import com.barryalan.kitchenmanager13.model.MealPlanWithRecipes
import com.barryalan.kitchenmanager13.model.Recipe
import com.barryalan.kitchenmanager13.viewmodel.shared.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MealPlannerCalendarViewModel(application: Application) : BaseViewModel(application) {

    val mealsLiveData = MutableLiveData<List<Recipe>>()
    val recipeLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh(date: String) {
        retrieveMealsFromDB(date)
    }

    private fun retrieveMealsFromDB(date: String) {
        loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val mealPlanWR: MealPlanWithRecipes? =
                AppDatabase(getApplication()).recipeIngredientsRefDao().getMealPlanWithRecipes(date)

            withContext(Dispatchers.Main) {
                if (mealPlanWR != null) {
                    mealPlanRetrieved(mealPlanWR.recipes)
                    Log.d("debug",mealPlanWR.toString() + " not null")

                } else {
                    mealPlanRetrieved(listOf())
                    Log.d("debug",mealPlanWR.toString() + " null")

                }
            }
        }
    }

    private fun mealPlanRetrieved(recipeList: List<Recipe>?) {
        mealsLiveData.value = recipeList
        recipeLoadError.value = false
        loading.value = false
    }

    fun deleteMeal(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase(getApplication()).recipeIngredientsRefDao().deleteRecipe(recipe)
        }
    }


}