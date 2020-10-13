package com.barryalan.kitchenmanager13.view.mealPlanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.util.communication.RecipeOnClickListener
import com.barryalan.kitchenmanager13.viewmodel.mealPlanner.MealPlannerCalendarViewModel
import kotlinx.android.synthetic.main.fragment_calendar.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.ExperimentalTime

class CalendarFragment : Fragment(), RecipeOnClickListener {

    private lateinit var mealListAdapter: MealListAdapter
    private var mSelectedDate: String = "0"
    private lateinit var viewModel: MealPlannerCalendarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            Navigation.findNavController(requireView())
                .navigate(CalendarFragmentDirections.actionCalendarToHomeScreenFragment())
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    @ExperimentalTime
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MealPlannerCalendarViewModel::class.java)
        initRecyclerView()
        subscribeObservers()

        arguments?.let {
            //get date from bundle
            mSelectedDate = CalendarFragmentArgs.fromBundle(it).selectedDate
        }
        setCorrectCalendarDate()

        cv_mealPlanner.setOnDateChangeListener { calendarView, year, month, day ->
            mSelectedDate = "$year/${month + 1}/$day"
            viewModel.refresh(mSelectedDate)
        }


    }

    @SuppressLint("SimpleDateFormat")
    private fun setCorrectCalendarDate() {
        val sdf = SimpleDateFormat("yyyy/MM/dd")
        if (mSelectedDate == "0") {//There was no selectedDate passed to this fragment
            val date = Date(cv_mealPlanner.date)
            mSelectedDate = sdf.format(date)

        } else {//we got an actual date

            //convert the date from this format to epoch time to be able to set the calendar view
            val date = sdf.parse(mSelectedDate)
            val startDate = date!!.time
            cv_mealPlanner.date = startDate
        }
        viewModel.refresh(mSelectedDate)

    }

    private fun subscribeObservers() {
        viewModel.mealsLiveData.observe(viewLifecycleOwner, Observer { meals ->
            meals?.let {
                mealListAdapter.updateRecipeList(meals)
            }
        })
    }

    private fun initRecyclerView() {
        rv_meals.apply {
            mealListAdapter = MealListAdapter(arrayListOf(), this@CalendarFragment)
            layoutManager = GridLayoutManager(context, 3)
            adapter = mealListAdapter
            mealListAdapter.updateRecipeList(mutableListOf())
        }
    }

    override fun onClick(recipeID: Long) {
        //if the id has not been initialized aka you pressed the AddNewRecipe card
        if (recipeID == 0L) {
            val action = CalendarFragmentDirections.actionCalendarToRecipePickerFragment()
            action.selectedDate = mSelectedDate
            Navigation.findNavController(requireView()).navigate(action)
        } else {

        }
    }

}


