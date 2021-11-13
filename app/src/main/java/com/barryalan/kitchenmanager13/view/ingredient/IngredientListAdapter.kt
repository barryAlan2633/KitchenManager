package com.barryalan.kitchenmanager13.view.ingredient

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.Amount
import com.barryalan.kitchenmanager13.model.Ingredient
import com.barryalan.kitchenmanager13.util.getProgressDrawable
import com.barryalan.kitchenmanager13.util.loadCircleImage
import com.barryalan.kitchenmanager13.util.loadImage
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_ingredient.view.*
import java.util.*
import kotlin.collections.ArrayList

class IngredientListAdapter(private val ingredientList: ArrayList<Ingredient>) :
    RecyclerView.Adapter<IngredientListAdapter.IngredientViewHolder>(), Filterable {

    private var filteredIngredientList = ingredientList
    private var mRemovedPosition: Int = 0
    private var mRemovedIngredient: Ingredient? = null
    private var mRemovedIngredientAmount: Amount? = null
    private var thisRecyclerView: RecyclerView? = null
    private var amounts: MutableList<Amount> = mutableListOf()

    fun getIngredientList(): ArrayList<Ingredient> {
        return ingredientList
    }

    fun updateIngredientList(newIngredientList: List<Ingredient>, newAmounts: List<Amount>) {
        ingredientList.clear()
        ingredientList.addAll(newIngredientList)

        amounts.clear()
        amounts.addAll(newAmounts)

        notifyDataSetChanged()
    }

    fun addIngredientItem(newIngredient: Ingredient, newAmount: Amount) {
        ingredientList.add(newIngredient)
        amounts.add(newAmount)
        notifyItemInserted(itemCount)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(
            view
        )
    }

    override fun getItemCount() = filteredIngredientList.size

    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.view.et_ingredientName.text =
            filteredIngredientList[position].name.capitalize(Locale.ROOT)

        if (amounts.isNotEmpty()) {
            //todo these amounts are wrong they should correspond to the filtered list not the regular list
            holder.view.tv_ingredientAmount.text = amounts[position].amount.toString()
            holder.view.tv_ingredientAmountUnit.text = amounts[position].unit
        }


        filteredIngredientList[position].image?.let {
            holder.view.img_ingredient.loadImage(
                Uri.parse(it),
                getProgressDrawable(holder.view.context)
            )
        } ?: run{
            holder.view.img_ingredient.setImageResource(R.drawable.ic_error_outline_white_24dp)
        }

        holder.view.setOnClickListener {

            when (Navigation.findNavController(it).currentDestination?.id) {
                R.id.ingredientListFragment -> {
                    val action =
                        IngredientListFragmentDirections.actionIngredientListFragmentToIngredientDetailFragment()

                    action.ingredientUID = ingredientList[holder.adapterPosition].ID
                    Navigation.findNavController(holder.view).navigate(action)
                }
            }


        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        thisRecyclerView = recyclerView
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        mRemovedPosition = viewHolder.adapterPosition
        mRemovedIngredient = ingredientList[viewHolder.adapterPosition]
        mRemovedIngredientAmount = amounts[viewHolder.adapterPosition]
        ingredientList.removeAt(viewHolder.adapterPosition)
        amounts.removeAt(viewHolder.adapterPosition)

        notifyItemRemoved(viewHolder.adapterPosition)

        mRemovedIngredient?.let { deletedIngredient ->
            Snackbar.make(
                viewHolder.itemView,
                "${deletedIngredient.name} deleted.",
                Snackbar.LENGTH_LONG
            ).apply {

                setAction("UNDO") {
                    ingredientList.add(mRemovedPosition, deletedIngredient)
                    amounts.add(mRemovedPosition, mRemovedIngredientAmount!!)
                    notifyItemInserted(mRemovedPosition)
                    thisRecyclerView?.smoothScrollToPosition(mRemovedPosition)
                }.show()
            }
        }


    }

    fun getAmountsList(): List<Amount> {
        return amounts
    }


    class IngredientViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredIngredientList = if (charSearch.isEmpty()) {
                    ingredientList
                } else {
                    val resultList = ArrayList<Ingredient>()
                    for (ingredient in ingredientList) {
                        if (ingredient.name.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(ingredient)
                        }
                    }

                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredIngredientList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredIngredientList = results?.values as ArrayList<Ingredient>
                notifyDataSetChanged()
            }
        }
    }
}