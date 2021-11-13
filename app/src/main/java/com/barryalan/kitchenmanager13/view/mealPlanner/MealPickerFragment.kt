package com.barryalan.kitchenmanager13.view.mealPlanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager

import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.util.communication.AreYouSureCallBack
import com.barryalan.kitchenmanager13.util.communication.UIMessage
import com.barryalan.kitchenmanager13.util.communication.UIMessageType
import com.barryalan.kitchenmanager13.view.shared.BaseFragment
import com.barryalan.kitchenmanager13.viewmodel.mealPlanner.MealPickerViewModel
import kotlinx.android.synthetic.main.fragment_meal_picker.*

class MealPickerFragment : BaseFragment() {

    private lateinit var viewModel: MealPickerViewModel
    private val recipePickerAdapter = MealPickerAdapter(arrayListOf())
    private var mSelectedDate: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This callback will only be called when MyFragment is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            confirmCancel(requireView())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meal_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MealPickerViewModel::class.java)
        arguments?.let {
            mSelectedDate = MealPickerFragmentArgs.fromBundle(it).selectedDate
            viewModel.refresh(mSelectedDate)

        }
        initRecyclerView()
        subscribeObservers()

        btn_cancelMealSelection.setOnClickListener {
            confirmCancel(it)
        }

        btn_saveMeals.setOnClickListener {
            if (mSelectedDate != "0") {

                //save meals
                viewModel.saveMeals(
                    mSelectedDate,
                    recipePickerAdapter.getSelectedRecipesIDs(),
                    recipePickerAdapter.getOldSelectedRecipesIDs()
                )

                //Navigate back
                val action = MealPickerFragmentDirections.actionRecipePickerFragmentToCalendar()
                action.selectedDate = mSelectedDate
                Navigation.findNavController(it).navigate(action)
            }
        }
    }

    private fun confirmCancel(view: View) {
        val callback: AreYouSureCallBack = object :
            AreYouSureCallBack {
            override fun proceed() {
                val action =
                    MealPickerFragmentDirections.actionRecipePickerFragmentToCalendar()
                action.selectedDate = mSelectedDate
                Navigation.findNavController(view).navigate(action)
            }

            override fun cancel() {
                //Do nothing
            }
        }

        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                "Are you sure you want to cancel? Any changes will be lost forever!",
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }

    private fun initRecyclerView() {
        rv_recipePicker.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = recipePickerAdapter
        }
    }

    private fun subscribeObservers() {
        viewModel.allRecipesLiveData.observe(viewLifecycleOwner, Observer { recipes ->
            recipes?.let {
                rv_recipePicker.visibility = View.VISIBLE
                recipePickerAdapter.updateRecipeList(recipes)
            }
        })

        viewModel.recipeLoadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                pickerError.visibility = if (it) View.VISIBLE else View.GONE
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                if (it) {
                    pickerLoadingView.visibility = View.VISIBLE
                    rv_recipePicker.visibility = View.GONE
                    pickerError.visibility = View.GONE
                } else pickerLoadingView.visibility = View.GONE
            }

        })

        viewModel.selectedRecipesLiveData.observe(viewLifecycleOwner, Observer { recipes ->
            recipes?.let {
                recipePickerAdapter.updateSelectedRecipes(recipes)
            }
        })
    }

}
