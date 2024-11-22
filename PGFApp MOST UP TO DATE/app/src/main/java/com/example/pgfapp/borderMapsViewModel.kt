package com.example.pgfapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class BorderViewModel : ViewModel() {
    private val _borderPoints = MutableLiveData<ArrayList<LatLng>>()
    val borderPoints: LiveData<ArrayList<LatLng>> = _borderPoints

    fun setBorderPoints(points: ArrayList<LatLng>) {
        _borderPoints.value = points
    }
}