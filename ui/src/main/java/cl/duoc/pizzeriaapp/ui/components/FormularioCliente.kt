package cl.duoc.pizzeriaapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.pizzeriaapp.utils.esCorreoValido
import cl.duoc.pizzeriaapp.utils.esTelefonoValido

// Formulario para registrar un cliente; valida en vivo con las funciones del módulo :utils
// Teléfono y correo son opcionales: si quedan vacíos, Registro asigna los valores por defecto
@Composable
fun FormularioCliente(
    onRegistrar: (nombre: String, telefono: String, correo: String, direccion: String) -> Unit,
    onCerrar: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }

    val telefonoInvalido = telefono.isNotBlank() && !esTelefonoValido(telefono)
    val correoInvalido = correo.isNotBlank() && !esCorreoValido(correo)
    val formularioValido = nombre.isNotBlank() && !telefonoInvalido && !correoInvalido

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Registrar cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    isError = telefonoInvalido,
                    supportingText = { if (telefonoInvalido) Text("Formato: +56 9 12345678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo") },
                    singleLine = true,
                    isError = correoInvalido,
                    supportingText = { if (correoInvalido) Text("Formato: nombre@dominio.cl") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    supportingText = { Text("Vacía: retiro en local") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onRegistrar(nombre, telefono, correo, direccion)
                    onCerrar()
                },
                enabled = formularioValido
            ) { Text("Registrar") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}
