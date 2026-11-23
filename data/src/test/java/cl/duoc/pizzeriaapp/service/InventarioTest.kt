package cl.duoc.pizzeriaapp.service

import cl.duoc.pizzeriaapp.model.Cliente
import cl.duoc.pizzeriaapp.model.EstadoPedido
import cl.duoc.pizzeriaapp.model.Ingredientes
import cl.duoc.pizzeriaapp.model.ItemPedido
import cl.duoc.pizzeriaapp.model.Pedido
import cl.duoc.pizzeriaapp.model.Pizza
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Aceptación o rechazo de pedidos según el stock de ingredientes
class InventarioTest {

    private val cliente = Cliente("Pedro Rojas", "+56 9 87651234", "pedro@correo.cl", "Retiro en local")
    private val cuatroQuesos = Pizza("Cuatro Quesos", 9990, mapOf(Ingredientes.MASA to 1, Ingredientes.QUESO to 4))

    private fun pedido(id: Long, cantidad: Int) = Pedido(id, cliente, listOf(ItemPedido(cuatroQuesos, cantidad)))

    @Test
    fun `con stock suficiente acepta el pedido y descuenta ingredientes`() {
        val inventario = Inventario(mapOf(Ingredientes.MASA to 2, Ingredientes.QUESO to 8))
        val p = pedido(1L, 2)
        assertTrue(inventario.aceptarPedido(p))
        assertEquals(EstadoPedido.EN_PREPARACION, p.estado)
        assertEquals(0, inventario.cantidadDisponible(Ingredientes.MASA))
        assertEquals(0, inventario.cantidadDisponible(Ingredientes.QUESO))
    }

    @Test
    fun `sin stock suficiente rechaza el pedido sin descontar`() {
        val inventario = Inventario(mapOf(Ingredientes.MASA to 5, Ingredientes.QUESO to 3))
        val p = pedido(2L, 1)
        assertFalse(inventario.aceptarPedido(p))
        assertEquals(EstadoPedido.RECHAZADO, p.estado)
        assertEquals(5, inventario.cantidadDisponible(Ingredientes.MASA))
        assertEquals(3, inventario.cantidadDisponible(Ingredientes.QUESO))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reabastecer con cantidad no positiva lanza excepcion`() {
        Inventario(emptyMap()).reabastecer(Ingredientes.QUESO, 0)
    }
}
