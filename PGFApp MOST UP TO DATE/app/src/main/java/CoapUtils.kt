import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import org.eclipse.californium.core.CoapClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoapUtils {
    companion object {
        suspend fun sendCoapGetRequest(uri: String): String = withContext(Dispatchers.IO) {
            val client = CoapClient(uri)
            val response = client.get()
            response.responseText
        }
    }

    @Composable
    fun OnSendCoapGetRq() {
        //get the coroutine scope
        val coroutineScope = rememberCoroutineScope()

        //launch the coroutine scope
        LaunchedEffect(coroutineScope) {
            coroutineScope.launch {
                //obtain the uri
                val uri = "coap://californium.eclipseprojects.io/echo/cali.Ahzio.nRF9160"
                //send a get request to the coap server
                val response = CoapUtils.sendCoapGetRequest(uri)
                Log.d("MainActivity", "Response: $response") //keep this for now, remove later
            }
        }
    }
}
