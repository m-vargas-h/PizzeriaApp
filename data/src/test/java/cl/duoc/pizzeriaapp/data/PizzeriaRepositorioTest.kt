package cl.duoc.pizzeriaapp.data

import cl.duoc.pizzeriaapp.model.EstadoPedido
import cl.duoc.pizzeriaapp.model.RETIRO_EN_LOCAL
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

// Datos del resumen de la pantalla inicial y su actualización al registrar clientes y pedidos
// runBlocking: ejecuta las funciones suspend del repositorio dentro de la prueba
class PizzeriaRepositorioTest {

    @Test
    fun `el estado inicial tiene 4 clientes y 4 pedidos`() {
        runBlocking {
            val estado = PizzeriaRepositorio().cargarDatosIniciales()
            assertEquals(4, estado.totalClientes)
            assertEquals(4, estado.totalPedidos)
            assertEquals("Sofía Díaz", estado.ultimoCliente?.nombre)
        }
    }

    @Test
    fun `cargar dos veces no duplica los datos`() {
        runBlocking {
            val repositorio = PizzeriaRepositorio()
            repositorio.cargarDatosIniciales()
            val estado = repositorio.cargarDatosIniciales()
            assertEquals(4, estado.totalPedidos)
        }
    }

    @Test
    fun `registrar un cliente actualiza total y ultimo cliente`() {
        runBlocking {
            val repositorio = PizzeriaRepositorio()
            repositorio.cargarDatosIniciales()
            val cliente = repositorio.registrarCliente("Camila Torres", "912345678", "camila@mail.cl", "")
            val estado = repositorio.obtenerEstado()
            assertEquals(5, estado.totalClientes)
            assertEquals("Camila Torres", estado.ultimoCliente?.nombre)
            assertEquals(RETIRO_EN_LOCAL, cliente.direccion)
        }
    }

    @Test
    fun `crear un pedido usa el siguiente correlativo y suma al total`() {
        runBlocking {
            val repositorio = PizzeriaRepositorio()
            val inicial = repositorio.cargarDatosIniciales()
            val pepperoni = inicial.catalogo.first { it.nombre == "Pepperoni" }
            val pedido = repositorio.crearPedido(inicial.clientes.first(), pepperoni, 1, "")
            assertEquals(1005L, pedido.idPedido)
            assertEquals(EstadoPedido.EN_PREPARACION, pedido.estado)
            assertEquals(5, repositorio.obtenerEstado().totalPedidos)
        }
    }

    @Test
    fun `un pedido sin ingredientes suficientes queda rechazado`() {
        runBlocking {
            val repositorio = PizzeriaRepositorio()
            val inicial = repositorio.cargarDatosIniciales()
            val vegetariana = inicial.catalogo.first { it.nombre == "Vegetariana" }
            // Quedan 3 champiñones después de los pedidos precargados; 2 vegetarianas necesitan 4
            val pedido = repositorio.crearPedido(inicial.clientes.first(), vegetariana, 2, "")
            assertEquals(EstadoPedido.RECHAZADO, pedido.estado)
        }
    }
}
