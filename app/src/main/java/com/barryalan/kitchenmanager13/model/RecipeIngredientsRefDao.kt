package com.barryalan.kitchenmanager13.model

import android.util.Log
import androidx.room.*
import retrofit2.http.GET
import java.util.*

@Dao
interface RecipeIngredientsRefDao {
    //RecipeDao=====================================================================================
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert
    suspend fun insertAllRecipes(recipes: List<Recipe>): List<Long>

    @Query("SELECT * FROM Recipe WHERE recipeID = :ID")
    suspend fun getRecipe(ID: Long): Recipe

    @Query("SELECT recipeID FROM Recipe WHERE name = :name")
    suspend fun getRecipeID(name: String): Long

    @Query("SELECT * FROM Recipe")
    suspend fun getAllRecipes(): List<Recipe>

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM Recipe Where recipeID = :recipeID")
    suspend fun deleteRecipe(recipeID: Long)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("DELETE FROM Recipe")
    suspend fun nukeRecipeTable()


    //IngredientDao=================================================================================
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredient(ingredient: Ingredient): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIngredients(ingredients: List<Ingredient>): List<Long>

    @Query("SELECT * FROM Ingredient")
    suspend fun getAllIngredients(): List<Ingredient>

    @Query("SELECT * FROM Ingredient WHERE ingredientID = :ID")
    suspend fun getIngredient(ID: Long): Ingredient

    @Query("SELECT ingredientID FROM Ingredient WHERE name = :name")
    suspend fun getIngredientID(name: String): Long

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Query("DELETE FROM Ingredient")
    suspend fun nukeIngredientTable()



    //AmountDao==================================================================================
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAmount(amount: Amount): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAmounts(amounts: List<Amount>): List<Long>

    @Query("SELECT * FROM Amount")
    suspend fun getAllAmounts(): List<Amount>

    @Query("SELECT * FROM Amount WHERE amountID = :ID")
    suspend fun getAmount(ID: Long): Amount

    @Delete
    suspend fun deleteAmount(amount: Amount)

    @Delete
    suspend fun deleteAllAmounts(amount: List<Amount>)

    @Update
    suspend fun updateAmount(amount: Amount)

    @Query("DELETE FROM Amount")
    suspend fun nukeAmountTable()

    //ReferenceDao==================================================================================
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeIngredientRef(recipeIngredientRef: RecipeIngredientRef): Long

    @Query("DELETE FROM RecipeIngredientRef WHERE recipeID = :recipeID")
    suspend fun deleteAllRecipeIngredientRef(recipeID: Long)

    @Query("DELETE FROM RecipeIngredientRef WHERE amountID = :amountID")
    suspend fun deleteRecipeIngredientRef(amountID: Long)

    @Query("DELETE FROM RecipeIngredientRef")
    suspend fun nukeRecipeIngredientRefTable()


    //RecipeWithIngredients CRUD====================================================================
    @Transaction
    @Query("SELECT * FROM Recipe WHERE recipeID = :ID")
    suspend fun getRecipeWithIngredients(ID: Long): RecipeWithIngredients

    @ExperimentalStdlibApi
    @Transaction
    suspend fun insertRecipeWithIngredients(recipeWithIngredients: RecipeWithIngredients) {
//        1)Normalize names(lowercase)
//        2)Insert recipe
//        3)Insert ingredients
//        4)Retrieve IDs of items that were not inserted, using name(ID = -1)
//        5)Insert amounts
//        6)Insert ref

        //1)
        //decapitalize recipe name to avoid duplication
        recipeWithIngredients.recipe.name =
            recipeWithIngredients.recipe.name.decapitalize(Locale.ROOT)

        //decapitalize all ingredient names to avoid duplication
        for (ingredient in recipeWithIngredients.ingredients) {
            ingredient.name = ingredient.name.decapitalize(Locale.ROOT)
        }

        //2)
        // insert recipe
        val recipeID = insertRecipe(recipeWithIngredients.recipe)
        Log.d(
            "insertTransaction:",
            "inserted new Recipe item: ${recipeWithIngredients.recipe.name}, ID= $recipeID"
        )

        //3)
        //insert all ingredients
        val retrievedIngredientsIDs = insertAllIngredients(recipeWithIngredients.ingredients)
        Log.d("insertTransaction:", "inserted new ingredient items, IDs= $retrievedIngredientsIDs")

        //4)
        val finalIngredientsIDs: MutableList<Long> = mutableListOf()
        for (ingredientID in retrievedIngredientsIDs.withIndex()) {
            if (ingredientID.value == -1L) {
                val actualID =
                    getIngredientID(recipeWithIngredients.ingredients[ingredientID.index].name)
                finalIngredientsIDs.add(actualID)
            } else {
                finalIngredientsIDs.add(ingredientID.value)
            }
        }

        //5)
        //insert all amounts
        val amountsIDs = insertAllAmounts(recipeWithIngredients.amounts)
        Log.d("insertTransaction:", "inserted new amount items IDs= $amountsIDs")

        //6)
        //insert all references
        for (count in recipeWithIngredients.ingredients.indices) {
            val reference =
                RecipeIngredientRef(recipeID, finalIngredientsIDs[count], amountsIDs[count])

            insertRecipeIngredientRef(reference)
            Log.d(
                "insertTransaction:",
                "inserted new recipeIngredientRef item IDs= $recipeID, ${finalIngredientsIDs[count]}, ${amountsIDs[count]}"
            )
        }

    }

    @ExperimentalStdlibApi
    @Transaction
    suspend fun updateRecipeWithIngredients(
        updated: RecipeWithIngredients,
        toUpdate: RecipeWithIngredients
    ) {
        //1)Give new recipe Item the correct id
        //2)Normalize the recipe name
        //3)Update recipe item

        //4)Get lists of items to be inserted and deleted
        //5)Normalize ingredient names
        //6)Insert new ingredients
        //7)Retrieve IDs of items that were not inserted properly(ID = -1), using name
        //8)insert new amounts
        //9)insert new references

        //10)delete unwanted amounts
        //11)delete unwanted references

        //1)
        //give the updated recipe the ID of the old one thus allowing it to be updated
        updated.recipe.ID = toUpdate.recipe.ID

        //2)
        // decapitalize recipe name to avoid duplication
        updated.recipe.name = updated.recipe.name.decapitalize(Locale.ROOT)

        //3)
        // update recipe
        updateRecipe(updated.recipe)

        //4)
        //items to be insert these don't have IDs set
        val ingredientsToInsert = updated.ingredients.filter { !toUpdate.ingredients.contains(it) }
        val amountsToInsert = updated.amounts.filter { !toUpdate.amounts.contains(it) }

        //items to be deleted these have IDs set already
        val ingredientsToDelete = toUpdate.ingredients.filter { !updated.ingredients.contains(it) }
        val amountsToDelete = toUpdate.amounts.filter { !updated.amounts.contains(it) }

        //5)
        //decapitalize all ingredient names to avoid duplication
        for (ingredient in ingredientsToInsert) {
            ingredient.name = ingredient.name.decapitalize(Locale.ROOT)
        }

        //6)
        // insert all ingredients
        val retrievedIngredientsIDs = insertAllIngredients(ingredientsToInsert)
        Log.d("updateTransaction:", "inserted new ingredient items, IDs= $retrievedIngredientsIDs")

        //7)
        //retrieve IDs of items that were not inserted properly(ID=-1)
        val finalIngredientsIDs: MutableList<Long> = mutableListOf()
        for (ingredientID in retrievedIngredientsIDs.withIndex()) {
            if (ingredientID.value == -1L) {
                val actualID = getIngredientID(ingredientsToInsert[ingredientID.index].name)
                finalIngredientsIDs.add(actualID)
            } else {
                finalIngredientsIDs.add(ingredientID.value)
            }
        }

        //8)
        //insert all new amounts
        val amountsIDs = insertAllAmounts(amountsToInsert)
        Log.d("updateTransaction:", "inserted new amount items IDs= $amountsIDs")

        //9)
        //insert all new references
        for (count in finalIngredientsIDs.indices) {
            val reference =
                RecipeIngredientRef(
                    toUpdate.recipe.ID,
                    finalIngredientsIDs[count],
                    amountsIDs[count]
                )

            insertRecipeIngredientRef(reference)
            Log.d(
                "updateTransaction:",
                "inserted new recipeIngredientRef item IDs= ${toUpdate.recipe.ID}, ${finalIngredientsIDs[count]}, ${amountsIDs[count]}"
            )
        }

        //10)
        // delete unwanted amounts
        deleteAllAmounts(amountsToDelete)

        //11)
        // delete unwanted references
        for (amount in amountsToDelete) {
            deleteRecipeIngredientRef(amount.ID)
        }



//        //TODO can probably make this perform better if I make a db request to only update the field that changed instead of the whole object
//        //TODO can probably write the updating of notes better as well
////        //update the recipe
////        if(updatedRecipeWithIngredients.recipe.name != recipeWithIngredientsToUpdate.recipe.name &&
////            updatedRecipeWithIngredients.recipe.image != recipeWithIngredientsToUpdate.recipe.image){
////            //update whole recipe
////        }else if(updatedRecipeWithIngredients.recipe.name != recipeWithIngredientsToUpdate.recipe.name){
////            //update the recipe name
////
////        }else{
////            //update the recipe image
////        }
//

    }

    @Transaction
    suspend fun deleteRecipeAndAssociations(recipeID: Long) {
        //TODO fix this mess
        //delete this recipe
        deleteRecipe(recipeID)

        //delete all recipeIngredientsRef that belong to this recipe
        deleteAllRecipeIngredientRef(recipeID)
    }


    //IngredientWithRecipes CRUD====================================================================
    @Transaction
    @Query("SELECT * FROM Recipe")
    suspend fun getAllIngredientWithRecipes(): IngredientWithRecipes


}

