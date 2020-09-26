package com.barryalan.kitchenmanager13.view.recipe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.viewmodel.RecipeListViewModel
import kotlinx.android.synthetic.main.fragment_recipe_list.*


class RecipeListFragment : Fragment() {

    private val recipeListAdapter = RecipeListAdapter(arrayListOf())
    private lateinit var viewModel: RecipeListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecipeListViewModel::class.java)
        viewModel.refresh()
        initRecyclerView()
        initRefreshLayout()
        subscribeObservers()

        ab_editRecipe.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(RecipeListFragmentDirections.actionRecipeListFragmentToNewEditFragment())
        }
    }

    private fun initRecyclerView() {
        rv_recipeList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeListAdapter
        }


        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                recipeListAdapter.removeItem(viewHolder)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rv_recipeList)

    }

    private fun initRefreshLayout() {
        refreshLayout.setOnRefreshListener {
            rv_recipeList.visibility = View.GONE
            listError.visibility = View.GONE
            loadingView.visibility = View.VISIBLE
            viewModel.refresh()
            refreshLayout.isRefreshing = false
        }
    }

    private fun subscribeObservers() {
        viewModel.recipesLiveData.observe(viewLifecycleOwner, Observer { recipes ->
            recipes?.let {
                rv_recipeList.visibility = View.VISIBLE
                recipeListAdapter.updateRecipeList(recipes)
            }
        })
        viewModel.recipeLoadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let {
                listError.visibility = if (it) View.VISIBLE else View.GONE
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                if (it) {
                    loadingView.visibility = View.VISIBLE
                    rv_recipeList.visibility = View.GONE
                    listError.visibility = View.GONE
                } else loadingView.visibility = View.GONE
            }
        })
    }
}
