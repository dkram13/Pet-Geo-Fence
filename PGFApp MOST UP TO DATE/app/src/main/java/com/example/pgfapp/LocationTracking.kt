package com.example.pgfapp

import CoapUtils
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import com.google.android.gms.maps.model.LatLng
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapObserveRelation
import org.eclipse.californium.core.CoapResponse
import org.json.JSONObject

class LocationForegroundService : Service() {
    private val TAG = "LFG Service"
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private var observeRelation: CoapObserveRelation? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startObservingLocation()
    }

    private fun startForegroundService() {
        val channelId = "LocationTrackingChannel"
        val channelName = "Location Tracking"

        // Create notification channel (required for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking")
            .setContentText("Tracking Collar...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true) // Make it ongoing (non-dismissible)
            .build()

        // Start the service in the foreground with the notification
        startForeground(1, notification)
    }

    private fun startObservingLocation() {
        val uri = "coap://15.204.232.135:5683/batch"

        observeCoapResource(uri, coroutineScope) { newLocation ->
            Log.d(TAG, "New Location Observed: $newLocation")

            // Parse the new location JSON
            val jsonObject = JSONObject(newLocation)
            val latitude = jsonObject.getDouble("latitude")
            val longitude = jsonObject.getDouble("longitude")
            val newLatLng = LatLng(latitude, longitude)

            // Send the new location to MapsActivity or update the marker directly
            updateLocationInMapsActivity(newLatLng)
        }
    }

    private fun updateLocationInMapsActivity(newLatLng: LatLng) {
        // Send a broadcast to MapsActivity with the new location
        val intent = Intent("com.example.pgfapp.LOCATION_UPDATE")
        intent.putExtra("newLocation", newLatLng)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        job.cancel()

        // Stop observing when the service is destroyed
        Log.d(TAG, "Destroying Observer Instance")
        cancelObserveCoapResource()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun observeCoapResource(uri: String, coroutineScope: CoroutineScope, onUpdate: (String) -> Unit) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val client = CoapClient(uri)

                observeRelation = client.observe(object : CoapHandler {
                    override fun onLoad(response: CoapResponse?) {
                        response?.let {
                            val responseText = it.responseText
                            onUpdate(responseText) // Call the callback with new data
                        }
                    }

                    override fun onError() {
                        Log.e(TAG, "Observation failed")
                    }
                })
            }
        }
    }

    private fun cancelObserveCoapResource() {
        observeRelation?.let {
            it.proactiveCancel() // Cancel the observation
            Log.d(TAG, "Observation canceled")
            observeRelation = null
        } ?: Log.d(TAG, "No active observation to cancel")
    }
}

/*
class LocationForegroundService : Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startObservingLocation()
    }

    private fun startForegroundService() {
        val channelId = "LocationTrackingChannel"
        val channelName = "Location Tracking"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking")
            .setContentText("Tracking Collar...")
            .build()

        startForeground(1, notification)
    }

    private fun startObservingLocation() {
        val uri = "coap://15.204.232.135:5683/batch"

        CoapUtils.observeCoapResource(uri, coroutineScope) { newLocation ->
            Log.d("com.example.pgfapp.LocationForegroundService", "New Location Observed: $newLocation")

            val jsonObject = JSONObject(newLocation)
            val latitude = jsonObject.getDouble("latitude")
            val longitude = jsonObject.getDouble("longitude")
            val newLatLng = LatLng(latitude, longitude)

            // Send the new location to MapsActivity or update the marker directly
            updateLocationInMapsActivity(newLatLng)
        }
    }

    private fun updateLocationInMapsActivity(newLatLng: LatLng) {
        // Send a broadcast to MapsActivity with the new location
        val intent = Intent("com.example.pgfapp.LOCATION_UPDATE")
        intent.putExtra("newLocation", newLatLng)
        sendBroadcast(intent)
    }

    @Override
    override fun onDestroy() {
        Log.d("com.example.pgfapp.LocationForegroundService", "Destroying Observer Instance")
        CoapUtils.cancelObserveCoapResource() // Stop observing when service is destroyed
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
*/