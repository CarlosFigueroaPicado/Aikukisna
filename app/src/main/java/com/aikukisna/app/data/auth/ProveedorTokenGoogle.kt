package com.aikukisna.app.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.aikukisna.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

data class CredencialGoogle(val idToken: String, val nonce: String)

/** Por qué no se obtuvo la cuenta de Google; la pantalla elige el mensaje para el estudiante. */
class ErrorInicioGoogle(val motivo: Motivo, causa: Throwable) : Exception(causa.message, causa) {
    enum class Motivo {
        /** El estudiante cerró el selector de cuentas. */
        CANCELADO,

        /** El teléfono no tiene ninguna cuenta de Google. */
        SIN_CUENTA,

        /** Sin conexión, sin servicios de Google u otro problema del sistema. */
        OTRO
    }
}

class ProveedorTokenGoogle @Inject constructor() {

    suspend fun obtenerCredencial(contextoActividad: Context): CredencialGoogle {
        val nonceOriginal = UUID.randomUUID().toString()
        val nonceHasheado = MessageDigest.getInstance("SHA-256")
            .digest(nonceOriginal.toByteArray())
            .joinToString("") { "%02x".format(it) }

        // Selector "Iniciar sesión con Google" (para un botón). Antes se usaba el panel automático
        // (GetGoogleIdOption): si el estudiante lo cerraba un par de veces, Google lo bloqueaba hasta
        // 24 h con un error "canceled" y la app mostraba "Inicio de sesión cancelado" sin que nadie
        // cancelara. Este selector no tiene ese bloqueo y deja agregar una cuenta si no hay ninguna.
        val opcionGoogle = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setNonce(nonceHasheado)
            .build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(opcionGoogle)
            .build()

        val respuesta = try {
            CredentialManager.create(contextoActividad).getCredential(contextoActividad, solicitud)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialException) {
            // El tipo y el texto exactos quedan en el registro para diagnosticar fallas en teléfonos reales.
            Log.w(ETIQUETA, "getCredential falló: ${e.type} ${e.message}")
            val motivo = when (e) {
                is GetCredentialCancellationException -> ErrorInicioGoogle.Motivo.CANCELADO
                is NoCredentialException -> ErrorInicioGoogle.Motivo.SIN_CUENTA
                else -> ErrorInicioGoogle.Motivo.OTRO
            }
            throw ErrorInicioGoogle(motivo, e)
        }

        val credencial = GoogleIdTokenCredential.createFrom(respuesta.credential.data)
        return CredencialGoogle(idToken = credencial.idToken, nonce = nonceOriginal)
    }

    private companion object {
        const val ETIQUETA = "AikukisnaGoogle"
    }
}
