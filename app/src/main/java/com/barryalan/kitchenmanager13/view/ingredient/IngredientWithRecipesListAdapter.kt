package com.barryalan.kitchenmanager13.view.ingredient

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.IngredientWithRecipes
import com.barryalan.kitchenmanager13.util.communication.OnClickListener
import com.barryalan.kitchenmanager13.util.getProgressDrawable
import com.barryalan.kitchenmanager13.util.loadCircleImage
import com.barryalan.kitchenmanager13.viewmodel.BaseViewModel
import com.barryalan.kitchenmanager13.viewmodel.IngredientListViewModel
import kotlinx.android.synthetic.main.item_ingredient_with_recipes.view.*
import java.util.*
import kotlin.collections.ArrayList

class IngredientWithRecipesListAdapter(private val ingredientList: ArrayList<IngredientWithRecipes>,private val clickListener:OnClickListener) :
    RecyclerView.Adapter<IngredientWithRecipesListAdapter.IngredientWithRecipesViewHolder>() ,
    Filterable {

    private var filteredIngredientList = ingredientList
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

    override fun getItemCount() = filteredIngredientList.size

    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: IngredientWithRecipesViewHolder, position: Int) {
        holder.view.tv_ingredientWRName.text =
            filteredIngredientList[position].ingredient.name.capitalize(Locale.ROOT)
        holder.view.tv_amountOfRecipes.text = filteredIngredientList[position].recipes.size.toString()

        if (filteredIngredientList[position].recipes.isEmpty()) {

            holder.view.ab_deleteIngredient.apply {
                show()
                setOnClickListener {
                    clickListener.onClick(filteredIngredientList[position].ingredient.ID)
                }
            }
        } else {
            holder.view.ab_deleteIngredient.hide()
        }

        filteredIngredientList[position].ingredient.image?.let {
            holder.view.img_ingredientWR.loadCircleImage(
                Uri.parse(filteredIngredientList[position].ingredient.image),
                getProgressDrawable(holder.view.context)
            )
        }

        holder.view.setOnClickListener {

            val action =
                IngredientListFragmentDirections.actionIngredientListFragmentToIngredientDetailFragment()

            action.ingredientUID = filteredIngredientList[holder.adapterPosition].ingredient.ID
            Navigation.findNavController(holder.view).navigate(action)

        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        thisRecyclerView = recyclerView
    }

    class IngredientWithRecipesViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    filteredIngredientList = ingredientList
                } else {
                    val resultList = ArrayList<IngredientWithRecipes>()
                    for (ingredient in ingredientList) {
                        if (ingredient.ingredient.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(ingredient)
                        }
                    }
                    filteredIngredientList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredIngredientList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredIngredientList = results?.values as ArrayList<IngredientWithRecipes>
                notifyDataSetChanged()
            }

        }
    }
}