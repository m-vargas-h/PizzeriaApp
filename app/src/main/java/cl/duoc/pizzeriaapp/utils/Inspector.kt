package cl.duoc.pizzeriaapp.utils

import cl.duoc.pizzeriaapp.annotations.Promocionable
import cl.duoc.pizzeriaapp.model.Producto

// Reflection: lee la anotación @Promocionable de la clase del producto en tiempo de ejecución
// En Android se usa la API de reflection de Java (getAnnotation), que viene incluida en la plataforma,
// en lugar de kotlin.reflect.full, que requiere agregar la librería kotlin-reflect
fun obtenerPromocion(producto: Producto): Promocionable? =
    producto.javaClass.getAnnotation(Promocionable::class.java)

fun esPromocionable(producto: Producto): Boolean = obtenerPromocion(producto) != null
