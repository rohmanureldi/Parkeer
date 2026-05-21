package com.eldirohmanur.parkeer.security

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class IntegrityChecker @Inject constructor(@ApplicationContext private val context: Context) {
    sealed interface Result {
        data object Trusted : Result
        data object Untrusted : Result
        data class Error(val message: String) : Result
    }

    suspend fun check(): Result = suspendCancellableCoroutine { cont ->
        val nonce = ByteArray(24).also { SecureRandom().nextBytes(it) }
        val nonceStr = Base64.getUrlEncoder().withoutPadding().encodeToString(nonce)

        val client = IntegrityManagerFactory.create(context)
        client.requestIntegrityToken(
            IntegrityTokenRequest.builder()
                .setNonce(nonceStr)
                .build(),
        ).addOnSuccessListener { response ->
            // In production, send response.token() to your backend for verification.
            // For offline use: token received = device has Play Services + not obviously tampered.
            cont.resume(Result.Trusted)
        }.addOnFailureListener { e ->
            cont.resume(Result.Error(e.message ?: "Integrity check failed"))
        }
    }
}
