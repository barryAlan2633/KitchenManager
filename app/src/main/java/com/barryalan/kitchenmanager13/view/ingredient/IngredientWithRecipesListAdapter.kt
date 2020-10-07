package com.barryalan.kitchenmanager13.view.ingredient

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.IngredientWithRecipes
import com.barryalan.kitchenmanager13.util.getProgressDrawable
import com.barryalan.kitchenmanager13.util.loadCircleImage
import com.barryalan.kitchenmanager13.viewmodel.BaseViewModel
import com.barryalan.kitchenmanager13.viewmodel.IngredientListViewModel
import kotlinx.android.synthetic.main.item_ingredient_with_recipes.view.*
import java.util.*
import kotlin.collections.ArrayList

class IngredientWithRecipesListAdapter(private val ingredientList: ArrayList<IngredientWithRecipes>,private var viewModel:IngredientListViewModel) :
    RecyclerView.Adapter<IngredientWithRecipesListAdapter.IngredientWithRecipesViewHolder>() {
    private var thisRecyclerView: RecyclerView? = null

    fun updateIngredientList(newIngredientList: List<IngredientWithRecipes>) {
        ingredientList.clear()
        ingredientList.addAll(newIngredientList)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IngredientWithRecipesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_ingredient_with_recipes, parent, false)
        return IngredientWithRecipesViewHolder(
            view
        )
    }

    override fun getItemCount() = ingredientList.size

    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: IngredientWithRecipesViewHolder, position: Int) {
        holder.view.tv_ingredientWRName.text =
            ingredientList[position].ingredient.name.capitalize(Locale.ROOT)
        holder.view.tv_amountOfRecipes.text = ingredientList[position].recipes.size.toString()

        if (ingredientList[position].recipes.isEmpty()) {

            holder.view.ab_deleteIngredient.apply {
                show()
                setOnClickListener {
                    //TODO move this to fragment and ask permission with dialog before deleting

                    viewModel.deleteIngredient(ingredientList[position].ingredient.ID)
                    ingredientList.remove(ingredientList[position])
                    notifyItemRemoved(position)
                }
            }
        } else {
            holder.view.ab_deleteIngredient.hide()
        }

        ingredientList[position].ingredient.image?.let {
            holder.view.img_ingredientWR.loadCircleImage(
                Uri.parse(ingredientList[position].ingredient.image),
                getProgressDrawable(holder.view.context)
            )
        }

        holder.view.setOnClickListener {

            val action =
                IngredientListFragmentDirections.actionIngredientListFragmentToIngredientDetailFragment()

            action.ingredientUID = ingredientList[holder.adapterPosition].ingredient.ID
            Navigation.findNavController(holder.view).navigate(action)

        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        thisRecyclerView = recyclerView
    }

    fun setViewModel(viewModel: IngredientListViewModel) {
        this.viewModel = viewModel
    }

    class IngredientWithRecipesViewHolder(var view: View) : RecyclerView.ViewHolder(view)
}