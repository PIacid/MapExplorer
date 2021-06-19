package com.devplacid.mapexplorer

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.devplacid.mapexplorer.datamodel.Place
import com.devplacid.mapexplorer.datamodel.Repository
import com.devplacid.mapexplorer.datamodel.api.Category
import com.devplacid.mapexplorer.datamodel.api.RetrofitClient
import com.devplacid.mapexplorer.datamodel.database.PlaceEntity
import com.devplacid.mapexplorer.datamodel.database.PlacesDatabase
import com.devplacid.mapexplorer.geo.BoundingBox
import com.devplacid.mapexplorer.geo.LocationSource
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ViewModel(app: Application) : AndroidViewModel(app) {

    private val loadedPlaces = mutableListOf<Place>()

    var currentLatLng: LatLng? = null
    var areaRadiusM: Int = 1000
    var currentCategory: String = "bookmarks"

    private val locationSource by lazy { LocationSource(app.baseContext) }
    private val retrofitClient by lazy { RetrofitClient() }
    private val placesDao by lazy { PlacesDatabase.getDB(app).getDao() }
    private val repo by lazy { Repository(retrofitClient, placesDao) }

    var databaseDisposable: Disposable? = null
    var serverDisposable: Disposable? = null

    private val locationData = MutableLiveData<LatLng?>()
    val outDataLocation: LiveData<LatLng?> = locationData

    private val markersData = MutableLiveData<List<Place>?>()
    val outDataMarkers: LiveData<List<Place>?> = markersData

    private fun checkInRange(place: Place, rad: Int) = place.distance < rad

    fun remove(place: Place?) = place?.let {
        if (place.isBookmark) {
            val entity = PlaceEntity(
                it.lat,
                it.lon,
                it.name,
                it.distance
            )
            repo.deleteFromDb(entity)
        } else {
            loadedPlaces.remove(it)
            onSearchRangeChanged()
        }
    }

    fun save(place: Place?) {
        if (currentCategory == Category.BOOKMARKS.apiName) return
        place?.let {
            val entity = PlaceEntity(
                place.lat,
                place.lon,
                place.name,
                place.distance
            )
            repo.saveBookmark(entity)
            place.isBookmark = true
            onSearchRangeChanged()
        }
    }



    @SuppressLint("CheckResult")
    fun getServerCategory(category: String = currentCategory) {
        if (currentLatLng == null) return

        val boundingBox = BoundingBox(currentLatLng!!)
        currentCategory = category

        serverDisposable = repo.requestPlaces(category, boundingBox)
            .doOnSubscribe{ databaseDisposable?.dispose() }
            .subscribe(
                {
                    loadedPlaces.clear()
                    loadedPlaces.addAll(it)
                    onSearchRangeChanged()
                }, {
                    markersData.postValue(null)
                    it.printStackTrace()
                }
            )
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
    fun getDatabaseBookmarks() {
        currentCategory = "bookmarks"

        databaseDisposable = repo.readBookmarksFromDB()
            .doOnSubscribe{ serverDisposable?.dispose() }
            .subscribe(
                {
                    loadedPlaces.clear()
                    loadedPlaces.addAll(it)
                    onSearchRangeChanged()
                }, {
                    it.printStackTrace()
                }
            )
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