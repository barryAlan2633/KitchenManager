package com.barryalan.kitchenmanager13.view.ingredient

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.viewmodel.IngredientListViewModel
import kotlinx.android.synthetic.main.fragment_ingredient_list.*

class IngredientListFragment : Fragment() {

    private val ingredientListAdapter = IngredientListAdapter(arrayListOf())
    private lateinit var viewModel: IngredientListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            Navigation.findNavController(requireView())
                .navigate(IngredientListFragmentDirections.actionIngredientListFragmentToHomeScreenFragment())
        }

        // The callback can be enabled or disabled here or in the lambda

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ingredient_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(IngredientListViewModel::class.java)
        viewModel.refresh()
        initRecyclerView()
        initRefreshLayout()
        subscribeObservers()
    }

    private fun initRecyclerView() {
        rv_ingredientList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ingredientListAdapter
        }
    }

    private fun initRefreshLayout() {
        refreshLayout.setOnRefreshListener {
            rv_ingredientList.visibility = View.GONE
            listError.visibility = View.GONE
            loadingView.visibility = View.VISIBLE
            viewModel.refresh()
            refreshLayout.isRefreshing = false
        }
    }

    private fun subscribeObservers() {
        viewModel.ingredientsLiveData.observe(viewLifecycleOwner, Observer { ingredients ->
            ingredients?.let {
                rv_ingredientList.visibility = View.VISIBLE
                ingredientListAdapter.updateIngredientList(ingredients)
            }
        })
        viewModel.ingredientLoadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                listError.visibility = if (it) View.VISIBLE else View.GONE
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                if (it) {
                    loadingView.visibility = View.VISIBLE
                    rv_ingredientList.visibility = View.GONE
                    listError.visibility = View.GONE
                } else loadingView.visibility = View.GONE
            }
        })
    }
}
