package com.example.spatiaroomtest

import android.content.Context
import androidx.room.*
import co.anbora.labs.spatia.builder.SpatiaRoom

@Database(entities = [Poi::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun poiDao(): PoiDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            val db = SpatiaRoom.databaseBuilder(context, AppDatabase::class.java, "db").build()
            instance = db
            return db
        }
    }

}

@Entity
data class Poi(
    @PrimaryKey val id: Int,
    val name: String,
    val location: LatLng
)

data class LatLng(val lat: Double, val lng: Double)

@Dao
interface PoiDao {
    @Query("SELECT * FROM poi")
    suspend fun getAll(): List<Poi>

    @Insert
    suspend fun insertAll(vararg pois: Poi)

    @Delete
    suspend fun delete(poi: Poi)
}

