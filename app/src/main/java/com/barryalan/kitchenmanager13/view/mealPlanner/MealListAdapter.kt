package com.barryalan.kitchenmanager13.view.mealPlanner

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.Recipe
import com.barryalan.kitchenmanager13.util.*
import com.barryalan.kitchenmanager13.util.communication.RecipeOnClickListener
import kotlinx.android.synthetic.main.item_recipe.view.*
import java.util.*
import kotlin.collections.ArrayList

class MealListAdapter(
    private val recipeList: ArrayList<Recipe>,
    private val recipeClickListener: RecipeOnClickListener?
) :
    RecyclerView.Adapter<MealListAdapter.RecipeViewHolder>() {

    private val TAG: String = "AppDebug"

    private var thisRecyclerView: RecyclerView? = null

    fun getRecipeList(): ArrayList<Recipe> {
        //remove the add new recipe
        recipeList.removeAt(0)

        //return the recipe list
        return recipeList
    }

    fun updateRecipeList(newRecipeList: List<Recipe>) {
        recipeList.clear()

        //add the addNewRecipe card
        recipeList.add(Recipe("New Meal", null, "Food"))

        //add the rest of the recipes
        recipeList.addAll(newRecipeList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(
            view
        )
    }

    override fun getItemCount() = recipeList.size

    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.view.tv_recipeName.text = recipeList[position].name.capitalize(Locale.ROOT)

        //If this is the AddNewRecipe card
        if (recipeList[position].ID == 0L) {
            holder.view.img_recipe.setImageDrawable(
                holder.view.context.resources.getDrawable(
                    R.drawable.ic_add_circle_outline_white_24dp,
                    null
                )
            )
        }else{
            recipeList[position].image?.let {
                holder.view.img_recipe.loadImage(
                    Uri.parse(it),
                    getProgressDrawable(holder.view.context)
                )
            }?: run{
                holder.view.img_recipe.loadCircleImage(
                    R.drawable.ic_error_outline_white_24dp,
                    getProgressDrawable(holder.view.context)
                )
            }
        }

        holder.view.setOnClickListener {
            recipeClickListener!!.onClick(recipeList[position].ID)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        thisRecyclerView = recyclerView
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        recipeList.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)
    }


    class RecipeViewHolder(var view: View) : RecyclerView.ViewHolder(view)
}
