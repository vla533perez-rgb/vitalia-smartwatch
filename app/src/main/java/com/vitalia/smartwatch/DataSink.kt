package com.vitalia.smartwatch

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/** Recibe telemetría periódica y eventos de emergencia del simulador. */
interface DataSink {
    fun enviar(json: String, esEmergencia: Boolean)
}

/** Emisor local para depuración (filtro de Logcat: "WatchSim"). */
class LogSink : DataSink {
    override fun enviar(json: String, esEmergencia: Boolean) {
        Log.d("WatchSim", (if (esEmergencia) "[EMERGENCIA] " else "") + json)
    }
}

object FirebaseConfig {
    const val DATABASE_URL = "https://vitalia-app-aaf99-default-rtdb.firebaseio.com"
}

object WatchIdentity {
    fun get(context: Context): String {
        val preferences = context.getSharedPreferences("vitalia", Context.MODE_PRIVATE)
        return preferences.getString("watchId", null) ?: UUID.randomUUID().toString().also {
            preferences.edit().putString("watchId", it).apply()
        }
    }
}

class FirebaseRestDataSink(private val watchId: String) : DataSink {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun enviar(json: String, esEmergencia: Boolean) {
        scope.launch {
            var connection: HttpURLConnection? = null
            try {
                val activeConnection = URL("${FirebaseConfig.DATABASE_URL.trimEnd('/')}/devices/$watchId/latest.json")
                    .openConnection() as HttpURLConnection
                connection = activeConnection
                activeConnection.requestMethod = "PUT"
                activeConnection.connectTimeout = 5000
                activeConnection.readTimeout = 5000
                activeConnection.doOutput = true
                activeConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                activeConnection.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                val responseCode = activeConnection.responseCode
                if (responseCode !in 200..299) {
                    Log.w("WatchSim", "Firebase respondió HTTP $responseCode")
                }
            } catch (error: Exception) {
                Log.w("WatchSim", "No se pudo enviar la telemetría", error)
            } finally {
                connection?.disconnect()
            }
        }
    }
}
