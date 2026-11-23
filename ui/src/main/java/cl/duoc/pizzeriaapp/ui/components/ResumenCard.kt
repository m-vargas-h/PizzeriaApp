package cl.duoc.pizzeriaapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.pizzeriaapp.data.EstadoPizzeria

// Mensaje de resumen de la pantalla inicial: se recalcula con cada estado que publica el ViewModel
@Composable
fun ResumenCard(estado: EstadoPizzeria, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Resumen de la jornada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DatoResumen("Clientes registrados", estado.totalClientes.toString())
            DatoResumen("Pedidos realizados", estado.totalPedidos.toString())
            DatoResumen("Último cliente registrado", estado.ultimoCliente?.nombre ?: "Sin clientes")
        }
    }
}

@Composable
private fun DatoResumen(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
