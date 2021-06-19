package com.devplacid.mapexplorer.datamodel

import com.devplacid.mapexplorer.datamodel.api.PlaceDTO
import com.devplacid.mapexplorer.datamodel.database.PlaceEntity

data class Place(
    val lat: Double,
    val lon: Double,
    val name: String,
    val distance: Double,
    var isBookmark: Boolean
) {
    constructor(placeDTO: PlaceDTO, isBookmark: Boolean) : this(
        placeDTO.properties.lat,
        placeDTO.properties.lon,
        placeDTO.properties.name,
        placeDTO.properties.distance,
        isBookmark
    )

    constructor(placeEntity: PlaceEntity) : this(
        placeEntity.lat,
        placeEntity.lon,
        placeEntity.name,
        placeEntity.distance,
        true
    )

    override fun equals(other: Any?): Boolean {
        other as Place
        return other.name == this.name && other.lon == this.lon && other.lat == this.lat
    }

}