package cl.duoc.pizzeriaapp.data

import cl.duoc.pizzeriaapp.app.Jornada
import cl.duoc.pizzeriaapp.model.Cliente
import cl.duoc.pizzeriaapp.model.EstadoPedido
import cl.duoc.pizzeriaapp.model.ItemPedido
import cl.duoc.pizzeriaapp.model.Pedido
import cl.duoc.pizzeriaapp.model.Producto
import cl.duoc.pizzeriaapp.service.procesarPedido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// Punto de acceso a la lógica de la pizzería para la capa de UI
// La UI no conoce Jornada, Inventario ni los servicios: solo pide el estado y solicita operaciones
class PizzeriaRepositorio(private val jornada: Jornada = Jornada()) {

    private var datosCargados = false

    // Mutex: si llegan dos operaciones seguidas, se ejecutan de a una sobre la misma jornada
    private val mutex = Mutex()

    // suspend + Dispatchers.Default: registrar clientes y procesar pedidos se hace fuera del hilo principal
    // Los mensajes de procesarPedido (inventario, notificaciones, despacho) se siguen viendo en Logcat
    suspend fun cargarDatosIniciales(): EstadoPizzeria = enSegundoPlano {
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

    // Registro valida teléfono y correo (asigna valores por defecto si no son válidos)
    // Si el cliente ya existe (mismo nombre y correo) retorna el registro existente
    suspend fun registrarCliente(nombre: String, telefono: String, correo: String, direccion: String): Cliente =
        enSegundoPlano { jornada.registro.registrarCliente(nombre, telefono, correo, direccion) }

    // El pedido nuevo toma el siguiente correlativo y la fecha de hoy (define si aplica la promoción)
    // Inventario lo acepta (En preparación) o lo rechaza por falta de ingredientes
    // ItemPedido lanza IllegalArgumentException si la cantidad está fuera de rango
    suspend fun crearPedido(cliente: Cliente, producto: Producto, cantidad: Int, notas: String): Pedido =
        enSegundoPlano {
            val pedido = Pedido(
                idPedido = jornada.proximoIdPedido(),
                cliente = cliente,
                items = listOf(ItemPedido(producto, cantidad)),
                notas = notas.ifBlank { null }
            )
            jornada.agregarPedido(pedido)
            procesarPedido(pedido, jornada.inventario, jornada.repartidores, EstadoPedido.EN_PREPARACION)
            pedido
        }

    fun obtenerEstado(): EstadoPizzeria = EstadoPizzeria(
        catalogo = jornada.registro.obtenerCatalogo(),
        pedidos = jornada.pedidosDelDia().toList(),
        clientes = jornada.registro.obtenerClientes(),
        repartidores = jornada.repartidores
    )

    private suspend fun <T> enSegundoPlano(operacion: () -> T): T =
        withContext(Dispatchers.Default) { mutex.withLock { operacion() } }

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
