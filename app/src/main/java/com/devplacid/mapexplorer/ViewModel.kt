package com.devplacid.mapexplorer

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.devplacid.mapexplorer.api.Category
import com.devplacid.mapexplorer.api.Place
import com.devplacid.mapexplorer.api.RetrofitClient
import com.devplacid.mapexplorer.geo.BoundingBox
import com.devplacid.mapexplorer.geo.LocationSource
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class ViewModel(val app: Application) : AndroidViewModel(app) {

    private val loadedPlaces = mutableListOf<Place>()

    var currentLatLng: LatLng? = null
    var areaRadiusM: Int = 1000
    var currentCategory: String? = null

    private val locationSource = LocationSource(app.baseContext)
    private val retrofitClient = RetrofitClient()

    private val locationData = MutableLiveData<LatLng?>()
    val outDataLocation: LiveData<LatLng?> = locationData

    private val markersData = MutableLiveData<List<Place>?>()
    val outDataMarkers: LiveData<List<Place>?> = markersData

    private fun checkInRange(place: Place, rad: Int) = place.properties.distance < rad

    fun remove(place: Place?) = place?.let {
        loadedPlaces.remove(it)
        onSearchRangeChanged()
    }


    @SuppressLint("CheckResult")
    fun onSearchRangeChanged(rad: Int = areaRadiusM) {
        areaRadiusM = rad

        Observable.fromIterable(loadedPlaces)
            .subscribeOn(Schedulers.io())
            .filter { checkInRange(it, areaRadiusM) }
            .toList()
            .toObservable()
            .subscribe { markersData.postValue(it) }
    }

    @SuppressLint("CheckResult")
    fun requestPlaces(category: String? = currentCategory) {
        if (currentLatLng == null || category == null) return

        val boundingBox = BoundingBox(currentLatLng!!)
        currentCategory = category

        retrofitClient.getApiClient().getPlaces(
            category,
            "100",
            boundingBox.lat1,
            boundingBox.lon1,
            boundingBox.lat2,
            boundingBox.lon2
        )
            .subscribeOn(Schedulers.io())
            .subscribe({
                loadedPlaces.clear()
                loadedPlaces.addAll(it.features)
                onSearchRangeChanged()
            }, {
                markersData.postValue(null)
            })
    }


    @SuppressLint("CheckResult")
    fun getLocation() {
        locationSource.getLocation()
            .subscribeOn(Schedulers.io())
            .map { LatLng(it.latitude, it.longitude) }
            .filter {
                currentLatLng == null || SphericalUtil.computeDistanceBetween(
                    it,
                    currentLatLng
                ) > 50
            }
            .subscribe {
                currentLatLng = it
                locationData.postValue(currentLatLng)
            }
    }


}