package com.barryalan.kitchenmanager13.model

import android.util.Log
import androidx.room.*
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

    @Query("DELETE FROM Recipe")
    suspend fun nukeRecipeTable()

    @Update
    suspend fun updateRecipe(recipe: Recipe)


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

    @Query("DELETE FROM Ingredient")
    suspend fun nukeIngredientTable()

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)


    //ReferenceDao==================================================================================
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeIngredientRef(recipeIngredientRef: RecipeIngredientRef): Long

    @Query("DELETE FROM RecipeIngredientRef WHERE recipeID = :recipeID")
    suspend fun deleteAllRecipeIngredientRef(recipeID: Long)

    @Query("DELETE FROM RecipeIngredientRef WHERE ingredientID = :ingredientID")
    suspend fun deleteRecipeIngredientRef(ingredientID: Long)

    @Query("DELETE FROM RecipeIngredientRef")
    suspend fun nukeRecipeIngredientRefTable()


    //RecipeWithIngredients CRUD====================================================================
    @Transaction
    @Query("SELECT * FROM Recipe WHERE recipeID = :ID")
    suspend fun getRecipeWithIngredients(ID: Long): RecipeWithIngredients

    @ExperimentalStdlibApi
    @Transaction
    suspend fun insertRecipeWithIngredients(recipeWithIngredients: RecipeWithIngredients) {

        //decapitalize recipe name to avoid duplication
        recipeWithIngredients.recipe.name = recipeWithIngredients.recipe.name.decapitalize(Locale.ROOT)


        //insert recipe
        insertRecipe(recipeWithIngredients.recipe)
        Log.d(
            "insertTransaction:",
            "inserted new Recipe item: ${recipeWithIngredients.recipe.name}"
        )

        //get recipeID after it is generated at save time
        val recipeID = getRecipeID(recipeWithIngredients.recipe.name)
        Log.d("insertTransaction:", "retrieved new recipe item ID: $recipeID")

        //insert all ingredients
        val ingredientsIDs = insertAllIngredients(recipeWithIngredients.ingredients)
        Log.d("insertTransaction:", "retrieved new ingredient item ID: $ingredientsIDs")

        for (ingredient in recipeWithIngredients.ingredients) {

            //get all ingredient ids
            val ingredientID = getIngredientID(ingredient.name)
            Log.d(
                "insertTransaction:",
                "retrieved new ingredient item Name: ${ingredient.name}, ID: $ingredientID"
            )

            //insert RecipeIngredientRef for all ingredients
            insertRecipeIngredientRef(RecipeIngredientRef(recipeID, ingredientID))
            Log.d("insertTransaction:", "inserted new recipeIngredientRef item")
        }
    }

    @ExperimentalStdlibApi
    @Transaction
    suspend fun updateRecipeWithIngredients(
        updatedRecipeWithIngredients: RecipeWithIngredients,
        recipeWithIngredientsToUpdate: RecipeWithIngredients
    ) {

        //give the updated recipe the ID of the old one thus allowing it to be updated
        updatedRecipeWithIngredients.recipe.ID = recipeWithIngredientsToUpdate.recipe.ID

        //decapitalize recipe name to avoid duplication
        recipeWithIngredientsToUpdate.recipe.name =
            recipeWithIngredientsToUpdate.recipe.name.decapitalize(Locale.ROOT)

        //update recipe
        updateRecipe(updatedRecipeWithIngredients.recipe)

        //TODO can probably make this perform better if I make a db request to only update the field that changed instead of the whole object
        //TODO can probably write the updating of notes better as well
//        //update the recipe
//        if(updatedRecipeWithIngredients.recipe.name != recipeWithIngredientsToUpdate.recipe.name &&
//            updatedRecipeWithIngredients.recipe.image != recipeWithIngredientsToUpdate.recipe.image){
//            //update whole recipe
//        }else if(updatedRecipeWithIngredients.recipe.name != recipeWithIngredientsToUpdate.recipe.name){
//            //update the recipe name
//
//        }else{
//            //update the recipe image
//        }

        //get a list of all in the old list that was deleted for the new list,
        //these have the ids set because we got them from a db call
        val deletedIngredients = recipeWithIngredientsToUpdate.ingredients
            .filter { !updatedRecipeWithIngredients.ingredients.contains(it) }

        //delete the reference between this recipe and the deleted ingredients
        for (ingredient in deletedIngredients) {
            deleteRecipeIngredientRef(ingredient.ID)
        }

        //insert all ingredients, onConflict = ignore
        val ingredientsIDs = insertAllIngredients(updatedRecipeWithIngredients.ingredients)
        Log.d("insertTransaction:", "retrieved new ingredient item ID: $ingredientsIDs")

        for (ingredient in updatedRecipeWithIngredients.ingredients) {

            //get all ingredient ids
            val ingredientID = getIngredientID(ingredient.name)
            Log.d(
                "insertTransaction:",
                "retrieved new ingredient item Name: ${ingredient.name}, ID: $ingredientID"
            )

            //insert RecipeIngredientRef for all ingredients
            insertRecipeIngredientRef(
                RecipeIngredientRef(
                    recipeWithIngredientsToUpdate.recipe.ID,
                    ingredientID
                )
            )
            Log.d("insertTransaction:", "inserted new recipeIngredientRef item")
        }
    }
}

