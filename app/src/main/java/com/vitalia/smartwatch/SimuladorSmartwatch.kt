package com.vitalia.smartwatch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Simula los sensores de un smartwatch y publica un WatchData cada 2 segundos. */
class SimuladorSmartwatch(
    private val sink: DataSink,
    private val nombre: String = "María López"
) {
    companion object {
        const val INTERVALO_MS = 2000L
        // Coordenadas de "casa" (cámbialas si quieres)
        const val CASA_LAT = 13.6929
        const val CASA_LON = -89.2182
    }

    private var fc = 74
    private var bateria = 100.0
    private var caidas = 0
    private var alertasLeves = 0
    private var horasActivas = 0.0
    private var enCasa = true
    private var movimiento = "Normal"
    private var ticksFcAlta = 0
    private var ticksCaida = 0
    private var sosActivo = false
    private var fcAltaNotificada = false
    private var bateriaBajaNotificada = false
    private val alertas = mutableListOf<Alerta>()
    private var job: Job? = null

    private val _datos = MutableStateFlow(construir())
    val datos: StateFlow<WatchData> = _datos.asStateFlow()

    fun iniciar(scope: CoroutineScope) {
        if (job?.isActive == true) return
        job = scope.launch {
            while (isActive) {
                delay(INTERVALO_MS)
                tick()
            }
        }
    }

    fun detener() { job?.cancel() }

    // ---------- Acciones manuales (botones del reloj) ----------

    fun simularCaida() {
        ticksCaida = 3
        caidas++
        agregarAlerta("CAIDA", "Caída detectada")
        publicar(emergencia = true)
    }

    fun simularFcAlta() {
        ticksFcAlta = 5
    }

    fun botonEmergencia() {
        sosActivo = true
        agregarAlerta("SOS", "Botón de emergencia presionado")
        publicar(emergencia = true)
    }

    fun cancelarEmergencia() {
        sosActivo = false
        publicar(emergencia = false)
    }

    fun alternarUbicacion() {
        enCasa = !enCasa
        if (!enCasa) {
            alertasLeves++
            agregarAlerta("FUERA_DE_CASA", "Salió de casa")
        }
        publicar(emergencia = false)
    }

    // ---------- Lógica de simulación ----------

    private fun tick() {
        // Frecuencia cardíaca
        fc = if (ticksFcAlta > 0) {
            ticksFcAlta--
            Random.nextInt(115, 131)
        } else {
            (fc + Random.nextInt(-3, 4) + (74 - fc) / 8).coerceIn(58, 98)
        }

        // Movimiento
        movimiento = when {
            ticksCaida > 0 -> { ticksCaida--; "Caída detectada" }
            else -> when (Random.nextInt(100)) {
                in 0..64 -> "Normal"
                in 65..84 -> "Reposo"
                else -> "Activo"
            }
        }
        if (movimiento == "Normal" || movimiento == "Activo") {
            horasActivas += INTERVALO_MS / 3_600_000.0
        }

        // Batería: baja ~1% por minuto (30 ticks)
        bateria = (bateria - 1.0 / 30.0).coerceAtLeast(0.0)
        if (bateria <= 20 && !bateriaBajaNotificada) {
            bateriaBajaNotificada = true
            alertasLeves++
            agregarAlerta("BATERIA_BAJA", "Batería del reloj baja (${bateria.toInt()}%)")
        }

        // Alertas por frecuencia cardíaca
        if ((fc > 110 || fc < 50) && !fcAltaNotificada) {
            fcAltaNotificada = true
            alertasLeves++
            agregarAlerta(if (fc > 110) "FC_ALTA" else "FC_BAJA", "Frecuencia cardíaca anormal: $fc bpm")
        } else if (fc in 50..110) {
            fcAltaNotificada = false
        }

        publicar(emergencia = false)
    }

    private fun agregarAlerta(tipo: String, mensaje: String) {
        alertas.add(0, Alerta(tipo, mensaje, System.currentTimeMillis()))
        if (alertas.size > 20) alertas.removeAt(alertas.lastIndex)
    }

    private fun construir(): WatchData {
        val estado = when {
            sosActivo || movimiento == "Caída detectada" -> "EMERGENCIA"
            fc > 110 || fc < 50 -> "Alerta"
            else -> "Todo normal"
        }
        val lat = if (enCasa) CASA_LAT else CASA_LAT + 0.004
        val lon = if (enCasa) CASA_LON else CASA_LON + 0.003
        return WatchData(
            adultoMayor = nombre,
            estado = estado,
            frecuenciaCardiaca = fc,
            movimiento = movimiento,
            ubicacion = if (enCasa) "En casa" else "Fuera de casa",
            latitud = lat,
            longitud = lon,
            bateria = bateria.toInt(),
            caidas = caidas,
            alertasLeves = alertasLeves,
            horasActivas = Math.round(horasActivas * 100) / 100.0,
            alertas = alertas.toList(),
            timestamp = System.currentTimeMillis()
        )
    }

    private fun publicar(emergencia: Boolean) {
        val d = construir()
        _datos.value = d
        sink.enviar(d.toJson(), emergencia || d.estado == "EMERGENCIA")
    }
}
