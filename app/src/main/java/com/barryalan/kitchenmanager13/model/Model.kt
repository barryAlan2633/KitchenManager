package com.barryalan.kitchenmanager13.model

import androidx.room.*

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Recipe(
    var name: String,
    val image: String?,
    val type: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "recipeID")
    var ID: Long = 0
}

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Ingredient(
    var name: String,
    val image: String?
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ingredientID")
    var ID: Long = 0
}

@Entity
data class Amount(
    val amount: Float,
    val unit: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "amountID")
    var ID: Long = 0
}


@Entity(primaryKeys = ["recipeID", "ingredientID", "amountID"])
data class RecipeIngredientRef(
    val recipeID: Long,
    val ingredientID: Long,
    val amountID: Long
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
    val ingredients: List<Ingredient>,

    @Relation(
        parentColumn = "recipeID",
        entity = Amount::class,
        entityColumn = "amountID",
        associateBy = Junction(
            value = RecipeIngredientRef::class,
            parentColumn = "recipeID",
            entityColumn = "amountID"
        )
    )
    val amounts: List<Amount>
)

data class IngredientWithRecipes(
    @Embedded
    val ingredient: Ingredient,
    @Relation(
        parentColumn = "ingredientID",
        entity = Recipe::class,
        entityColumn = "recipeID",
        associateBy = Junction(
            value = RecipeIngredientRef::class,
            parentColumn = "ingredientID",
            entityColumn = "recipeID"
        )
    )
    val recipes: List<Recipe>
)



