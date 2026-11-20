package cl.duoc.pizzeriaapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.pizzeriaapp.Greeting
import cl.duoc.pizzeriaapp.app.Jornada
import cl.duoc.pizzeriaapp.data.crearPedidosIniciales
import cl.duoc.pizzeriaapp.data.registrarClientesIniciales
import cl.duoc.pizzeriaapp.model.Cliente
import cl.duoc.pizzeriaapp.model.Combo
import cl.duoc.pizzeriaapp.model.EstadoPedido
import cl.duoc.pizzeriaapp.model.Pedido
import cl.duoc.pizzeriaapp.model.PeriodoPromocion
import cl.duoc.pizzeriaapp.model.Producto
import cl.duoc.pizzeriaapp.model.Usuario
import cl.duoc.pizzeriaapp.service.procesarPedido
import cl.duoc.pizzeriaapp.annotations.esPromocionable
import cl.duoc.pizzeriaapp.utils.formatearFecha
import cl.duoc.pizzeriaapp.utils.formatearPesos

// Jornada lista para mostrar: clientes registrados y pedidos precargados ya procesados
private class DatosJornada(val jornada: Jornada, val clientes: List<Cliente>)

// Misma secuencia que la demostración de consola: cada pedido precargado se lleva hasta su estado objetivo
// Los mensajes que imprime procesarPedido (inventario, notificaciones, despacho) se ven en Logcat
private fun prepararJornada(): DatosJornada {
    val jornada = Jornada()
    val clientes = registrarClientesIniciales(jornada.registro)
    val objetivos = listOf(
        EstadoPedido.ENTREGADO,
        EstadoPedido.EN_CAMINO,
        EstadoPedido.LISTO,
        EstadoPedido.ENTREGADO      // sin champiñones suficientes: queda rechazado
    )
    crearPedidosIniciales(clientes, jornada.menu).zip(objetivos).forEach { (pedido, objetivo) ->
        jornada.agregarPedido(pedido)
        procesarPedido(pedido, jornada.inventario, jornada.repartidores, objetivo)
    }
    return DatosJornada(jornada, clientes)
}

// Pantalla principal: bienvenida, menú del día, pedidos del día y usuarios registrados
@Composable
fun PizzeriaScreen(modifier: Modifier = Modifier) {
    // remember: la jornada se prepara una sola vez, no en cada recomposición
    val datos = remember { prepararJornada() }
    val jornada = datos.jornada
    val pedidos = jornada.pedidosDelDia().toList()
    // Polimorfismo: clientes y repartidores se tratan como Usuario
    val usuarios: List<Usuario> = datos.clientes + jornada.repartidores

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Greeting() }
        item { BannerPromocion() }

        item { TituloSeccion("Menú del día") }
        items(jornada.registro.obtenerCatalogo()) { producto -> TarjetaProducto(producto) }

        item { TituloSeccion("Pedidos del día") }
        items(pedidos) { pedido -> TarjetaPedido(pedido) }
        item { ResumenDelDia(pedidos) }

        item { TituloSeccion("Usuarios registrados") }
        items(usuarios) { usuario -> FilaUsuario(usuario) }
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp)
    )
}

// Periodo de promoción vigente (range de fechas de PeriodoPromocion)
@Composable
private fun BannerPromocion() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            text = "${PeriodoPromocion.NOMBRE}: ${PeriodoPromocion.PORCENTAJE_DESCUENTO}% de descuento en productos " +
                    "promocionables del ${formatearFecha(PeriodoPromocion.INICIO)} al ${formatearFecha(PeriodoPromocion.FIN)}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun TarjetaProducto(producto: Producto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = producto.nombre, fontWeight = FontWeight.Bold)
                Text(text = formatearPesos(producto.precio))
            }
            Text(text = producto.categoria.descripcion, style = MaterialTheme.typography.bodySmall)
            if (producto is Combo) {
                Text(
                    text = "Incluye: " + producto.productos.joinToString(", ") { it.nombre },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // Anotaciones: @Promocionable leída con reflection
            if (esPromocionable(producto)) {
                Text(
                    text = "★ Promocionable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TarjetaPedido(pedido: Pedido) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "#${pedido.idPedido} - ${pedido.cliente.nombre}", fontWeight = FontWeight.Bold)
                Text(
                    text = pedido.estado.descripcion,
                    color = colorEstado(pedido.estado),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = pedido.items.joinToString(", ") { "${it.cantidad}x ${it.producto.nombre}" },
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Fecha: ${formatearFecha(pedido.fecha)} | Repartidor: ${pedido.repartidor?.nombre ?: "Sin asignar"}",
                style = MaterialTheme.typography.bodySmall
            )
            if (pedido.descuento > 0) {
                Text(
                    text = "Descuento por cantidad (${Pedido.PORCENTAJE_DESCUENTO}%): -${formatearPesos(pedido.descuento)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (pedido.descuentoPromocion > 0) {
                Text(
                    text = "${PeriodoPromocion.NOMBRE} (${PeriodoPromocion.PORCENTAJE_DESCUENTO}%): -${formatearPesos(pedido.descuentoPromocion)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Total: ${formatearPesos(pedido.total)} (IVA ${formatearPesos(pedido.iva)} incluido)",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun colorEstado(estado: EstadoPedido): Color = when (estado) {
    EstadoPedido.RECHAZADO -> MaterialTheme.colorScheme.error
    EstadoPedido.ENTREGADO -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.tertiary
}

// Totales del día: los pedidos rechazados no suman al total vendido
@Composable
private fun ResumenDelDia(pedidos: List<Pedido>) {
    val validos = pedidos.filter { it.estado != EstadoPedido.RECHAZADO }
    val entregados = pedidos.count { it.estado == EstadoPedido.ENTREGADO }
    val rechazados = pedidos.size - validos.size
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Entregados: $entregados | En curso: ${validos.size - entregados} | Rechazados: $rechazados")
            Text(text = "Total vendido: ${formatearPesos(validos.sumOf { it.total })}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilaUsuario(usuario: Usuario) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = usuario.describir())
        Text(text = usuario.contacto(), style = MaterialTheme.typography.bodySmall)
    }
}
