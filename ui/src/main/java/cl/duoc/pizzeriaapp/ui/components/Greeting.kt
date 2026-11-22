package cl.duoc.pizzeriaapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.pizzeriaapp.ui.theme.PizzeriaAppTheme

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Text(
        text = "Bienvenidos a la mejor pizzería de la ciudad",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PizzeriaAppTheme {
        Greeting()
    }
}
