package cl.duoc.pizzeriaapp.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

// Cambios de estado del pedido y asignación de repartidor
class PedidoEstadosTest {

    private val cliente = Cliente("Ana Soto", "+56 9 12345678", "ana.soto@mail.cl", "Los Aromos 456")
    private val pizza = Pizza("Pepperoni", 8990, mapOf(Ingredientes.MASA to 1))

    private fun nuevoPedido() = Pedido(1002L, cliente, listOf(ItemPedido(pizza, 1)))

    @Test
    fun `un pedido nuevo parte pendiente y sin repartidor`() {
        val pedido = nuevoPedido()
        assertEquals(EstadoPedido.PENDIENTE, pedido.estado)
        assertNull(pedido.repartidor)
    }

    @Test
    fun `un estado final no admite cambios`() {
        val pedido = nuevoPedido()
        assertTrue(pedido.cambiarEstado(EstadoPedido.ENTREGADO))
        assertFalse(pedido.cambiarEstado(EstadoPedido.EN_CAMINO))
        assertEquals(EstadoPedido.ENTREGADO, pedido.estado)
    }

    @Test
    fun `solo se asigna repartidor a un pedido listo`() {
        val pedido = nuevoPedido()
        val repartidor = Repartidor("María", "+56 9 33334444", "maria@pizzeria.cl", 1.5)
        assertFalse(repartidor.tomarPedido(pedido))
        assertTrue(repartidor.disponible)
        assertEquals(EstadoPedido.PENDIENTE, pedido.estado)
    }

    @Test
    fun `flujo completo de despacho y entrega`() {
        val pedido = nuevoPedido()
        val repartidor = Repartidor("María", "+56 9 33334444", "maria@pizzeria.cl", 1.5)
        pedido.cambiarEstado(EstadoPedido.EN_PREPARACION)
        pedido.cambiarEstado(EstadoPedido.LISTO)

        assertTrue(repartidor.tomarPedido(pedido))
        assertEquals(EstadoPedido.EN_CAMINO, pedido.estado)
        assertSame(repartidor, pedido.repartidor)
        assertFalse(repartidor.disponible)

        assertTrue(repartidor.entregarPedido(pedido))
        assertEquals(EstadoPedido.ENTREGADO, pedido.estado)
        assertTrue(repartidor.disponible)
    }

    @Test
    fun `un repartidor ocupado no toma otro pedido`() {
        val repartidor = Repartidor("Carlos", "+56 9 11112222", "carlos@pizzeria.cl", 4.0)
        val primero = nuevoPedido().apply { cambiarEstado(EstadoPedido.LISTO) }
        val segundo = nuevoPedido().apply { cambiarEstado(EstadoPedido.LISTO) }
        assertTrue(repartidor.tomarPedido(primero))
        assertFalse(repartidor.tomarPedido(segundo))
        assertEquals(EstadoPedido.LISTO, segundo.estado)
    }
}
