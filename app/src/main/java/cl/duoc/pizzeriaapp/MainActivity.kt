package cl.duoc.pizzeriaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cl.duoc.pizzeriaapp.ui.screens.PizzeriaScreen
import cl.duoc.pizzeriaapp.ui.theme.PizzeriaAppTheme
import cl.duoc.pizzeriaapp.ui.viewmodel.PizzeriaViewModel

class MainActivity : ComponentActivity() {

    // by viewModels(): el sistema crea el ViewModel una vez y lo conserva al rotar la pantalla
    private val viewModel: PizzeriaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PizzeriaAppTheme {
                // observeAsState: cada vez que el LiveData publica un estado nuevo, la pantalla se recompone
                val estado by viewModel.estado.observeAsState()
                val mensaje by viewModel.mensaje.observeAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                // Muestra el resultado de cada operación (cliente registrado, pedido creado o rechazado)
                LaunchedEffect(mensaje) {
                    mensaje?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.mensajeMostrado()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    // Conexión lógica-UI: los datos del ViewModel llegan a la pantalla Compose del módulo :ui
                    // y las acciones de los formularios vuelven al ViewModel
                    PizzeriaScreen(
                        estado = estado,
                        modifier = Modifier.padding(innerPadding),
                        onRegistrarCliente = viewModel::registrarCliente,
                        onCrearPedido = viewModel::crearPedido
                    )
                }
            }
        }
    }
}
