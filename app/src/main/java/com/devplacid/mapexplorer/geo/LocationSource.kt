package com.devplacid.mapexplorer.geo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.reactivex.subjects.PublishSubject

class LocationSource(context: Context) {

    private val locationSubject = PublishSubject.create<Location>()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.create().apply {
        interval = 200*1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            locationSubject.onNext(p0.locations[0])
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation() : PublishSubject<Location> {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        return locationSubject
    }

}