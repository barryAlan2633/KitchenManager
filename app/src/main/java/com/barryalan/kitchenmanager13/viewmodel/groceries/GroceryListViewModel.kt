package com.barryalan.kitchenmanager13.viewmodel.groceries

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barryalan.kitchenmanager13.model.AppDatabase
import com.barryalan.kitchenmanager13.model.Ingredient
import com.barryalan.kitchenmanager13.model.MealPlan
import com.barryalan.kitchenmanager13.viewmodel.shared.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroceryListViewModel(application: Application) : BaseViewModel(application) {

    //    val mealPlanLiveData = MutableLiveData<MealPlan>()
    val groceryListLiveData = MutableLiveData<List<Ingredient>>()

    fun refresh(startDate: Long, endDate: Long) {
        retrieveMealPlanFromDB(startDate, endDate)
    }

    private fun retrieveMealPlanFromDB(startDate: Long, endDate: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val groceryList = AppDatabase(getApplication()).recipeIngredientsRefDao()
                .getGroceryList(startDate, endDate)

            withContext(Dispatchers.Main) {
                groceryListLiveData.value = groceryList
            }
        }
    }
}