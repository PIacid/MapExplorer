package com.devplacid.mapexplorer.datamodel.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlaceEntity::class], version = 1)
abstract class PlacesDatabase : RoomDatabase() {

    abstract fun getDao(): Idao

    companion object {
        private var instance: PlacesDatabase? = null

        fun getDB(context: Context): PlacesDatabase {
            return instance ?: Room.databaseBuilder(
                context.applicationContext,
                PlacesDatabase::class.java,
                "places_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}