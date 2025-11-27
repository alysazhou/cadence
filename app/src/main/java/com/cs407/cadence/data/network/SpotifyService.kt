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
    private var clientId: String? = null
    private var clientSecret: String? = null

    fun buildConnectionParams(clientId: String, redirectUri: String, clientSecret: String = "") {
        this.clientId = clientId
        this.clientSecret = clientSecret
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

    suspend fun playRecommendedTracks(context: Context, genre: String, targetBpm: Int, bpmRange: Int = 10) {
        if (spotifyAppRemote == null || spotifyAppRemote?.isConnected == false) {
            Log.e(TAG, "not connected to spotify")
            return
        }

        Log.d(TAG, "Fetching tracks for genre '$genre' with target BPM '$targetBpm'")

        try {
            // Get BPM-filtered tracks from Web API using OAuth
            val tracks = SpotifyWebApiClient.getTracksByBpmAndGenre(context, genre, targetBpm, bpmRange)
            
            Log.d(TAG, "Web API returned ${tracks.size} tracks for genre '$genre' with BPM ${targetBpm}±${bpmRange}")
            
            if (tracks.isNotEmpty()) {
                // log first few tracks
                tracks.take(5).forEachIndexed { index, track ->
                    Log.d(TAG, "Track ${index + 1}: '${track.name}' by ${track.artists.firstOrNull()?.name ?: "unknown"}")
                }
                
                // play first track
                val trackUri = tracks.first().uri
                Log.d(TAG, "Playing BPM-filtered track: '${tracks.first().name}' by ${tracks.first().artists.firstOrNull()?.name ?: "unknown"}")
                
                spotifyAppRemote?.playerApi?.play(trackUri)?.setResultCallback {
                    Log.d(TAG, "Successfully started playing BPM-filtered track")
                }?.setErrorCallback { throwable ->
                    Log.e(TAG, "Error playing track: ${throwable.message}")
                }
                
                // queue remaining tracks
                tracks.drop(1).take(19).forEach { track ->
                    Log.d(TAG, "Queueing track: '${track.name}'")
                    spotifyAppRemote?.playerApi?.queue(track.uri)
                }
            } else {
                Log.w(TAG, "No tracks found with BPM criteria (${targetBpm}±${bpmRange}), falling back to genre station")
                playGenreStation(genre)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in playRecommendedTracks: ${e.message}", e)
            playGenreStation(genre)
        }
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

    fun resume() {
        spotifyAppRemote?.playerApi?.resume()?.setResultCallback {
            Log.d(TAG, "Playback resumed.")
        }?.setErrorCallback { throwable ->
            Log.e(TAG, "Error resuming playback: ${throwable.message}")
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
