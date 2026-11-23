package cl.duoc.pizzeriaapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.pizzeriaapp.model.Cliente
import cl.duoc.pizzeriaapp.model.ItemPedido
import cl.duoc.pizzeriaapp.model.Producto
import cl.duoc.pizzeriaapp.utils.formatearPesos

// Formulario para crear un pedido de un producto: cliente y producto se eligen de las listas registradas
@Composable
fun FormularioPedido(
    clientes: List<Cliente>,
    productos: List<Producto>,
    onCrear: (cliente: Cliente, producto: Producto, cantidad: Int, notas: String) -> Unit,
    onCerrar: () -> Unit
) {
    var cliente by remember { mutableStateOf<Cliente?>(null) }
    var producto by remember { mutableStateOf<Producto?>(null) }
    var cantidadTexto by rememberSaveable { mutableStateOf("1") }
    var notas by rememberSaveable { mutableStateOf("") }

    // Mismo rango que valida ItemPedido en el modelo
    val cantidad = cantidadTexto.toIntOrNull()
    val cantidadValida = cantidad != null && cantidad in ItemPedido.RANGO_CANTIDAD
    val clienteElegido = cliente
    val productoElegido = producto

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nuevo pedido") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Selector(
                    etiqueta = "Cliente",
                    seleccionado = cliente,
                    opciones = clientes,
                    texto = { it.nombre },
                    onSeleccionar = { cliente = it }
                )
                Selector(
                    etiqueta = "Producto",
                    seleccionado = producto,
                    opciones = productos,
                    texto = { "${it.nombre} - ${formatearPesos(it.precio)}" },
                    onSeleccionar = { producto = it }
                )
                OutlinedTextField(
                    value = cantidadTexto,
                    onValueChange = { cantidadTexto = it },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    isError = !cantidadValida,
                    supportingText = {
                        if (!cantidadValida) {
                            Text("Entre ${ItemPedido.RANGO_CANTIDAD.first} y ${ItemPedido.RANGO_CANTIDAD.last}")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas (opcional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (clienteElegido != null && productoElegido != null && cantidad != null) {
                        onCrear(clienteElegido, productoElegido, cantidad, notas)
                        onCerrar()
                    }
                },
                enabled = clienteElegido != null && productoElegido != null && cantidadValida
            ) { Text("Crear pedido") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}

// Lista desplegable genérica: un botón muestra la opción elegida y abre el menú
@Composable
private fun <T> Selector(
    etiqueta: String,
    seleccionado: T?,
    opciones: List<T>,
    texto: (T) -> String,
    onSeleccionar: (T) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    Column {
        Text(text = etiqueta, style = MaterialTheme.typography.labelMedium)
        Box {
            OutlinedButton(onClick = { expandido = true }, modifier = Modifier.fillMaxWidth()) {
                Text(seleccionado?.let(texto) ?: "Seleccionar")
            }
            DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(texto(opcion)) },
                        onClick = {
                            onSeleccionar(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}
