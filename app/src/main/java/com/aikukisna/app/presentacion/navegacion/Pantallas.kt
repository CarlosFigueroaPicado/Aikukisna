package com.aikukisna.app.pantallas.navegacion

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

sealed class Pantalla(val ruta: String) {
    object Login : Pantalla("login")
    object Main : Pantalla("main")
    object Register : Pantalla("register")
}

sealed class TabItem(val ruta: String, private val tituloId: Int) {
    val titulo: String get() = t(tituloId)

    object Inicio : TabItem("inicio", R.string.pantallas_inicio)
    object Lecciones : TabItem("lecciones", R.string.pantallas_lecciones)
    object Capsulas : TabItem("capsulas", R.string.pantallas_capsulas)
    object Marcadores : TabItem("marcadores", R.string.pantallas_marcadores)
    object Perfil : TabItem("perfil", R.string.pantallas_perfil)
}