package cl.duoc.pizzeriaapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pizzeriaapp.data.EstadoPizzeria
import cl.duoc.pizzeriaapp.data.PizzeriaRepositorio
import cl.duoc.pizzeriaapp.model.Cliente
import cl.duoc.pizzeriaapp.model.EstadoPedido
import cl.duoc.pizzeriaapp.model.Producto
import cl.duoc.pizzeriaapp.utils.CORREO_POR_DEFECTO
import cl.duoc.pizzeriaapp.utils.TELEFONO_POR_DEFECTO
import cl.duoc.pizzeriaapp.utils.formatearPesos
import kotlinx.coroutines.launch

// ViewModel: intermediario entre la lógica (repositorio en :data) y las pantallas Compose
// Sobrevive a cambios de configuración (rotación), así la jornada no se vuelve a procesar
class PizzeriaViewModel(
    private val repositorio: PizzeriaRepositorio = PizzeriaRepositorio()
) : ViewModel() {

    // MutableLiveData privado: solo el ViewModel publica cambios; la UI observa el LiveData de solo lectura
    private val _estado = MutableLiveData<EstadoPizzeria>()
    val estado: LiveData<EstadoPizzeria> = _estado

    // Mensaje de confirmación o error de la última operación (se muestra en un Snackbar)
    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> = _mensaje

    init {
        // viewModelScope: la corrutina se cancela sola si el ViewModel se destruye
        viewModelScope.launch {
            _estado.value = repositorio.cargarDatosIniciales()
        }
    }

    fun registrarCliente(nombre: String, telefono: String, correo: String, direccion: String) {
        viewModelScope.launch {
            val clientesAntes = _estado.value?.totalClientes ?: 0
            val cliente = repositorio.registrarCliente(nombre, telefono, correo, direccion)
            val nuevoEstado = repositorio.obtenerEstado()
            // Publicar un estado nuevo actualiza el resumen y las listas en pantalla
            _estado.value = nuevoEstado

            _mensaje.value = if (nuevoEstado.totalClientes == clientesAntes) {
                "${cliente.nombre} ya estaba registrado"
            } else {
                val avisos = listOfNotNull(
                    "teléfono".takeIf { cliente.telefono == TELEFONO_POR_DEFECTO },
                    "correo".takeIf { cliente.correo == CORREO_POR_DEFECTO }
                )
                "Cliente registrado: ${cliente.nombre}" +
                        if (avisos.isEmpty()) "" else " (sin ${avisos.joinToString(" ni ")} válido)"
            }
        }
    }

    fun crearPedido(cliente: Cliente, producto: Producto, cantidad: Int, notas: String) {
        viewModelScope.launch {
            _mensaje.value = try {
                val pedido = repositorio.crearPedido(cliente, producto, cantidad, notas)
                _estado.value = repositorio.obtenerEstado()
                if (pedido.estado == EstadoPedido.RECHAZADO) {
                    "Pedido #${pedido.idPedido} rechazado por falta de ingredientes"
                } else {
                    "Pedido #${pedido.idPedido} de ${cliente.nombre}: ${pedido.estado.descripcion} (${formatearPesos(pedido.total)})"
                }
            } catch (e: IllegalArgumentException) {
                // Validaciones del modelo (por ejemplo, cantidad fuera de rango)
                e.message
            }
        }
    }

    // La UI avisa que ya mostró el mensaje, para no repetirlo al recomponer
    fun mensajeMostrado() {
        _mensaje.value = null
    }
}
