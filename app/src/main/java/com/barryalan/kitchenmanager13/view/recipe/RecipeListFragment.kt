package com.barryalan.kitchenmanager13.view.recipe

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.view.shared.BaseFragment
import com.barryalan.kitchenmanager13.viewmodel.recipe.RecipeListViewModel
import kotlinx.android.synthetic.main.fragment_recipe_list.*
import kotlinx.android.synthetic.main.fragment_recipe_list.listError
import kotlinx.android.synthetic.main.fragment_recipe_list.loadingView
import kotlinx.android.synthetic.main.fragment_recipe_list.refreshLayout


class RecipeListFragment : BaseFragment() {

    private val recipeListAdapter = RecipeListAdapter(arrayListOf())
    private lateinit var viewModel: RecipeListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_list, container, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            Navigation.findNavController(requireView()).navigate(RecipeListFragmentDirections.actionRecipeListFragmentToHomeScreenFragment())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecipeListViewModel::class.java)
        viewModel.refresh()
        initRecyclerView()
        initRefreshLayout()
        subscribeObservers()
    }

    private fun initRecyclerView() {
        rv_recipeList.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = recipeListAdapter
        }

        recipe_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                recipeListAdapter.filter.filter(newText)
                return false
            }

        })
        val searchIcon = recipe_search.findViewById<ImageView>(R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.WHITE)

        val cancelIcon = recipe_search.findViewById<ImageView>(R.id.search_close_btn)
        cancelIcon.setColorFilter(Color.WHITE)

        val textView = recipe_search.findViewById<TextView>(R.id.search_src_text)
        textView.setTextColor(Color.WHITE)
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
