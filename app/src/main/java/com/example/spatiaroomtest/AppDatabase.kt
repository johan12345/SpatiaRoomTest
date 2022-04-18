package com.example.spatiaroomtest

import android.content.Context
import androidx.room.*
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import co.anbora.labs.spatia.builder.SpatiaRoom

@Database(entities = [Poi::class], version = 2)
@TypeConverters(PointTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun poiDao(): PoiDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            val db = SpatiaRoom.databaseBuilder(context, AppDatabase::class.java, "db")
                .addMigrations(MIGRATION)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // init spatialite
                        db.query("SELECT InitSpatialMetaData();").moveToNext()
                        // add geometry column to Poi table
                        db.query("SELECT AddGeometryColumn('Poi', 'location', 4326, 'POINT', 'XY');").moveToNext()
                        db.query("SELECT CreateSpatialIndex('Poi', 'location');").moveToNext()
                    }
                })
                .build()
            instance = db
            return db
        }

        val MIGRATION = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.beginTransaction()
                try {
                    // init spatialite
                    db.query("SELECT InitSpatialMetaData();").moveToNext()

                    // add geometry column and set it based on lat/lng columns
                    db.query("SELECT AddGeometryColumn('Poi', 'location', 4326, 'POINT', 'XY');").moveToNext()
                    db.execSQL("UPDATE `Poi` SET `location` = GeomFromText('POINT('||\"lat\"||' '||\"lng\"||')',4326);")

                    // recreate table to remove lat/lng columns
                    db.execSQL("CREATE TABLE `PoiNew` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `location` BLOB NOT NULL, PRIMARY KEY(`id`))")
                    db.query("SELECT AddGeometryColumn('PoiNew', 'location', 4326, 'POINT', 'XY');").moveToNext()
                    db.query("SELECT CreateSpatialIndex('PoiNew', 'location');").moveToNext()

                    db.execSQL("INSERT INTO `PoiNew` SELECT `id`, `name`, `location` FROM `Poi`")

                    db.execSQL("DROP TABLE `Poi`")
                    db.execSQL("ALTER TABLE `PoiNew` RENAME TO `Poi`")
                } finally {
                    db.setTransactionSuccessful()
                }
                db.endTransaction()
            }
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

