package com.barryalan.kitchenmanager13.view.shared

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation

import com.barryalan.kitchenmanager13.R
import kotlinx.android.synthetic.main.fragment_home_screen.*

class HomeScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnRecipes.setOnClickListener {
            Navigation.findNavController(it).navigate(HomeScreenFragmentDirections.actionHomeScreenFragmentToRecipeListFragment())
        }

        btnIngredients.setOnClickListener {
            Navigation.findNavController(it).navigate(HomeScreenFragmentDirections.actionHomeScreenFragmentToIngredientListFragment())
        }

        btnMealPlanner.setOnClickListener {
            Navigation.findNavController(it).navigate(HomeScreenFragmentDirections.actionHomeScreenFragmentToCalendar())
        }

        btnGroceries.setOnClickListener {
            Navigation.findNavController(it).navigate(HomeScreenFragmentDirections.actionHomeScreenFragmentToGroceriesCalendar())
        }
    }


}
