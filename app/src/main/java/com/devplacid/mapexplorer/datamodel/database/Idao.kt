package com.devplacid.mapexplorer.datamodel.database

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface Idao {

    @Query("SELECT * FROM places_table")
    fun getAll(): Observable<List<PlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOne(placeEntity: PlaceEntity)

    @Delete
    fun remove(placeEntity: PlaceEntity)

    @Query("DELETE FROM places_table")
    fun dropTable()

}