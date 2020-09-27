package com.barryalan.kitchenmanager13.view.recipe

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.Ingredient
import com.barryalan.kitchenmanager13.model.Recipe
import com.barryalan.kitchenmanager13.model.RecipeWithIngredients
import com.barryalan.kitchenmanager13.util.getProgressDrawable
import com.barryalan.kitchenmanager13.util.loadImage
import com.barryalan.kitchenmanager13.view.ingredient.IngredientListAdapter
import com.barryalan.kitchenmanager13.view.shared.CameraActivity
import com.barryalan.kitchenmanager13.viewmodel.RecipeNewEditViewModel
import kotlinx.android.synthetic.main.fragment_recipe_new_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class RecipeNewEditFragment : Fragment() {

    private val ingredientListAdapter = IngredientListAdapter(arrayListOf())
    private lateinit var viewModel: RecipeNewEditViewModel

    private var mRecipeToEditUID: Long = -1L
    private var mRecipeImageURIString: String? = null
    private var mIngredientImageURIString: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_new_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(RecipeNewEditViewModel::class.java)
        arguments?.let {
            mRecipeToEditUID = RecipeNewEditFragmentArgs.fromBundle(it).recipeUID
            viewModel.fetch(mRecipeToEditUID)
        }
        initRecyclerView()
        subscribeObservers()


        btn_addIngredient.setOnClickListener {

            //create new object with the input fields
            val newIngredient = Ingredient(et_newIngredientName.text.toString(), mIngredientImageURIString)

            //add to recyclerview
            ingredientListAdapter.addIngredientItem(newIngredient)

            //clear fields
            et_newIngredientName.text.clear()
            img_newIngredient.setImageResource(R.drawable.ic_error_black_24dp)
        }

        btn_cancel.setOnClickListener {
            if (mRecipeToEditUID == -1L) { //User is in this fragment to make a new recipe
                Navigation.findNavController(it)
                    .navigate(RecipeNewEditFragmentDirections.actionRecipeNewEditFragmentToRecipeListFragment())
            } else {//User is in this fragment to edit an existing recipe
                Navigation.findNavController(it)
                    .navigate(RecipeNewEditFragmentDirections.actionNewEditFragmentToRecipeDetailFragment())
            }
        }

        btn_updateSaveRecipe.setOnClickListener {

            if (mRecipeToEditUID == -1L) { //User is in this fragment to make a new recipe
                Toast.makeText(context,"saved",Toast.LENGTH_SHORT).show()

                //create new recipeWithIngredients item
                val newRecipeWithIngredients = RecipeWithIngredients(
                    Recipe(tv_recipeName.text.toString(), mRecipeImageURIString),
                    ingredientListAdapter.getIngredientList()
                )

                lifecycleScope.launch(Dispatchers.Main) {
                    //save item to database
                    val savingJob = viewModel.saveRecipeWithIngredients(newRecipeWithIngredients)

                    //block the current co-routine until the job is completed
                    savingJob.join()

                    //navigate to back to recipe list
                    Navigation.findNavController(it)
                        .navigate(RecipeNewEditFragmentDirections.actionRecipeNewEditFragmentToRecipeListFragment())
                }
            } else {//User is in this fragment to edit an existing recipe
                Toast.makeText(context,"updated",Toast.LENGTH_SHORT).show()

                //TODO probably can refactor this out of the if statement and only write it once, rename it
                //create new recipeWithIngredients item with data from screen
                val updatedRecipeWithIngredients = RecipeWithIngredients(
                    Recipe(tv_recipeName.text.toString(), mRecipeImageURIString),
                    ingredientListAdapter.getIngredientList()
                )

                lifecycleScope.launch(Dispatchers.Main) {
                    //update item in database
                    val updatingJob = viewModel.updateRecipeWithIngredients(updatedRecipeWithIngredients)

                    //block the current co-routine until the job is completed
                    updatingJob.join()

                    //navigate to back to recipe list
                    val action = RecipeNewEditFragmentDirections.actionNewEditFragmentToRecipeDetailFragment()
                    action.recipeUID = mRecipeToEditUID
                    Navigation.findNavController(it)
                        .navigate(action)
                }
            }
        }

        img_recipe.setOnClickListener{
            val intent = Intent(activity, CameraActivity::class.java)

            startActivityForResult(intent, 1)
        }

        img_newIngredient.setOnClickListener{
            val intent = Intent(activity, CameraActivity::class.java)

            startActivityForResult(intent, 2)
        }
    }

    private fun initRecyclerView() {
        rv_ingredientList.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = ingredientListAdapter
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
                ingredientListAdapter.removeItem(viewHolder)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rv_ingredientList)
    }

    private fun subscribeObservers() {
        viewModel.recipeToUpdateLiveData.observe(viewLifecycleOwner, Observer { recipeWithIngredients ->
                recipeWithIngredients?.let {
                    tv_recipeName.setText(recipeWithIngredients.recipe.name)

                recipeWithIngredients.recipe.image?.let {
                    img_recipe.loadImage(Uri.parse(it), getProgressDrawable(requireContext()))
                    mRecipeImageURIString = it
                }

                    ingredientListAdapter.updateIngredientList(recipeWithIngredients.ingredients)
                }
            })
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1 -> {
                    data?.let {intent->
                        mRecipeImageURIString = intent.extras?.get("photoURI").toString()

                        img_recipe.loadImage(
                            intent.extras?.get("photoURI") as Uri?,
                            getProgressDrawable(requireContext())
                        )
                    }
                }

                2 -> {
                    data?.let {intent->
                        mIngredientImageURIString = intent.extras?.get("photoURI").toString()

                        img_newIngredient.loadImage(
                            intent.extras?.get("photoURI") as Uri?,
                            getProgressDrawable(requireContext())
                        )
                    }
                }
            }
        }
    }

}





