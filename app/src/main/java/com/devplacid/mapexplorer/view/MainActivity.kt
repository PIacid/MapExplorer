package com.devplacid.mapexplorer.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.devplacid.mapexplorer.R
import com.devplacid.mapexplorer.ViewModel
import com.devplacid.mapexplorer.api.Place
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxSeekBar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import kotlin.math.log

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val displayedPlaces = mutableMapOf<Marker?, Place?>()

    private var gMap: GoogleMap? = null
    private var circle: Circle? = null

    private lateinit var viewModel: ViewModel
    private var selectedMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(ViewModel::class.java)

        seekBarRadius.max = 5000
        seekBarRadius.min = 1000

        seekBarRadius.progress = viewModel.areaRadiusM

        initMapIndependentListeners()

        checkPermissions()
    }

    @SuppressLint("CheckResult")
    private fun initMapIndependentListeners() {
        RxView.clicks(searchOptionsButton)
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribe { openSelectorActivity() }

        RxView.clicks(saveButton)
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribe { saveMarker() }

        RxView.clicks(deleteButton)
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribe { removeMarker() }

    }

    @SuppressLint("CheckResult")
    private fun initMapDependentListeners() {
        RxView.clicks(locateMeButton)
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribe { zoom() }

        RxSeekBar.changes(seekBarRadius)
            .skipInitialValue()
            .subscribe(
                {
                    resizeCircle(it)
                    zoom(it)
                    viewModel.onSearchRangeChanged(it)
                    expandableLayout.collapse()
                },
                {
                    it.printStackTrace()
                }
            )

        gMap?.setOnMarkerClickListener {
            selectedMarker = it
            expandableLayout.expand()
            it.showInfoWindow()
            true
        }

        gMap?.setOnMapClickListener {
            expandableLayout.collapse()
        }
    }

    fun saveMarker() {
        selectedMarker?.let {
            //TODO save it
            expandableLayout.collapse()
        }
    }

    fun removeMarker() {
        selectedMarker?.let {
            viewModel.remove(displayedPlaces[it])
            expandableLayout.collapse()
        }
    }


    private fun openSelectorActivity() {
        val intent = Intent(this.baseContext, SelectionActivity::class.java)
        viewModel.currentCategory.let {
            intent.putExtra("category", it)
        }
        expandableLayout.collapse()
        startActivityForResult(intent, 1)
    }


    private fun checkPermissions() {
        if (
            applicationContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            &&
            applicationContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            initMap()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (
            grantResults.isNotEmpty()
            &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
            &&
            grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            initMap()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        viewModel.getLocation()
    }

    @SuppressLint("MissingPermission", "CheckResult")
    override fun onMapReady(p0: GoogleMap?) {
        this.gMap = p0 ?: return
        gMap?.isMyLocationEnabled = true

        initMapDependentListeners()

        viewModel.outDataLocation.observe(this, Observer {
            when (it) {
                null -> Toast.makeText(
                    this,
                    "Location service is not available",
                    Toast.LENGTH_SHORT
                )
                    .show()
                else -> {
                    zoom()
                    drawCircle()
                }
            }
        })

        viewModel.outDataMarkers.observe(this, Observer {
            when (it) {
                null -> Toast.makeText(
                    this,
                    "Internet connection is not available",
                    Toast.LENGTH_SHORT
                )
                    .show()
                else -> {
                    displayedPlaces.keys.forEach { marker -> marker?.remove() }
                    displayedPlaces.clear()
                    it.forEach { place -> setMarker(place) }
                }
            }
        })
    }

    private fun setMarker(place: Place) {
        val marker = gMap?.addMarker(
            MarkerOptions().position(
                LatLng(
                    place.properties.lat,
                    place.properties.lon
                )
            ).title(place.properties.name)
        )
        marker?.isDraggable = true

        displayedPlaces[marker] = place
    }

    private fun getZoom(rad: Int) = (0.95 * log((40_000_000 / rad / 2.0), 2.0)).toFloat()

    private fun zoom(rad: Int = viewModel.areaRadiusM) {
        expandableLayout.collapse()
        val requiredZoom = getZoom(rad)
        gMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(viewModel.currentLatLng, requiredZoom))
    }

    private fun drawCircle() {
        val circleOptions = CircleOptions()
            .center(viewModel.currentLatLng)
            .radius(viewModel.areaRadiusM.toDouble())
            .strokeWidth(5.0f)
            .strokeColor(R.color.colorBounds)

        circle = gMap?.addCircle(circleOptions)
    }

    private fun resizeCircle(rad: Int) {
        circle?.apply {
            radius = rad.toDouble()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data != null) {
            val category = data.extras?.getString("apiName", "") ?: return
            viewModel.requestPlaces(category)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}