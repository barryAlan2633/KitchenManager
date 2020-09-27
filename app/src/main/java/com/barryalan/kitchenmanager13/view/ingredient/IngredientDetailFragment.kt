package com.barryalan.kitchenmanager13.view.ingredient

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.Ingredient
import com.barryalan.kitchenmanager13.util.communication.AreYouSureCallBack
import com.barryalan.kitchenmanager13.util.communication.UIMessage
import com.barryalan.kitchenmanager13.util.communication.UIMessageType
import com.barryalan.kitchenmanager13.util.getProgressDrawable
import com.barryalan.kitchenmanager13.util.loadImage
import com.barryalan.kitchenmanager13.view.shared.BaseFragment
import com.barryalan.kitchenmanager13.view.shared.CameraActivity
import com.barryalan.kitchenmanager13.viewmodel.IngredientDetailViewModel
import kotlinx.android.synthetic.main.fragment_ingredient_detail.*
import kotlinx.android.synthetic.main.fragment_ingredient_detail.btn_cancel
import kotlinx.android.synthetic.main.fragment_ingredient_detail.et_ingredientName
import kotlinx.android.synthetic.main.fragment_recipe_new_edit.*
import kotlinx.android.synthetic.main.item_ingredient.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class IngredientDetailFragment : BaseFragment() {

    private lateinit var viewModel: IngredientDetailViewModel
    private var mIngredientImageURIString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            confirmBackNavigation(requireView())
        }

        // The callback can be enabled or disabled here or in the lambda

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ingredient_detail, container, false)
    }

    @ExperimentalStdlibApi
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
            if (et_ingredientName.text.isEmpty()) {
                uiCommunicationListener.onUIMessageReceived(
                    UIMessage(
                        "Ingredient must have a name",
                        UIMessageType.ErrorDialog()
                    )
                )
            } else {
                saveIngredient(it)
            }
        }

        img_ingredientDetail.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)

            startActivityForResult(intent, 2)
        }
    }

    private fun confirmBackNavigation(view: View) {
        val callback: AreYouSureCallBack = object :
            AreYouSureCallBack {
            override fun proceed() {
                Navigation.findNavController(view).navigate(
                    IngredientDetailFragmentDirections.actionIngredientDetailFragmentToIngredientListFragment()
                )
            }

            override fun cancel() {
                //Do nothing
            }
        }

        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                "Are you sure you want to go back? Any information that was not saved on this page will be forever lost",
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }

    private fun saveIngredient(view: View) {
        lifecycleScope.launch(Dispatchers.Main) {

            //create new ingredient object from UI fields
            val updatedIngredient =
                Ingredient(et_ingredientName.text.toString().trim(), mIngredientImageURIString)

            //update ingredient in the database
            val updateJob = viewModel.updateIngredient(updatedIngredient)

            //block the current co-routine until the job is completed
            updateJob.join()

            //navigate back to ingredientList fragment
            Navigation.findNavController(view)
                .navigate(IngredientDetailFragmentDirections.actionIngredientDetailFragmentToIngredientListFragment())

        }
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


    @ExperimentalStdlibApi
    private fun subscribeObservers() {
        viewModel.selectedIngredientLiveData.observe(viewLifecycleOwner, Observer { ingredient ->
            ingredient?.let { it ->
                et_ingredientName.setText(ingredient.name.capitalize(Locale.ROOT))
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
}
