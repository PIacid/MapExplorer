package com.devplacid.mapexplorer.geo

import com.google.android.gms.maps.model.LatLng

data class BoundingBox(val latLng: LatLng) {

    private val maxDeltaDegrees = 5000 / 111_000.00

    val lat1: Double = latLng.latitude - maxDeltaDegrees
    val lon1: Double = latLng.longitude - maxDeltaDegrees
    val lat2: Double= latLng.latitude + maxDeltaDegrees
    val lon2: Double = latLng.longitude + maxDeltaDegrees
}
