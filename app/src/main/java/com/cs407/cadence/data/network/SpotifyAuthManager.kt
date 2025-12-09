package com.cs407.cadence.data.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class SpotifyAuthManager(private val context: Context) {
    companion object {
        const val CLIENT_ID = com.cs407.cadence.BuildConfig.SPOTIFY_CLIENT_ID
        const val REDIRECT_URI = "com.cs407.cadence://callback"
        const val AUTH_ENDPOINT = "https://accounts.spotify.com/authorize"
        const val TOKEN_ENDPOINT = "https://accounts.spotify.com/api/token"
        const val SCOPES =
                "user-read-private user-read-email user-library-read user-top-read user-read-playback-state user-modify-playback-state"

        fun isConfigured(): Boolean {
            return CLIENT_ID.isNotEmpty() && CLIENT_ID != "null"
        }
    }

    private val serviceConfig =
            AuthorizationServiceConfiguration(Uri.parse(AUTH_ENDPOINT), Uri.parse(TOKEN_ENDPOINT))
    private val authService = AuthorizationService(context)
    var authState: AuthState? = null

    fun getAuthRequest(): AuthorizationRequest {
        if (!isConfigured()) {
            throw IllegalStateException(
                    "Spotify Client ID not configured. Please add SPOTIFY_CLIENT_ID to local.properties"
            )
        }
        return AuthorizationRequest.Builder(
                        serviceConfig,
                        CLIENT_ID,
                        ResponseTypeValues.CODE,
                        Uri.parse(REDIRECT_URI)
                )
                .setScopes(SCOPES.split(" "))
                .build()
    }

    fun startAuth(activity: Activity, requestCode: Int = 1001) {
        val authRequest = getAuthRequest()
        val intent = authService.getAuthorizationRequestIntent(authRequest)
        activity.startActivityForResult(intent, requestCode)
    }

    fun handleAuthResponse(
            intent: Intent,
            onSuccess: (accessToken: String) -> Unit,
            onError: (String) -> Unit
    ) {
        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        if (resp != null) {
            val tokenRequest = resp.createTokenExchangeRequest()
            authService.performTokenRequest(tokenRequest) { response, exception ->
                if (response != null) {
                    authState = AuthState(serviceConfig)
                    authState?.update(response, exception)
                    val accessToken = response.accessToken
                    if (accessToken != null) {
                        onSuccess(accessToken)
                    } else {
                        onError("No access token returned")
                    }
                } else {
                    onError(exception?.errorDescription ?: "Unknown error")
                }
            }
        } else {
            onError(ex?.errorDescription ?: "Authorization failed")
        }
    }
}
