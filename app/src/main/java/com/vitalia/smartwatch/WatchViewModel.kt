package com.vitalia.smartwatch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

class WatchViewModel(application: Application) : AndroidViewModel(application) {

    val watchId = WatchIdentity.get(application)
    private val simulador = SimuladorSmartwatch(sink = FirebaseRestDataSink(watchId))

    val datos = simulador.datos

    init { simulador.iniciar(viewModelScope) }

    fun simularCaida() = simulador.simularCaida()
    fun simularFcAlta() = simulador.simularFcAlta()
    fun emergencia() = simulador.botonEmergencia()
    fun cancelarEmergencia() = simulador.cancelarEmergencia()
    fun alternarUbicacion() = simulador.alternarUbicacion()
    fun actualizarAcelerometro(x: Float, y: Float, z: Float) =
        simulador.actualizarAcelerometro(x, y, z)
    fun actualizarBateria(porcentaje: Int) = simulador.actualizarBateria(porcentaje)

    override fun onCleared() {
        simulador.detener()
        super.onCleared()
    }
}
