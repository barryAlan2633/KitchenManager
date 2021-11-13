package com.barryalan.kitchenmanager13.view.mealPlanner

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.Recipe
import com.barryalan.kitchenmanager13.util.*
import kotlinx.android.synthetic.main.item_recipe.view.*
import java.util.*
import kotlin.collections.ArrayList

class MealPickerAdapter(private val recipeList: ArrayList<Recipe>) :
    RecyclerView.Adapter<MealPickerAdapter.RecipeViewHolder>() {

    private val TAG: String = "AppDebug"

    private var thisRecyclerView: RecyclerView? = null
    private var selectedRecipesIDs: MutableList<Long> = mutableListOf()
    private var oldSelectedRecipesIDs: MutableList<Long> = mutableListOf()

    fun getRecipeList(): ArrayList<Recipe> = recipeList

    fun getSelectedRecipesIDs(): List<Long> = selectedRecipesIDs

    fun getOldSelectedRecipesIDs(): List<Long> = oldSelectedRecipesIDs

    fun updateRecipeList(newRecipeList: List<Recipe>) {
        recipeList.clear()

        //add the rest of the recipes
        recipeList.addAll(newRecipeList)
        notifyDataSetChanged()
    }

    fun updateSelectedRecipes(recipes: List<Long>) {
        selectedRecipesIDs.addAll(recipes)
        oldSelectedRecipesIDs.addAll(recipes)
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

        recipeList[position].image?.let {
            holder.view.img_meal.loadImage(
                Uri.parse(it),
                getProgressDrawable(holder.view.context)
            )
        }

        //if this recipe is already selected
        if (selectedRecipesIDs.contains(recipeList[position].ID)) {
            holder.view.background =
                ContextCompat.getDrawable(holder.view.context, R.color.darkGray)
        }

        holder.view.setOnClickListener {
            //if this recipe is already selected
            if (selectedRecipesIDs.contains(recipeList[position].ID)) {
                holder.view.background =
                    ContextCompat.getDrawable(holder.view.context, R.color.activity_background)
                selectedRecipesIDs.remove(recipeList[position].ID)
            } else {
                holder.view.background =
                    ContextCompat.getDrawable(holder.view.context, R.color.darkGray)
                selectedRecipesIDs.add(recipeList[position].ID)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        thisRecyclerView = recyclerView
    }

    class RecipeViewHolder(var view: View) : RecyclerView.ViewHolder(view)
}
