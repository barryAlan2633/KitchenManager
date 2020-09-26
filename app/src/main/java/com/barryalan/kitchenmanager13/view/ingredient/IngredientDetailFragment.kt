package com.barryalan.kitchenmanager13.view.ingredient

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.Ingredient
import com.barryalan.kitchenmanager13.util.getProgressDrawable
import com.barryalan.kitchenmanager13.util.loadImage
import com.barryalan.kitchenmanager13.view.shared.CameraActivity
import com.barryalan.kitchenmanager13.viewmodel.IngredientDetailViewModel
import kotlinx.android.synthetic.main.fragment_ingredient_detail.*
import kotlinx.android.synthetic.main.fragment_ingredient_detail.btn_cancel
import kotlinx.android.synthetic.main.fragment_ingredient_detail.et_ingredientName
import kotlinx.android.synthetic.main.fragment_recipe_new_edit.*
import kotlinx.android.synthetic.main.item_ingredient.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IngredientDetailFragment : Fragment() {

    private lateinit var viewModel: IngredientDetailViewModel
    private var mIngredientImageURIString: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ingredient_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(IngredientDetailViewModel::class.java)

        arguments?.let {
            val selectedRecipeID = IngredientDetailFragmentArgs.fromBundle(it).ingredientUID
            viewModel.fetch(selectedRecipeID)
        }
        subscribeObservers()

        btn_cancel.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(IngredientDetailFragmentDirections.actionIngredientDetailFragmentToIngredientListFragment())
        }

        btn_saveIngredient.setOnClickListener {

            lifecycleScope.launch(Dispatchers.Main) {

                //create new ingredient object from UI fields
                val updatedIngredient = Ingredient(et_ingredientName.text.toString(), mIngredientImageURIString)

                //update ingredient in the database
                val updateJob = viewModel.updateIngredient(updatedIngredient)

                //block the current co-routine until the job is completed
                updateJob.join()

                //navigate back to ingredientList fragment
                Navigation.findNavController(it)
                    .navigate(IngredientDetailFragmentDirections.actionIngredientDetailFragmentToIngredientListFragment())

            }
        }

        img_ingredientDetail.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)

            startActivityForResult(intent, 2)
        }
    }

    private fun subscribeObservers() {
        viewModel.selectedIngredientLiveData.observe(viewLifecycleOwner, Observer { ingredient ->
            ingredient?.let { it ->
                et_ingredientName.setText(ingredient.name)
                it.image?.let { imageURI ->
                    mIngredientImageURIString = imageURI
                    img_ingredientDetail.loadImage(
                        Uri.parse(imageURI),
                        getProgressDrawable(requireContext())
                    )
                }
            }
        })
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                2 -> {
                    data?.let { intent ->
                        mIngredientImageURIString = intent.extras?.get("photoURI").toString()

                        img_ingredientDetail.loadImage(
                            intent.extras?.get("photoURI") as Uri?,
                            getProgressDrawable(requireContext())
                        )
                    }
                }
            }
        }
    }
}
