package cl.duoc.pizzeriaapp.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// Montos del pedido: descuento por cantidad, Promo Noviembre e IVA incluido
class PedidoCalculosTest {

    private val cliente = Cliente("Juan Pérez", "+56 9 87654321", "juan@mail.com", "Av. Central 123")
    private val pepperoni = Pizza("Pepperoni", 8990, mapOf(Ingredientes.MASA to 1, Ingredientes.QUESO to 2))
    private val cuatroQuesos = Pizza("Cuatro Quesos", 9990, mapOf(Ingredientes.MASA to 1, Ingredientes.QUESO to 4))
    private val bebida = Bebida("Bebida 1.5L", 2490)
    private val combo = Combo("Combo Pareja", 19990, listOf(pepperoni, cuatroQuesos, bebida))

    // Fuera del periodo de promoción (1 al 30 de noviembre de 2026)
    private val fechaSinPromo = LocalDate.of(2026, 10, 28)
    private val fechaConPromo = LocalDate.of(2026, 11, 5)

    private fun pedido(vararg items: ItemPedido, fecha: LocalDate) =
        Pedido(1L, cliente, items.toList(), fecha = fecha)

    @Test
    fun `sin descuentos el total es el subtotal`() {
        val p = pedido(ItemPedido(pepperoni, 2), fecha = fechaSinPromo)
        assertEquals(17980L, p.subtotal)
        assertEquals(0L, p.descuento)
        assertEquals(0L, p.descuentoPromocion)
        assertEquals(17980L, p.total)
    }

    @Test
    fun `mas de 3 pizzas aplica 10 por ciento de descuento`() {
        val p = pedido(ItemPedido(pepperoni, 4), fecha = fechaSinPromo)
        assertEquals(35960L, p.subtotal)
        assertEquals(3596L, p.descuento)
        assertEquals(32364L, p.total)
    }

    @Test
    fun `exactamente 3 pizzas no aplica descuento por cantidad`() {
        val p = pedido(ItemPedido(pepperoni, 3), fecha = fechaSinPromo)
        assertEquals(0L, p.descuento)
    }

    @Test
    fun `las pizzas de un combo cuentan para el descuento`() {
        val p = pedido(ItemPedido(combo, 1), ItemPedido(pepperoni, 2), fecha = fechaSinPromo)
        assertEquals(4, p.cantidadPizzas())
        assertEquals(37970L, p.subtotal)
        assertEquals(3797L, p.descuento)
        assertEquals(34173L, p.total)
    }

    @Test
    fun `promo noviembre aplica 15 por ciento sobre productos promocionables`() {
        val p = pedido(ItemPedido(pepperoni, 3), fecha = fechaConPromo)
        assertTrue(p.aplicaPromocion())
        assertEquals(4046L, p.descuentoPromocion)
        assertEquals(22924L, p.total)
    }

    @Test
    fun `la promo se calcula despues del descuento por cantidad`() {
        val p = pedido(ItemPedido(pepperoni, 4), fecha = fechaConPromo)
        assertEquals(3596L, p.descuento)
        assertEquals(4855L, p.descuentoPromocion)
        assertEquals(27509L, p.total)
    }

    @Test
    fun `un pedido solo de bebidas no recibe la promo`() {
        val p = pedido(ItemPedido(bebida, 2), fecha = fechaConPromo)
        assertFalse(p.aplicaPromocion())
        assertEquals(4980L, p.total)
    }

    @Test
    fun `el iva incluido se desglosa del total`() {
        val pizza = Pizza("Prueba", 11900, mapOf(Ingredientes.MASA to 1))
        val p = pedido(ItemPedido(pizza, 1), fecha = fechaSinPromo)
        assertEquals(10000L, p.neto)
        assertEquals(1900L, p.iva)
        assertEquals(p.total, p.neto + p.iva)
    }

    @Test
    fun `el iva se redondea al peso mas cercano`() {
        val p = pedido(ItemPedido(pepperoni, 4), fecha = fechaSinPromo)
        // 32.364 / 1,19 = 27.196,6 -> 27.197
        assertEquals(27197L, p.neto)
        assertEquals(5167L, p.iva)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `una cantidad fuera de rango lanza excepcion`() {
        ItemPedido(pepperoni, 0)
    }
}
