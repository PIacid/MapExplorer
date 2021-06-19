package com.devplacid.mapexplorer.datamodel.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places_table")
data class PlaceEntity(
    val lat: Double,
    val lon: Double,
    @PrimaryKey val name: String,
    val distance: Double
)