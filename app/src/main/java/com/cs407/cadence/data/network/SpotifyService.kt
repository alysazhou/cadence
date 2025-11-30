package com.cs407.cadence.data.network

import android.content.Context
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SpotifyService {

    private const val TAG = "SpotifyAppRemoteService"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var connectionParams: ConnectionParams? = null
    private var clientId: String? = null
    private var clientSecret: String? = null
    private var queueManager: BpmTrackQueueManager? = null
    
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

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
            // Stop any existing background processing and clear internal queue
            queueManager?.stop()
            queueManager?.clearQueue()
            
            // Get large pool of tracks for background BPM processing
            val tracks = SpotifyWebApiClient.getTracksByBpmAndGenre(context, genre, targetBpm, bpmRange)
            
            Log.d(TAG, "Web API returned ${tracks.size} tracks for background BPM processing")
            
            if (tracks.isNotEmpty()) {
                // Find first track with matching BPM before playing
                Log.d(TAG, "Checking BPM for first matching track...")
                var firstMatchingTrack: SpotifyTrack? = null
                val minBpm = targetBpm - bpmRange
                val maxBpm = targetBpm + bpmRange
                
                for (track in tracks) {
                    val bpm = RapidApiClient.getTrackBpm(track.id)
                    if (bpm != null && bpm in minBpm..maxBpm) {
                        firstMatchingTrack = track
                        Log.d(TAG, "✓ Found first matching track: '${track.name}' (BPM: $bpm)")
                        break
                    } else if (bpm != null) {
                        Log.d(TAG, "✗ Skipping '${track.name}' (BPM: $bpm, need $minBpm-$maxBpm)")
                    }
                }
                
                if (firstMatchingTrack != null) {
                    // Play the first matching track
                    spotifyAppRemote?.playerApi?.play(firstMatchingTrack.uri)?.setResultCallback {
                        Log.d(TAG, "Successfully started playing BPM-matched track")
                        // Set up listeners for track updates and track end
                        setupPlayerStateSubscription()
                        setupTrackEndListener()
                    }?.setErrorCallback { throwable ->
                        Log.e(TAG, "Error playing track: ${throwable.message}")
                    }
                    
                    // Start background BPM checking for remaining tracks
                    queueManager = BpmTrackQueueManager(context, targetBpm, bpmRange)
                    queueManager?.startProcessing(tracks)
                } else {
                    Log.w(TAG, "No tracks found with matching BPM, falling back to genre station")
                    playGenreStation(genre)
                }
                
            } else {
                Log.w(TAG, "No tracks found, falling back to genre station")
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

    private fun setupPlayerStateSubscription() {
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            val track = playerState.track
            Log.d(TAG, "Player state updated - Track: ${track.name} by ${track.artist.name}")
            _currentTrack.value = track
        }?.setErrorCallback { throwable ->
            Log.e(TAG, "Error subscribing to player state: ${throwable.message}")
        }
    }

    fun queueTrack(trackUri: String) {
        spotifyAppRemote?.playerApi?.queue(trackUri)?.setResultCallback {
            Log.d(TAG, "Track queued: $trackUri")
        }?.setErrorCallback { throwable ->
            Log.e(TAG, "Error queueing track: ${throwable.message}")
        }
    }
    
    private fun setupTrackEndListener() {
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            // When track ends (position reaches duration), play next from internal queue
            if (playerState.isPaused && playerState.track.duration - playerState.playbackPosition < 1000) {
                Log.d(TAG, "Track ended, checking internal queue...")
                val nextTrack = queueManager?.getNextTrack()
                if (nextTrack != null) {
                    Log.d(TAG, "Playing next track from internal queue: '${nextTrack.name}'")
                    spotifyAppRemote?.playerApi?.play(nextTrack.uri)
                } else {
                    Log.d(TAG, "No more tracks in internal queue (${queueManager?.getQueueSize() ?: 0} available)")
                }
            }
        }
    }
    
    fun playNextTrack() {
        val nextTrack = queueManager?.getNextTrack()
        if (nextTrack != null) {
            Log.d(TAG, "Skipping to next track: '${nextTrack.name}'")
            spotifyAppRemote?.playerApi?.play(nextTrack.uri)?.setResultCallback {
                Log.d(TAG, "Successfully skipped to next track")
            }?.setErrorCallback { throwable ->
                Log.e(TAG, "Error skipping to next track: ${throwable.message}")
            }
        } else {
            Log.d(TAG, "No next track available in queue")
        }
    }
    
    fun skipToPosition(positionMs: Long) {
        spotifyAppRemote?.playerApi?.seekTo(positionMs)?.setResultCallback {
            Log.d(TAG, "Skipped to position: ${positionMs}ms")
        }?.setErrorCallback { throwable ->
            Log.e(TAG, "Error seeking to position: ${throwable.message}")
        }
    }
    
    fun stopBpmProcessing() {
        queueManager?.stop()
        queueManager = null
    }

    fun disconnect() {
        stopBpmProcessing()
        spotifyAppRemote?.let {
            if (it.isConnected) {
                Log.d(TAG, "Disconnecting.")
                SpotifyAppRemote.disconnect(it)
                spotifyAppRemote = null
            }
        }
    }
}
