package com.devplacid.mapexplorer.api

import com.google.android.gms.maps.model.Marker

data class ApiResponse(
    val features: List<Place>
)

data class Place(
    val properties: Properties
)

data class Properties(
    val lat: Double,
    val lon: Double,
    val name: String,
    val distance: Double,
    val formatted: String
)