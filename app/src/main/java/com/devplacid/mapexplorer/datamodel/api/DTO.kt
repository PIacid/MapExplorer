package com.devplacid.mapexplorer.datamodel.api

data class ApiResponse(
    val features: List<PlaceDTO>
)

data class PlaceDTO(
    val properties: Properties
)

data class Properties(
    val lat: Double,
    val lon: Double,
    val name: String,
    val distance: Double
)