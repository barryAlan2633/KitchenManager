package com.barryalan.kitchenmanager13.view.recipe

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.Amount
import com.barryalan.kitchenmanager13.model.Ingredient
import com.barryalan.kitchenmanager13.model.Recipe
import com.barryalan.kitchenmanager13.model.RecipeWithIngredients
import com.barryalan.kitchenmanager13.util.communication.AreYouSureCallBack
import com.barryalan.kitchenmanager13.util.communication.UIMessage
import com.barryalan.kitchenmanager13.util.communication.UIMessageType
import com.barryalan.kitchenmanager13.util.getProgressDrawable
import com.barryalan.kitchenmanager13.util.loadImage
import com.barryalan.kitchenmanager13.view.ingredient.IngredientListAdapter
import com.barryalan.kitchenmanager13.view.shared.BaseFragment
import com.barryalan.kitchenmanager13.view.shared.CameraActivity
import com.barryalan.kitchenmanager13.viewmodel.recipe.RecipeNewEditViewModel
import kotlinx.android.synthetic.main.fragment_recipe_new_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

open class RecipeNewEditFragment : BaseFragment(), AdapterView.OnItemSelectedListener {

    private val ingredientListAdapter = IngredientListAdapter(arrayListOf())
    private lateinit var viewModel: RecipeNewEditViewModel

    private var mSelectedRecipeType: String? = null
    private var mRecipeToEditUID: Long = -1L
    private var mRecipeImageURIString: String? = null
    private var mIngredientImageURIString: String? = null
    private var mIngredientSelectedUnit: String = "Unit"
    private var mIngredientList: MutableList<Ingredient> = mutableListOf()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            confirmBackNavigation(requireView())
        }

        // The callback can be enabled or disabled here or in the lambda

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_new_edit, container, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RecipeNewEditViewModel::class.java)
        viewModel.fetchIngredientList()
        arguments?.let {
            mRecipeToEditUID = RecipeNewEditFragmentArgs.fromBundle(it).recipeUID
            viewModel.fetchSelectedRecipeWI(mRecipeToEditUID)
        }
        initRecyclerView()
        initRecipeTypeSpinner()
        initUnitSpinner()
        initSelectIngredientSpinner()
        subscribeObservers()

        btn_addIngredient.setOnClickListener {
            when {
                et_newIngredientName.text.isEmpty() -> {
                    uiCommunicationListener.onUIMessageReceived(
                        UIMessage(
                            "Ingredient must have a name",
                            UIMessageType.ErrorDialog()
                        )
                    )
                }
                et_newIngredientAmount.text.isEmpty() -> {
                    uiCommunicationListener.onUIMessageReceived(
                        UIMessage(
                            "Ingredient must have an amount",
                            UIMessageType.ErrorDialog()
                        )
                    )
                }
                else -> {
                    //create new object with the input fields
                    val newIngredient =
                        Ingredient(
                            et_newIngredientName.text.toString().trim().capitalize(Locale.ROOT),
                            mIngredientImageURIString
                        )
                    val newAmount =
                        Amount(
                            et_newIngredientAmount.text.toString().toFloat(),
                            sp_newIngredientUnit.selectedItem.toString()
                        )

                    //add to recyclerview
                    ingredientListAdapter.addIngredientItem(newIngredient, newAmount)

                    //clear fields
                    et_newIngredientName.text.clear()
                    et_newIngredientAmount.text.clear()
                    img_newIngredient.setImageResource(R.drawable.ic_error_outline_white_24dp)
                    mIngredientImageURIString = null
                }
            }
        }

        btn_cancel.setOnClickListener { v ->
            confirmBackNavigation(v)
        }

        btn_updateSaveRecipe.setOnClickListener {

            //Check to make sure there is a name on the recipe
            if (tv_recipeName.text.isEmpty()) {
                uiCommunicationListener.onUIMessageReceived(
                    UIMessage(
                        "Recipe must have a name",
                        UIMessageType.ErrorDialog()
                    )
                )
            } else if (mSelectedRecipeType == null) {
                uiCommunicationListener.onUIMessageReceived(
                    UIMessage(
                        "Recipe must have a type. (Drink or Food)",
                        UIMessageType.ErrorDialog()
                    )
                )
            } else {
                if (mRecipeToEditUID == -1L) { //User is in this fragment to make a new recipe
                    saveNewRecipe(it)
                } else {//User is in this fragment to edit an existing recipe
                    updateRecipe(it)
                }
            }

        }

        img_meal.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)

            startActivityForResult(intent, 1)
        }

        img_newIngredient.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)

            startActivityForResult(intent, 2)
        }

    }

    private fun initRecyclerView() {
        rv_ingredientList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
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

    private fun initRecipeTypeSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.recipe_type,
            R.layout.item_spinner
        ).also { adapter ->

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Apply the adapter to the spinner
            sp_newRecipeType.adapter = adapter
            sp_newRecipeType.onItemSelectedListener = this

        }
    }

    private fun initUnitSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.units,
            android.R.layout.simple_spinner_item
        ).also { adapter ->

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Apply the adapter to the spinner
            sp_newIngredientUnit.adapter = adapter
            sp_newIngredientUnit.onItemSelectedListener = this

        }
    }

    private fun initSelectIngredientSpinner() {
        adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_small,
            mutableListOf("No items")
        ).also { adapter ->

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Apply the adapter to the spinner
            sp_selectIngredient.adapter = adapter
            sp_selectIngredient.onItemSelectedListener = this

        }
    }

    @ExperimentalStdlibApi
    private fun saveNewRecipe(view: View) {
        Toast.makeText(context, "saved", Toast.LENGTH_SHORT).show()

        //create new recipeWithIngredients item
        val newRecipeWithIngredients = RecipeWithIngredients(
            Recipe(
                tv_recipeName.text.toString().trim(),
                mRecipeImageURIString,
                mSelectedRecipeType!!
            ),
            ingredientListAdapter.getIngredientList(),
            ingredientListAdapter.getAmountsList()
        )

        lifecycleScope.launch(Dispatchers.Main) {
            //save item to database
            val savingJob = viewModel.saveRecipeWithIngredients(newRecipeWithIngredients)

            //block the current co-routine until the job is completed
            savingJob.join()

            //navigate to back to recipe list
            Navigation.findNavController(view)
                .navigate(RecipeNewEditFragmentDirections.actionRecipeNewEditFragmentToRecipeListFragment())
        }
    }

    @ExperimentalStdlibApi
    private fun updateRecipe(view: View) {
        Toast.makeText(context, "updated", Toast.LENGTH_SHORT).show()

        //TODO probably can refactor this out of the if statement and only write it once, rename it
        //create new recipeWithIngredients item with data from screen
        val updatedRecipeWithIngredients = RecipeWithIngredients(
            Recipe(
                tv_recipeName.text.toString().trim(),
                mRecipeImageURIString,
                mSelectedRecipeType!!
            ),
            ingredientListAdapter.getIngredientList(),
            ingredientListAdapter.getAmountsList()
        )

        lifecycleScope.launch(Dispatchers.Main) {
            //update item in database
            val updatingJob =
                viewModel.updateRecipeWithIngredients(updatedRecipeWithIngredients)

            //block the current co-routine until the job is completed
            updatingJob.join()

            //navigate to back to recipe list
            val action =
                RecipeNewEditFragmentDirections.actionNewEditFragmentToRecipeDetailFragment()
            action.recipeUID = mRecipeToEditUID
            Navigation.findNavController(view)
                .navigate(action)
        }
    }

    //handle result of image taken
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1 -> {
                    data?.let { intent ->
                        mRecipeImageURIString = intent.extras?.get("photoURI").toString()

                        img_meal.loadImage(
                            intent.extras?.get("photoURI") as Uri?,
                            getProgressDrawable(requireContext())
                        )
                    }
                }

                2 -> {
                    data?.let { intent ->
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

    private fun confirmBackNavigation(view: View) {
        val callback: AreYouSureCallBack = object :
            AreYouSureCallBack {
            override fun proceed() {
                if (mRecipeToEditUID == -1L) { //User is in this fragment to make a new recipe
                    Navigation.findNavController(view)
                        .navigate(RecipeNewEditFragmentDirections.actionRecipeNewEditFragmentToRecipeListFragment())
                } else {//User is in this fragment to edit an existing recipe
                    val action =
                        RecipeNewEditFragmentDirections.actionNewEditFragmentToRecipeDetailFragment()
                    action.recipeUID = mRecipeToEditUID
                    Navigation.findNavController(view)
                        .navigate(action)
                }
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

    override fun onNothingSelected(parent: AdapterView<*>?) {
        when (parent!!.id) {
            R.id.sp_newIngredientUnit -> {

            }

            R.id.sp_selectIngredient -> {

            }

            R.id.sp_newRecipeType -> {

            }
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, p3: Long) {

        when (parent!!.id) {
            R.id.sp_newIngredientUnit -> {
                mIngredientSelectedUnit = parent.getItemAtPosition(position).toString()

            }

            R.id.sp_selectIngredient -> {
                if (position != 0) {

                    //-1 is the offset for the "nothing selected" option
                    et_newIngredientName.setText(mIngredientList[position - 1].name)
                    mIngredientImageURIString = mIngredientList[position - 1].image

                    mIngredientImageURIString?.let {
                        img_newIngredient.loadImage(
                            Uri.parse(mIngredientImageURIString),
                            getProgressDrawable(requireContext())
                        )
                    }
                }
            }

            R.id.sp_newRecipeType -> {
                if(position != 0){
                    mSelectedRecipeType = parent.getItemAtPosition(position).toString()
                }
            }
        }
    }

    @ExperimentalStdlibApi
    private fun subscribeObservers() {
        viewModel.recipeToUpdateLiveData.observe(
            viewLifecycleOwner,
            Observer { recipeWithIngredients ->
                recipeWithIngredients?.let {
                    tv_recipeName.setText(recipeWithIngredients.recipe.name.capitalize(Locale.ROOT))

                    if(recipeWithIngredients.recipe.type == "Drink"){
                        sp_newRecipeType.setSelection(1)
                    }else{
                        sp_newRecipeType.setSelection(2)
                    }

                    recipeWithIngredients.recipe.image?.let {
                        img_meal.loadImage(
                            Uri.parse(it),
                            getProgressDrawable(requireContext())
                        )
                        mRecipeImageURIString = it
                    }

                    ingredientListAdapter.updateIngredientList(
                        recipeWithIngredients.ingredients,
                        recipeWithIngredients.amounts
                    )
                }
            })

        viewModel.ingredientListLiveData.observe(viewLifecycleOwner, Observer { ingredientList ->
            ingredientList?.let {
                mIngredientList.clear()
                mIngredientList.addAll(ingredientList)
                val ingredientNames = mutableListOf("Nothing Selected")
                ingredientList.map { ingredient -> ingredientNames.add(ingredient.name) }
                adapter.clear()
                adapter.addAll(ingredientNames)
                adapter.notifyDataSetChanged()
            }
        })
    }
}





