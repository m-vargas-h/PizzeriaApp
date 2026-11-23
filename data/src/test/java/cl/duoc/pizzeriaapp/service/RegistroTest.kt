package cl.duoc.pizzeriaapp.service

import cl.duoc.pizzeriaapp.model.RETIRO_EN_LOCAL
import cl.duoc.pizzeriaapp.utils.CORREO_POR_DEFECTO
import cl.duoc.pizzeriaapp.utils.TELEFONO_POR_DEFECTO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

// Registro de clientes: normalización y validación de datos
class RegistroTest {

    @Test
    fun `normaliza nombre telefono y correo`() {
        val cliente = Registro().registrarCliente("  juan   PÉREZ ", "+56 9 8765 4321", "Juan@Mail.COM", "Av. Central 123")
        assertEquals("Juan Pérez", cliente.nombre)
        assertEquals("+56 9 87654321", cliente.telefono)
        assertEquals("juan@mail.com", cliente.correo)
    }

    @Test
    fun `datos invalidos toman valores por defecto`() {
        val cliente = Registro().registrarCliente("Sofía Díaz", "12345", "sofia@mail", "Pasaje 7 #89")
        assertEquals(TELEFONO_POR_DEFECTO, cliente.telefono)
        assertEquals(CORREO_POR_DEFECTO, cliente.correo)
    }

    @Test
    fun `sin direccion queda como retiro en local`() {
        val cliente = Registro().registrarCliente("Pedro Rojas", "87651234", "pedro@correo.cl", "  ")
        assertEquals(RETIRO_EN_LOCAL, cliente.direccion)
    }

    @Test
    fun `un cliente duplicado no se registra dos veces`() {
        val registro = Registro()
        val original = registro.registrarCliente("Ana Soto", "912345678", "ana@mail.cl", "Los Aromos 456")
        val repetido = registro.registrarCliente("ANA SOTO", "987654321", "ana@mail.cl", "Otra dirección")
        assertSame(original, repetido)
        assertEquals(1, registro.obtenerClientes().size)
    }

    @Test
    fun `obtenerClientes conserva el orden de registro`() {
        val registro = Registro()
        registro.registrarCliente("Ana Soto", "912345678", "ana@mail.cl", null)
        registro.registrarCliente("Luis Mora", "987654321", "luis@mail.cl", null)
        assertEquals(listOf("Ana Soto", "Luis Mora"), registro.obtenerClientes().map { it.nombre })
    }
}
