package com.example.pgfapp.DatabaseStuff

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.common.reflect.TypeToken
import com.google.gson.Gson


class GeoPointsConverter {
    @TypeConverter
    fun fromLatLngList(latLngList: ArrayList<LatLng>?): String {
        return Gson().toJson(latLngList)
    }
    @TypeConverter
    fun toLatLngList(data: String): ArrayList<LatLng>? {
        val listType = object : TypeToken<ArrayList<LatLng>>() {}.type
        return Gson().fromJson(data, listType)
    }
}