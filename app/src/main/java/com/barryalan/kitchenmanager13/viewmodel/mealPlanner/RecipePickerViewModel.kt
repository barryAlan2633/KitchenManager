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


class RecipePickerViewModel(application: Application) : BaseViewModel(application) {


    val allRecipesLiveData = MutableLiveData<List<Recipe>>()
    val selectedRecipesLiveData = MutableLiveData<List<Long>>()
    val recipeLoadError = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun refresh(date: String) {
        retrieveRecipesFromDB()
        retrieveMealPlanWithRecipes(date)
    }

    private fun retrieveMealPlanWithRecipes(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedMealPlan: MealPlanWithRecipes? = AppDatabase(getApplication()).recipeIngredientsRefDao().getMealPlanWithRecipes(date)

            withContext(Dispatchers.Main) {
                val selectedRecipes: MutableList<Long> = mutableListOf()

                selectedMealPlan?.let {
                    selectedMealPlan.recipes.map { recipe -> selectedRecipes.add(recipe.ID) }
                    selectedRecipesLiveData.value = selectedRecipes
                }
            }
        }
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
        allRecipesLiveData.value = recipeList
        recipeLoadError.value = false
        loading.value = false
    }

    fun saveMeals(date: String, selectedRecipesIds: List<Long>, oldSelectedRecipesIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newMealPlan = MealPlan(date)

            AppDatabase(getApplication()).recipeIngredientsRefDao()
                .upsertMealPlan(newMealPlan, selectedRecipesIds,oldSelectedRecipesIds)

        }
    }
}