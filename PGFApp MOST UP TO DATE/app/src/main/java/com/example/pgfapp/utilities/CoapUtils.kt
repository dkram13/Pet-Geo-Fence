package com.example.pgfapp.utilities

import android.util.Log
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import org.eclipse.californium.core.CoapObserveRelation
import org.eclipse.californium.core.coap.MediaTypeRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import com.google.android.gms.maps.model.LatLng


class CoapUtils {
    companion object {
        private const val TAG = "com.example.pgfapp.utilities.CoapUtils"
        private var observeRelation: CoapObserveRelation? = null

        // Function to observe a CoAP resource with a callback for observed data
        fun observeCoapResource(uri: String, coroutineScope: CoroutineScope, onUpdate: (String) -> Unit) {
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

        // Function to cancel the CoAP observation
        fun cancelObserveCoapResource() {
            observeRelation?.let {
                it.proactiveCancel() // Cancel the observation
                Log.d(TAG, "Observation canceled")
                observeRelation = null
            } ?: Log.d(TAG, "No active observation to cancel")
        }

        fun sendCoordinates(uri: String, bounds: ArrayList<LatLng>, coroutineScope: CoroutineScope) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val client = CoapClient(uri)
                    val wkt = buildPolygonWKT(bounds)

                    Log.d("WKT", "WKT POLYGON: $wkt")

                    val response = client.put(wkt, MediaTypeRegistry.TEXT_PLAIN)

                    if (response.isSuccess) {
                        Log.d(TAG, "Coordinates sent successfully: ${response.responseText}")
                    } else {
                        Log.e(TAG, "Failed to send coordinates")
                    }
                }
            }
        }

        private fun buildPolygonWKT(bounds: ArrayList<LatLng>): String {
            if (bounds.isEmpty()) {
                return "POLYGON EMPTY"
            }
            val coordinates = bounds.joinToString(", ") { "${it.longitude} ${it.latitude}" }
            val closedCoordinates = "$coordinates, ${bounds[0].longitude} ${bounds[0].latitude}"

            return "POLYGON(($closedCoordinates))"
        }
    }
}
