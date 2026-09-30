package com.vitalia.smartwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText

private val VerdeVitalia = Color(0xFF52E37E)
private val RojoEmergencia = Color(0xFFE53935)
private val AmarilloAlerta = Color(0xFFFFC107)

class MainActivity : ComponentActivity() {
    private val vm: WatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { PantallaReloj(vm) } }
    }
}

@Composable
fun PantallaReloj(vm: WatchViewModel) {
    val d by vm.datos.collectAsState()
    val listState = rememberScalingLazyListState()

    val colorEstado = when (d.estado) {
        "EMERGENCIA" -> RojoEmergencia
        "Alerta" -> AmarilloAlerta
        else -> VerdeVitalia
    }

    Box(Modifier.fillMaxSize()) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Text("Vitalia", color = VerdeVitalia, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            item { Text(d.adultoMayor, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            item {
                Text(d.estado, color = colorEstado, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            item { Dato("❤️ Frecuencia", "${d.frecuenciaCardiaca} bpm") }
            item { Dato("🚶 Movimiento", d.movimiento) }
            item { Dato("📍 Ubicación", d.ubicacion) }
            item { Dato("🔋 Batería", "${d.bateria}%") }
            item { Dato("Eventos", "${d.caidas} caídas · ${d.alertasLeves} alertas leves") }
            item { Dato("Horas activas", "%.2f h".format(d.horasActivas)) }

            item {
                Chip(
                    onClick = { if (d.estado == "EMERGENCIA") vm.cancelarEmergencia() else vm.emergencia() },
                    label = {
                        Text(if (d.estado == "EMERGENCIA") "Cancelar SOS" else "🚑 Emergencia (SOS)",
                            textAlign = TextAlign.Center)
                    },
                    colors = ChipDefaults.chipColors(backgroundColor = RojoEmergencia)
                )
            }

            // Controles de simulación
            item { Text("Simulación", fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp)) }
            item { BotonSim("Simular caída") { vm.simularCaida() } }
            item { BotonSim("FC Alta") { vm.simularFcAlta() } }
            item { BotonSim("Salir de Casa/Entrar") { vm.alternarUbicacion() } }
        }
        TimeText()
    }
}

@Composable
private fun Dato(titulo: String, valor: String) {
    Text("$titulo: $valor", fontSize = 12.sp, textAlign = TextAlign.Center)
}

@Composable
private fun BotonSim(texto: String, onClick: () -> Unit) {
    Chip(
        onClick = onClick,
        label = { Text(texto, fontSize = 12.sp) },
        colors = ChipDefaults.secondaryChipColors()
    )
}
