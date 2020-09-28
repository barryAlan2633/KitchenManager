package com.barryalan.kitchenmanager13.model

import androidx.room.*

@Entity(indices = [Index(value = ["name"],unique = true)])
data class Recipe(
    var name: String,
    val image: String?
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "recipeID")
    var ID: Long = 0
}

@Entity(indices = [Index(value = ["name"],unique = true)])
data class Ingredient(
    val name: String,
    val image: String?,
    val amount: Int
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ingredientID")
    var ID: Long = 0
}

@Entity(primaryKeys = ["recipeID", "ingredientID"])
data class RecipeIngredientRef(
    val recipeID: Long,
    val ingredientID: Long
)

data class RecipeWithIngredients(
    @Embedded
    val recipe: Recipe,
    @Relation(
        parentColumn = "recipeID",
        entity = Ingredient::class,
        entityColumn = "ingredientID",
        associateBy = Junction(
            value = RecipeIngredientRef::class,
            parentColumn = "recipeID",
            entityColumn = "ingredientID"
        )
    )
    val ingredients: List<Ingredient>
)