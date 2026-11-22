package cl.duoc.pizzeriaapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pizzeriaapp.data.EstadoPizzeria
import cl.duoc.pizzeriaapp.data.PizzeriaRepositorio
import kotlinx.coroutines.launch

// ViewModel: intermediario entre la lógica (repositorio en :data) y las pantallas Compose
// Sobrevive a cambios de configuración (rotación), así la jornada no se vuelve a procesar
class PizzeriaViewModel(
    private val repositorio: PizzeriaRepositorio = PizzeriaRepositorio()
) : ViewModel() {

    // MutableLiveData privado: solo el ViewModel publica cambios; la UI observa el LiveData de solo lectura
    private val _estado = MutableLiveData<EstadoPizzeria>()
    val estado: LiveData<EstadoPizzeria> = _estado

    init {
        // viewModelScope: la corrutina se cancela sola si el ViewModel se destruye
        viewModelScope.launch {
            _estado.value = repositorio.cargarDatosIniciales()
        }
    }
}
