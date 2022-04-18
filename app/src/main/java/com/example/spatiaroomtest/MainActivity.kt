package com.example.spatiaroomtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.anbora.labs.spatia.builder.SpatiaRoom
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = AppDatabase.getInstance(applicationContext)
        val dao = db.poiDao()

        lifecycleScope.launch {
            val pois = dao.getAll()

            println("Current POIs:")
            println(pois)

            if (pois.isEmpty()) {
                db.poiDao().insertAll(
                    Poi(0, "first", LatLng(54.0, 9.0)),
                    Poi(1, "second", LatLng(48.0, 12.0))
                )
            } else if (pois.size == 2) {
                db.poiDao().insertAll(
                    Poi(2, "first", LatLng(40.0, -74.0)),
                    Poi(3, "second", LatLng(-34.0, 151.0))
                )
            }
        }
    }
}