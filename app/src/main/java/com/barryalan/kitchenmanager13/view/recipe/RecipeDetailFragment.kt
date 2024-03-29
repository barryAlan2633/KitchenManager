package com.barryalan.kitchenmanager13.view.recipe

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.util.communication.AreYouSureCallBack
import com.barryalan.kitchenmanager13.util.communication.UIMessage
import com.barryalan.kitchenmanager13.util.communication.UIMessageType
import com.barryalan.kitchenmanager13.util.getProgressDrawable
import com.barryalan.kitchenmanager13.util.loadCircleImage
import com.barryalan.kitchenmanager13.util.loadImage
import com.barryalan.kitchenmanager13.view.ingredient.IngredientListAdapter
import com.barryalan.kitchenmanager13.view.shared.BaseFragment
import com.barryalan.kitchenmanager13.viewmodel.recipe.RecipeDetailViewModel
import kotlinx.android.synthetic.main.fragment_recipe_detail.*
import kotlinx.android.synthetic.main.fragment_recipe_detail.ab_editRecipe
import java.util.*

class RecipeDetailFragment : BaseFragment() {

    private val ingredientListAdapter = IngredientListAdapter(arrayListOf())
    private lateinit var viewModel: RecipeDetailViewModel

    private var mSelectedRecipeID: Long = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            Navigation.findNavController(requireView())
                .navigate(RecipeDetailFragmentDirections.actionDetailFragmentToRecipeListFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecipeDetailViewModel::class.java)

        arguments?.let {
            mSelectedRecipeID = RecipeDetailFragmentArgs.fromBundle(it).recipeUID
            viewModel.fetch(mSelectedRecipeID)

        }
        initRecyclerView()
        subscribeObservers()

        ab_editRecipe.setOnClickListener {
            val action = RecipeDetailFragmentDirections.actionDetailFragmentToNewEditFragment()
            if (mSelectedRecipeID != 0L) {
                action.recipeUID = mSelectedRecipeID
                Navigation.findNavController(view).navigate(action)

            } else {
                Toast.makeText(context, "invalid action id: $mSelectedRecipeID", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        ab_deleteRecipe.setOnClickListener {
            val callback: AreYouSureCallBack = object :
                AreYouSureCallBack {
                override fun proceed() {
                    viewModel.deleteRecipeWithIngredients(mSelectedRecipeID)
                    Navigation.findNavController(it).navigate(RecipeDetailFragmentDirections.actionDetailFragmentToRecipeListFragment())
                }

                override fun cancel() {
                }

            }
            uiCommunicationListener.onUIMessageReceived(
                UIMessage(
                    "Are you sure you want to delete this recipe? This action cannot be un-done",
                    UIMessageType.AreYouSureDialog(callback)
                )
            )
        }
    }

    private fun initRecyclerView() {
        rv_ingredientList.apply {
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            adapter = ingredientListAdapter
        }
    }

    @ExperimentalStdlibApi
    private fun subscribeObservers() {
        viewModel.recipeWithIngredientsLiveData.observe(
            viewLifecycleOwner,
            Observer { recipeWithIngredients ->
                recipeWithIngredients?.let {
                    tv_recipeName.text = it.recipe.name.capitalize(Locale.ROOT)
                    tv_recipeType.text = it.recipe.type
                    it.recipe.image?.let { imageURI ->
                        img_recipe.loadImage(
                            Uri.parse(imageURI),
                            getProgressDrawable(requireContext())
                        )
                    }
                    ingredientListAdapter.updateIngredientList(it.ingredients, it.amounts)
                }
            })
    }
}
