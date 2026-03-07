package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val price: Long,
    val recipeJson: String?, // receta / parámetros (JSON)
    val updatedAt: Long
)
