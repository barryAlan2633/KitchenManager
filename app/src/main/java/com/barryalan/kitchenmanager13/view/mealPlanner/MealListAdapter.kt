package com.barryalan.kitchenmanager13.view.mealPlanner

import android.net.Uri
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.MealPlanWithRecipes
import com.barryalan.kitchenmanager13.model.Recipe
import com.barryalan.kitchenmanager13.util.*
import com.barryalan.kitchenmanager13.util.communication.MealOnClickListener
import com.barryalan.kitchenmanager13.util.communication.RecipeOnClickListener
import kotlinx.android.synthetic.main.item_meal.view.*
import kotlinx.android.synthetic.main.item_recipe.view.img_meal
import kotlinx.android.synthetic.main.item_recipe.view.tv_recipeName
import java.util.*
import kotlin.collections.ArrayList

class MealListAdapter(
    private val recipeList: ArrayList<Recipe>,
    private val recipeClickListener: RecipeOnClickListener?,
    private val mealClickListener: MealOnClickListener?
) :
    RecyclerView.Adapter<MealListAdapter.RecipeViewHolder>() {

    private val TAG: String = "AppDebug"

    private var mealPlan: MealPlanWithRecipes? = null
    private var thisRecyclerView: RecyclerView? = null

    fun getRecipeList(): ArrayList<Recipe> {
        //remove the add new recipe
        recipeList.removeAt(0)

        //return the recipe list
        return recipeList
    }

    fun updateMealPlan(newMealPlanWithRecipes: MealPlanWithRecipes?) {
        mealPlan = newMealPlanWithRecipes

        recipeList.clear()

        //add the addNewRecipe card
        recipeList.add(Recipe("New Meal", null, "Food"))
        Log.d("debug", "adding new meal card")

        //add the rest of the recipes
        newMealPlanWithRecipes?.let {
            recipeList.addAll(it.recipes)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_meal, parent, false)
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
            holder.view.img_meal.setImageDrawable(
                holder.view.context.resources.getDrawable(
                    R.drawable.ic_add_circle_outline_white_24dp,
                    null
                )
            )

            holder.view.img_decrease.visibility = View.GONE
            holder.view.img_increase.visibility = View.GONE
            holder.view.tv_numberOfMeals.visibility = View.GONE
        } else {
            recipeList[position].image?.let {
                holder.view.img_meal.loadImage(
                    Uri.parse(it),
                    getProgressDrawable(holder.view.context)
                )
            } ?: run {
                holder.view.img_meal.setImageResource(R.drawable.ic_error_outline_white_24dp)
            }
        }

        holder.view.img_meal.setOnClickListener {
            recipeClickListener!!.onClick(recipeList[position].ID)
        }

        holder.view.img_decrease.setOnClickListener {
            val currentValue = Integer.parseInt(holder.view.tv_numberOfMeals.text.toString())

            if (currentValue != 1) {
                //update ui
                holder.view.tv_numberOfMeals.text = (currentValue - 1).toString()

                //update db, position-1 accounts for the newAmount card
                mealClickListener!!.onClick(mealPlan!!.amounts[position - 1].ID, currentValue - 1)
            }
        }

        holder.view.img_increase.setOnClickListener {

            //update ui
            val currentValue = Integer.parseInt(holder.view.tv_numberOfMeals.text.toString())
            holder.view.tv_numberOfMeals.text = (currentValue + 1).toString()

            //update db, position-1 accounts for the newAmount card
            mealClickListener!!.onClick(mealPlan!!.amounts[position - 1].ID, currentValue + 1)

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
