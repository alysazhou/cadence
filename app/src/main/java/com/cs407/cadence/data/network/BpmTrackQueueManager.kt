package com.cs407.cadence.data.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages background BPM checking and track queueing Processes tracks one per second to avoid rate
 * limiting Maintains internal queue that can be cleared on new workout
 */
class BpmTrackQueueManager(
        private val context: Context,
        private val targetBpm: Int,
        private val bpmRange: Int
) {
    private val TAG = "BpmTrackQueueManager"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var processingJob: Job? = null
    private val pendingTracks = mutableListOf<SpotifyTrack>()
    private val internalQueue =
            mutableListOf<SpotifyTrack>() // Internal queue of BPM-matched tracks
    private val minBpm = targetBpm - bpmRange
    private val maxBpm = targetBpm + bpmRange

    fun startProcessing(tracks: List<SpotifyTrack>) {
        if (tracks.isEmpty()) {
            Log.w(TAG, "No tracks to process")
            return
        }

        pendingTracks.clear()
        pendingTracks.addAll(tracks)

        Log.d(
                TAG,
                "Starting background BPM processing for ${tracks.size} tracks (target: $targetBpm±$bpmRange BPM)"
        )

        processingJob?.cancel()
        processingJob =
                scope.launch {
                    while (isActive && pendingTracks.isNotEmpty()) {
                        val track = pendingTracks.removeAt(0)
                        processTrack(track)
                    }
                    Log.d(TAG, "Background BPM processing complete")
                }
    }

    private suspend fun processTrack(track: SpotifyTrack) {
        try {
            val bpm = RapidApiClient.getTrackBpm(track.id)

            if (bpm != null) {
                // Cache BPM in SpotifyService for later use
                SpotifyService.updateTrackBpmCache(track.id, bpm)

                if (bpm in minBpm..maxBpm) {
                    Log.d(
                            TAG,
                            "✓ Match found: '${track.name}' (BPM: $bpm) - adding to internal queue"
                    )
                    internalQueue.add(track)
                } else {
                    Log.d(TAG, "✗ Out of range: '${track.name}' (BPM: $bpm, need $minBpm-$maxBpm)")
                }
            } else {
                Log.d(TAG, "⊘ No BPM data for: '${track.name}'")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing track '${track.name}': ${e.message}")
        }
    }

    fun addTracks(tracks: List<SpotifyTrack>) {
        pendingTracks.addAll(tracks)
        Log.d(TAG, "Added ${tracks.size} tracks to queue. Total pending: ${pendingTracks.size}")

        // Start processing if not already running
        if (processingJob?.isActive != true && pendingTracks.isNotEmpty()) {
            startProcessing(pendingTracks.toList())
        }
    }

    fun stop() {
        Log.d(TAG, "Stopping background BPM processing")
        processingJob?.cancel()
        processingJob = null
        pendingTracks.clear()
    }

    fun clearQueue() {
        Log.d(TAG, "Clearing internal queue (${internalQueue.size} tracks)")
        internalQueue.clear()
    }

    fun getNextTrack(): SpotifyTrack? {
        return if (internalQueue.isNotEmpty()) {
            val track = internalQueue.removeAt(0)
            Log.d(TAG, "Dequeuing track: '${track.name}' (${internalQueue.size} remaining)")
            track
        } else {
            Log.d(TAG, "Internal queue is empty")
            null
        }
    }

    fun getQueueSize(): Int = internalQueue.size

    fun isProcessing(): Boolean = processingJob?.isActive == true
}
