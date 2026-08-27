package com.aikukisna.app.domain.model

enum class RolChat { USUARIO, TUKI }

data class MensajeChat(
    val rol: RolChat,
    val texto: String
)