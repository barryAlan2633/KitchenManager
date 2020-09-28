package com.barryalan.kitchenmanager13.view.ingredient

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.barryalan.kitchenmanager13.R
import com.barryalan.kitchenmanager13.model.Ingredient
import com.barryalan.kitchenmanager13.util.getProgressDrawable
import com.barryalan.kitchenmanager13.util.loadImage
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_ingredient.view.*
import java.util.*
import kotlin.collections.ArrayList

class IngredientListAdapter(private val ingredientList: ArrayList<Ingredient>) :
    RecyclerView.Adapter<IngredientListAdapter.IngredientViewHolder>() {

    private var mRemovedPosition: Int = 0
    private var mRemovedIngredient: Ingredient? = null
    private var thisRecyclerView: RecyclerView? = null

    fun getIngredientList(): ArrayList<Ingredient> {
        return ingredientList
    }

    fun updateIngredientList(newIngredientList: List<Ingredient>) {
        ingredientList.clear()
        ingredientList.addAll(newIngredientList)
        notifyDataSetChanged()
    }

    fun addIngredientItem(newIngredient: Ingredient) {
        ingredientList.add(newIngredient)
        notifyItemInserted(itemCount)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(
            view
        )
    }

    override fun getItemCount() = ingredientList.size

    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.view.et_ingredientName.text = ingredientList[position].name.capitalize(Locale.ROOT)
        holder.view.et_ingredientAmount.text = ingredientList[position].amount.toString()

        ingredientList[position].image?.let {
            holder.view.img_ingredient.loadImage(
                Uri.parse(ingredientList[position].image),
                getProgressDrawable(holder.view.context)
            )
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

        ingredientList.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)

        mRemovedIngredient?.let { deletedIngredient ->
            Snackbar.make(
                viewHolder.itemView,
                "${deletedIngredient.name} deleted.",
                Snackbar.LENGTH_LONG
            ).apply {

                setAction("UNDO") {
                    ingredientList.add(mRemovedPosition, deletedIngredient)
                    notifyItemInserted(mRemovedPosition)
                    thisRecyclerView?.smoothScrollToPosition(mRemovedPosition)
                }.show()
            }
        }


    }

    class IngredientViewHolder(var view: View) : RecyclerView.ViewHolder(view)
}