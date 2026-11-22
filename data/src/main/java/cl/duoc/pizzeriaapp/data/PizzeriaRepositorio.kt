package cl.duoc.pizzeriaapp.data

import cl.duoc.pizzeriaapp.app.Jornada
import cl.duoc.pizzeriaapp.model.EstadoPedido
import cl.duoc.pizzeriaapp.service.procesarPedido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Punto de acceso a la lógica de la pizzería para la capa de UI
// La UI no conoce Jornada, Inventario ni los servicios: solo pide el estado al repositorio
class PizzeriaRepositorio(private val jornada: Jornada = Jornada()) {

    private var datosCargados = false

    // suspend + Dispatchers.Default: registrar clientes y procesar pedidos se hace fuera del hilo principal
    // Los mensajes de procesarPedido (inventario, notificaciones, despacho) se siguen viendo en Logcat
    suspend fun cargarDatosIniciales(): EstadoPizzeria = withContext(Dispatchers.Default) {
        if (!datosCargados) {
            val clientes = registrarClientesIniciales(jornada.registro)
            crearPedidosIniciales(clientes, jornada.menu).zip(ESTADOS_OBJETIVO).forEach { (pedido, objetivo) ->
                jornada.agregarPedido(pedido)
                procesarPedido(pedido, jornada.inventario, jornada.repartidores, objetivo)
            }
            datosCargados = true
        }
        obtenerEstado()
    }

    fun obtenerEstado(): EstadoPizzeria = EstadoPizzeria(
        catalogo = jornada.registro.obtenerCatalogo(),
        pedidos = jornada.pedidosDelDia().toList(),
        clientes = jornada.registro.obtenerClientes(),
        repartidores = jornada.repartidores
    )

    companion object {
        // Estado hasta el que se lleva cada pedido precargado (#1001 a #1004)
        private val ESTADOS_OBJETIVO = listOf(
            EstadoPedido.ENTREGADO,
            EstadoPedido.EN_CAMINO,
            EstadoPedido.LISTO,
            EstadoPedido.ENTREGADO      // sin champiñones suficientes: queda rechazado
        )
    }
}
