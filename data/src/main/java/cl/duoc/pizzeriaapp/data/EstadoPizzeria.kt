package cl.duoc.pizzeriaapp.data

import cl.duoc.pizzeriaapp.model.Cliente
import cl.duoc.pizzeriaapp.model.Pedido
import cl.duoc.pizzeriaapp.model.Producto
import cl.duoc.pizzeriaapp.model.Repartidor

// Fotografía de la jornada que se entrega a la UI: listas de solo lectura
// Cada cambio en la jornada genera un estado nuevo, que el ViewModel publica en su LiveData
data class EstadoPizzeria(
    val catalogo: List<Producto>,
    val pedidos: List<Pedido>,
    val clientes: List<Cliente>,
    val repartidores: List<Repartidor>
)
