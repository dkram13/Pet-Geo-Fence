package com.example.pgfapp.services

import com.example.pgfapp.utilities.NotificationUtils
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
import androidx.lifecycle.LiveData
import com.example.pgfapp.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapObserveRelation
import org.eclipse.californium.core.CoapResponse
import org.json.JSONObject
import com.example.pgfapp.DatabaseStuff.Entities.Pets
import com.example.pgfapp.DatabaseStuff.UserDatabase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LocationForegroundService : Service() {
    private val TAG = "LFG Service"
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private val activeObservations = mutableMapOf<String, CoapObserveRelation>()
    private lateinit var notificationUtils: NotificationUtils
    private val observedPetIMEIs = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()

        notificationUtils = NotificationUtils(this)
        notificationUtils.createNotificationChannel()

        startForegroundService()

        val auth = Firebase.auth
        val user = auth.currentUser
        val userUuid = user?.uid ?: ""

        // Observe pets list for this user
        val petsDao = UserDatabase.getDatabase(application).PetsDao()
        val petsLiveData: LiveData<List<Pets>> = petsDao.grabPets(userUuid)
        petsLiveData.observeForever { pets ->
            /*val newPets = pets.filter { pet -> !observedPetIMEIs.contains(pet.IMEI) }
            if (newPets.isNotEmpty()) {
                newPets.forEach { pet -> observedPetIMEIs.add(pet.IMEI) }

                //cancelAllObservations()

                Log.d(TAG, "Service started for ${pets.size} pets")
                pets.forEach { pet ->
                    startObservingLocation(pet)
                }
            }*/

            cancelAllObservations()

            Log.d(TAG, "Service started for ${pets.size} pets")
            pets.forEach { pet ->
                startObservingLocation(pet)
            }
        }
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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun startObservingLocation(pet: Pets) {
        // Construct the URI dynamically using the pet's IMEI
        val uri = "coap://15.204.232.135:5683/${pet.IMEI}/batch"

        // Observe the location for this pet and store the CoapObserveRelation in the map
        observeCoapResource(uri, coroutineScope, pet.IMEI) { newLocation ->
            Log.d(TAG, "New Location Observed for Pet IMEI: ${pet.IMEI} - $newLocation")

            // Parse the new location JSON
            val jsonObject = JSONObject(newLocation)
            val latitude = jsonObject.getDouble("latitude")
            val longitude = jsonObject.getDouble("longitude")
            val newLatLng = LatLng(latitude, longitude)
            val inBounds = jsonObject.getInt("in_bounds")
            val batteryLevel = jsonObject.getInt("battery")
            val accuracy = jsonObject.getInt("accuracy")

            val batteryPercentage = convertBatteryToPercentage(batteryLevel)

            if (inBounds != 1) {
                notificationUtils.sendNotification(
                    "${pet.PetName} is out of bounds!",
                    "Latitude: $latitude, Longitude: $longitude",
                    5353
                )
            }

            updateBatteryLevel(pet.IMEI, batteryPercentage)
            updateLocationInMapsActivity(newLatLng, accuracy.toString(), pet.IMEI)
        }
    }

    private fun updateLocationInMapsActivity(newLatLng: LatLng, accuracy: String, petIMEI: String) {
        // Send a broadcast to MapsActivity with the new location
        val intent = Intent("com.example.pgfapp.LOCATION_UPDATE")
        intent.putExtra("newLocation", newLatLng)
        intent.putExtra("accuracy", accuracy)
        intent.putExtra("petIMEI", petIMEI)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        job.cancel()

        // Stop observing when the service is destroyed
        Log.d(TAG, "Destroying Observer Instance")
        cancelAllObservations()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun observeCoapResource(uri: String, coroutineScope: CoroutineScope, petIMEI: String, onUpdate: (String) -> Unit) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val client = CoapClient(uri)

                activeObservations[petIMEI] = client.observe(object : CoapHandler {
                    override fun onLoad(response: CoapResponse?) {
                        response?.let {
                            val responseText = it.responseText
                            onUpdate(responseText)
                        }
                    }

                    override fun onError() {
                        Log.e(TAG, "Observation failed")
                    }
                })
            }
        }
    }

    private fun cancelAllObservations() {
        activeObservations.forEach { (imei, observeRelation) ->
            observeRelation.proactiveCancel() // Cancel each observation
            Log.d(TAG, "Observation canceled for IMEI: $imei")
        }
        activeObservations.clear() // Clear the map of active observations
    }

    private fun updateBatteryLevel(petIMEI: String, batteryLevel: Int) {
        // Create an intent for the battery update
        val intent = Intent("com.example.pgfapp.BATTERY_UPDATE")
        intent.putExtra("petIMEI", petIMEI)
        intent.putExtra("batteryLevel", batteryLevel)
        sendBroadcast(intent)
    }

    fun convertBatteryToPercentage(batteryLevelMv: Int, minVoltageMv: Int = 3000, maxVoltageMv: Int = 4200): Int {
        val percentage = ((batteryLevelMv - minVoltageMv).toDouble() / (maxVoltageMv - minVoltageMv) * 100).toInt()
        return percentage.coerceIn(0, 100) // Ensure the value is between 0 and 100
    }
}