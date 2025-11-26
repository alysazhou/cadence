package com.cs407.cadence.data.network

import android.content.Context
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track

object SpotifyService {

    private const val TAG = "SpotifyAppRemoteService"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var connectionParams: ConnectionParams? = null

    fun buildConnectionParams(clientId: String, redirectUri: String) {
        connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()
    }

    fun connect(context: Context, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        if (connectionParams == null) {
            onFailure(IllegalStateException("ConnectionParams must be built before connecting."))
            return
        }

        if (spotifyAppRemote?.isConnected == true) {
            Log.d(TAG, "Already connected.")
            onSuccess()
            return
        }

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d(TAG, "successful connection")
                onSuccess()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "connection failed: ${throwable.message}", throwable)
                onFailure(throwable)
            }
        })
    }

    fun playRecommendedTracks(genre: String, targetBpm: Int) {
        if (spotifyAppRemote == null || spotifyAppRemote?.isConnected == false) {
            Log.e(TAG, "not connected to spotify")
            return
        }

        Log.d(TAG, "genre '$genre' with target BPM '$targetBpm'")
        Log.d(TAG, "placeholder: playing genre instead")

        // TODO: INTEGRATE WEB API
        playGenreStation(genre)
    }

    fun playGenreStation(genre: String) {
        val playlistUri = "spotify:station:genre:${genre.lowercase()}"
        spotifyAppRemote?.playerApi?.play(playlistUri)?.setResultCallback {
            Log.d(TAG, "Playing genre station: $genre")
        }?.setErrorCallback { throwable ->
            Log.e(TAG, "Error playing genre station $genre: ${throwable.message}")
        }
    }

    fun pause() {
        spotifyAppRemote?.playerApi?.pause()?.setResultCallback {
            Log.d(TAG, "Playback paused.")
        }?.setErrorCallback { throwable ->
            Log.e(TAG, "Error pausing playback: ${throwable.message}")
        }
    }

    fun subscribeToPlayerState(callback: (Track) -> Unit) {
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            callback(playerState.track)
        }
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            if (it.isConnected) {
                Log.d(TAG, "Disconnecting.")
                SpotifyAppRemote.disconnect(it)
                spotifyAppRemote = null
            }
        }
    }
}
