package com.barryalan.kitchenmanager13.view.ingredient

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
import com.barryalan.kitchenmanager13.util.communication.AreYouSureCallBack
import com.barryalan.kitchenmanager13.util.communication.OnClickListener
import com.barryalan.kitchenmanager13.util.communication.UIMessage
import com.barryalan.kitchenmanager13.util.communication.UIMessageType
import com.barryalan.kitchenmanager13.view.shared.BaseFragment
import com.barryalan.kitchenmanager13.viewmodel.IngredientListViewModel
import kotlinx.android.synthetic.main.fragment_ingredient_list.*

class IngredientListFragment : BaseFragment(),OnClickListener {

    private lateinit var ingredientListAdapter:IngredientWithRecipesListAdapter
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
            layoutManager = GridLayoutManager(context, 3)
            ingredientListAdapter = IngredientWithRecipesListAdapter(ArrayList(),this@IngredientListFragment)
            adapter = ingredientListAdapter


        }

        ingredient_with_recipes_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                ingredientListAdapter.filter.filter(newText)
                return false
            }

        })
        val searchIcon = ingredient_with_recipes_search.findViewById<ImageView>(R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.WHITE)

        val cancelIcon = ingredient_with_recipes_search.findViewById<ImageView>(R.id.search_close_btn)
        cancelIcon.setColorFilter(Color.WHITE)

        val textView = ingredient_with_recipes_search.findViewById<TextView>(R.id.search_src_text)
        textView.setTextColor(Color.WHITE)

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
        viewModel.ingredientWithRecipesListLiveData.observe(
            viewLifecycleOwner,
            Observer { ingredients ->
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

    //onClick for deleting ingredients that belong to zero recipes
    override fun onClick(ingredientID:Long) {
        val callback: AreYouSureCallBack = object :
            AreYouSureCallBack {
            override fun proceed() {
                viewModel.deleteIngredient(ingredientID)
                viewModel.refresh()
            }

            override fun cancel() {
                //Do nothing
            }
        }

        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                "Are you sure you wish to delete this ingredient? This action cannot be un-done",
                UIMessageType.AreYouSureDialog(callback)
            )
        )


    }
}
