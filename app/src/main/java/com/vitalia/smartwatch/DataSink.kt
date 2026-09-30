package com.vitalia.smartwatch

import android.util.Log

/**
 * AQUÍ SERÁ EL PUNTO DE CONEXIÓN
 * implementar (HTTP POST, WebSocket, Firebase, MQTT, etc.)
 * y pasarla al ViewModel en lugar de LogSink.
 * Recibe el JSON del paquete de datos cada ~2 s y de inmediato ante una emergencia.
 */
interface DataSink {
    fun enviar(json: String, esEmergencia: Boolean)
}

/** Implementación por defecto: solo imprime en Logcat (filtro: "WatchSim"). */
class LogSink : DataSink {
    override fun enviar(json: String, esEmergencia: Boolean) {
        Log.d("WatchSim", (if (esEmergencia) "[EMERGENCIA] " else "") + json)
    }
}
