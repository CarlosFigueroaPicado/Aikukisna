package com.aikukisna.app.pantallas.navegacion

sealed class Pantalla(val ruta: String) {
    object Login : Pantalla("login")
    object Main : Pantalla("main")
    object Register : Pantalla("register")
}

sealed class TabItem(val ruta: String, val titulo: String) {
    object Inicio : TabItem("inicio", "Inicio")
    object Lecciones : TabItem("lecciones", "Lecciones")
    object Capsulas : TabItem("capsulas", "Cápsulas")
    object Marcadores : TabItem("marcadores", "Marcadores")
    object Perfil : TabItem("perfil", "Perfil")
}