package com.barryalan.kitchenmanager13.view.recipe

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.Recipe
import com.barryalan.kitchenmanager13.util.getProgressDrawable
import com.barryalan.kitchenmanager13.util.loadImage
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_recipe.view.*

class RecipeListAdapter(private val recipeList: ArrayList<Recipe>) :
    RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    private var removedPosition: Int = 0
    private var removedRecipe: Recipe? = null
    private var thisRecyclerView: RecyclerView? = null

    fun getRecipeList(): ArrayList<Recipe> {
        return recipeList
    }

    fun updateRecipeList(newRecipeList: List<Recipe>) {
        recipeList.clear()
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

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.view.tv_recipeName.text = recipeList[position].name

        recipeList[position].image?.let {
            holder.view.img_recipe.loadImage(
                Uri.parse(it),
                getProgressDrawable(holder.view.context)
            )
        }
        holder.view.setOnClickListener {

            val action = RecipeListFragmentDirections.actionRecipeListFragmentToDetailFragment()
            action.recipeUID = recipeList[holder.adapterPosition].ID
            Navigation.findNavController(holder.view).navigate(action)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        thisRecyclerView = recyclerView
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        removedPosition = viewHolder.adapterPosition
        removedRecipe = recipeList[viewHolder.adapterPosition]

        recipeList.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)

        removedRecipe?.let { deletedRecipe ->
            Snackbar.make(
                viewHolder.itemView,
                "${deletedRecipe.name} deleted.",
                Snackbar.LENGTH_LONG
            ).apply {
                setAction("UNDO") {
                    recipeList.add(removedPosition, deletedRecipe)
                    notifyItemInserted(removedPosition)
                    thisRecyclerView?.smoothScrollToPosition(removedPosition)
                }
            }.show()
        }
    }

    class RecipeViewHolder(var view: View) : RecyclerView.ViewHolder(view)

}
