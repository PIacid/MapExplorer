package com.devplacid.mapexplorer.datamodel

import android.annotation.SuppressLint
import com.devplacid.mapexplorer.datamodel.api.RetrofitClient
import com.devplacid.mapexplorer.datamodel.database.Idao
import com.devplacid.mapexplorer.datamodel.database.PlaceEntity
import com.devplacid.mapexplorer.geo.BoundingBox
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class Repository(val retrofitClient: RetrofitClient, val placesDao: Idao) {

    val listOfBookMarks = mutableListOf<Place>()

    fun requestPlaces(category: String, box: BoundingBox) =
        retrofitClient.getApiClient().getPlaces(
            category,
            "200",
            box.lat1,
            box.lon1,
            box.lat2,
            box.lon2
        )
            .subscribeOn(Schedulers.io())
            .flatMap {
                Observable.fromIterable(it.features)
                    .map { Place(it, false) }
                    .map {
                        if (listOfBookMarks.contains(it)) {
                            it.isBookmark = true
                        }
                        return@map it
                    }
                    .toList()
                    .toObservable()
            }

    fun deleteFromDb(placeEntity: PlaceEntity) {
        Completable.fromRunnable {
            placesDao.remove(placeEntity)
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun saveBookmark(placeEntity: PlaceEntity) {
        Completable.fromRunnable {
            placesDao.insertOne(placeEntity)
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun readBookmarksFromDB() = placesDao.getAll()
        .subscribeOn(Schedulers.io())
        .flatMap {
            Observable.fromIterable(it)
                .map { Place(it) }
                .toList()
                .toObservable()
        }
        .doOnNext {
            listOfBookMarks.clear()
            listOfBookMarks.addAll(it)
        }

}