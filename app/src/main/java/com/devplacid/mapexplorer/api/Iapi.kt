package com.devplacid.mapexplorer.api

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface Iapi {

    @Headers(
        "x-rapidapi-key: 1d11dbeb3fmshca0d29673e3562bp114832jsn19792fbcf633",
        "x-rapidapi-host: places-by-category.p.rapidapi.com"
    )
    @GET("places")
    fun getPlaces(
        @Query("type") type: String,
        @Query("limit") limit: String,
        @Query("lat1") lat1: Double,
        @Query("lon1") lon1: Double,
        @Query("lat2") lat2: Double,
        @Query("lon2") lon2: Double
    ): Observable<ApiResponse>
}