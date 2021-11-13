package com.barryalan.kitchenmanager13.view.groceries

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.viewmodel.groceries.GroceryListViewModel
import com.barryalan.kitchenmanager13.viewmodel.shared.BaseViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.fragment_grocery_list.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class GroceryListFragment : Fragment() {

    private lateinit var viewModel: GroceryListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the  layout for this fragment
        return inflater.inflate(R.layout.fragment_grocery_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(GroceryListViewModel::class.java)

        btn_selectDates.setOnClickListener {
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            builder.setTitleText("Select a date range")
            val picker = builder.build()
            picker.show(requireActivity().supportFragmentManager, picker.toString())

            picker.addOnPositiveButtonClickListener {

                val sdf = SimpleDateFormat("MMM EEE dd, yyyy", Locale.getDefault())
                val startDate = sdf.format(Date(it.first!!))
                val endDate = sdf.format(Date(it.second!!))

                Toast.makeText(context,"$startDate -\n$endDate" ,Toast.LENGTH_SHORT).show()

                tv_selectedDates.text = "$startDate -\n$endDate"

                viewModel.refresh(it.first!!,it.second!!)
            }

            subscribeObservers()
        }


    }

    private fun subscribeObservers() {
        viewModel.groceryListLiveData.observe(viewLifecycleOwner, Observer {groceryList->
            groceryList?.let{
                Log.d("wack",it.toString())
                Toast.makeText(context,it.toString(),Toast.LENGTH_SHORT).show()
            }
        })
    }

}
