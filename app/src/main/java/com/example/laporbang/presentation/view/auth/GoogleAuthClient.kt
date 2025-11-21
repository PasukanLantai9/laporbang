package com.example.laporbang.presentation.view.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.laporbang.R
import com.example.laporbang.data.model.GoogleSignInResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(
    private val context: Context
) {
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

    suspend fun signIn(): GoogleSignInResult {
        return try {
            val signInResult = oneTapClient.beginSignIn(
                BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(context.getString(R.string.web_client_id))
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .setAutoSelectEnabled(false)
                    .build()
            ).await()

            GoogleSignInResult.Success(signInResult.pendingIntent.intentSender)

        } catch (e: Exception) {
            e.printStackTrace()
            GoogleSignInResult.Error(e.message ?: "Unknown Error")
        }
    }

    suspend fun getSignInCredentialFromIntent(intent: Intent): String? {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            credential.googleIdToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun signOut() {
        oneTapClient.signOut()
    }
}

