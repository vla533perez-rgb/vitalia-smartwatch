package com.vitalia.smartwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class WatchViewModel : ViewModel() {

    // Tu compañero reemplaza LogSink() por su implementación (HTTP, WebSocket, Firebase...)
    private val simulador = SimuladorSmartwatch(sink = LogSink())

    val datos = simulador.datos

    init { simulador.iniciar(viewModelScope) }

    fun simularCaida() = simulador.simularCaida()
    fun simularFcAlta() = simulador.simularFcAlta()
    fun emergencia() = simulador.botonEmergencia()
    fun cancelarEmergencia() = simulador.cancelarEmergencia()
    fun alternarUbicacion() = simulador.alternarUbicacion()

    override fun onCleared() {
        simulador.detener()
        super.onCleared()
    }
}
