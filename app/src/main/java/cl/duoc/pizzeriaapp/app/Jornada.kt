package cl.duoc.pizzeriaapp.app

import cl.duoc.pizzeriaapp.data.crearInventario
import cl.duoc.pizzeriaapp.data.crearMenu
import cl.duoc.pizzeriaapp.data.crearRepartidores
import cl.duoc.pizzeriaapp.model.Pedido
import cl.duoc.pizzeriaapp.model.Producto
import cl.duoc.pizzeriaapp.model.Repartidor
import cl.duoc.pizzeriaapp.service.Inventario
import cl.duoc.pizzeriaapp.service.Registro

// Estado de una jornada: inventario, menú, repartidores, registro de clientes/catálogo y pedidos del día
// Gestión de pedidos usa una jornada durante toda la sesión; cada demostración crea una nueva,  así las entradas de prueba no afectan la operación real
class Jornada(primerIdPedido: Long = 1001L) {
    val inventario: Inventario = crearInventario()
    val menu: List<Producto> = crearMenu()
    val repartidores: List<Repartidor> = crearRepartidores()
    val registro = Registro()

    private val pedidos = mutableListOf<Pedido>()
    private var siguienteId = primerIdPedido

    init {
        menu.forEach { registro.agregarAlCatalogo(it) }
    }

    fun proximoIdPedido(): Long = siguienteId

    // Guarda el pedido en la jornada (también los rechazados) y avanza el correlativo
    fun agregarPedido(pedido: Pedido) {
        pedidos.add(pedido)
        if (pedido.idPedido >= siguienteId) siguienteId = pedido.idPedido + 1
    }

    fun pedidosDelDia(): Array<Pedido> = pedidos.toTypedArray()

    fun buscarPedido(id: Long): Pedido? = pedidos.find { it.idPedido == id }
}