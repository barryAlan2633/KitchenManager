package com.barryalan.kitchenmanager13.view.recipe

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.Recipe
import com.barryalan.kitchenmanager13.util.*
import kotlinx.android.synthetic.main.item_recipe.view.*
import java.util.*
import kotlin.collections.ArrayList

class RecipeListAdapter(private val recipeList: ArrayList<Recipe>) :
    RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>(), Filterable {

    private val TAG: String = "AppDebug"

    private var filteredRecipeList = recipeList
    var removedPosition: Int = 0
    private var removedRecipe: Recipe? = null
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
        recipeList.add(Recipe("Add New Recipe",null))

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

    override fun getItemCount() = filteredRecipeList.size

    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.view.tv_recipeName.text = filteredRecipeList[position].name.capitalize(Locale.ROOT)

        filteredRecipeList[position].image?.let {
            holder.view.img_recipe.loadCircleImage(
                Uri.parse(it),
                getProgressDrawable(holder.view.context)
            )
        }

        //If this is the AddNewRecipe card
        if(filteredRecipeList[position].ID == 0L){
            holder.view.img_recipe.setImageDrawable(holder.view.context.resources.getDrawable(R.drawable.ic_add_black_24dp,null))

        }
        holder.view.setOnClickListener {
            //if the id has not been initialized aka you pressed the AddNewRecipe card
            if(filteredRecipeList[holder.adapterPosition].ID == 0L){
                Navigation.findNavController(holder.view).navigate(RecipeListFragmentDirections.actionRecipeListFragmentToNewEditFragment())

            }else{
                val action = RecipeListFragmentDirections.actionRecipeListFragmentToDetailFragment()
                action.recipeUID = filteredRecipeList[holder.adapterPosition].ID
                Navigation.findNavController(holder.view).navigate(action)
            }
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
    }

    fun undoRemoveItem() {
        notifyDataSetChanged()
    }

    class RecipeViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredRecipeList = if (charSearch.isEmpty()) {
                    recipeList
                } else {
                    val resultList = ArrayList<Recipe>()
                    for (recipe in recipeList) {
                        if (recipe.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(recipe)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredRecipeList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredRecipeList = results?.values as ArrayList<Recipe>
                notifyDataSetChanged()
            }

        }
    }

}
