package cl.duoc.pizzeriaapp.annotations

import cl.duoc.pizzeriaapp.model.Producto

// Reflection: lee la anotación @Promocionable de la clase del producto en tiempo de ejecución
// Vive junto a @Promocionable (módulo data) porque conoce el modelo; así :utils no depende del dominio
// Se usa la API de reflection de Java (getAnnotation), sin agregar la librería kotlin-reflect
fun obtenerPromocion(producto: Producto): Promocionable? =
    producto.javaClass.getAnnotation(Promocionable::class.java)

fun esPromocionable(producto: Producto): Boolean = obtenerPromocion(producto) != null
