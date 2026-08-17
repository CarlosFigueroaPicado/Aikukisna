package com.aikukisna.app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.aikukisna.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

data class CredencialGoogle(val idToken: String, val nonce: String)

class ProveedorTokenGoogle @Inject constructor() {

    suspend fun obtenerCredencial(contextoActividad: Context): CredencialGoogle {
        val nonceOriginal = UUID.randomUUID().toString()
        val nonceHasheado = MessageDigest.getInstance("SHA-256")
            .digest(nonceOriginal.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val opcionGoogle = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .setNonce(nonceHasheado)
            .build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(opcionGoogle)
            .build()

        val respuesta = CredentialManager.create(contextoActividad)
            .getCredential(contextoActividad, solicitud)

        val credencial = GoogleIdTokenCredential.createFrom(respuesta.credential.data)
        return CredencialGoogle(idToken = credencial.idToken, nonce = nonceOriginal)
    }
}