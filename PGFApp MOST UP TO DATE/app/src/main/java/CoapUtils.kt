import android.util.Log
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.CoapResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope


class CoapUtils {
    companion object {
        private const val TAG = "CoapUtils"

        // Function to observe a CoAP resource with a callback for observed data
        fun observeCoapResource(uri: String, coroutineScope: CoroutineScope, onUpdate: (String) -> Unit) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val client = CoapClient(uri)
                    client.observe(object : CoapHandler {
                        override fun onLoad(response: CoapResponse?) {
                            response?.let {
                                val responseText = it.responseText
                                //Log.d(TAG, "Observed Response: $responseText")
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
    }
}
