package com.cs407.cadence.data.network

import android.content.Context
import android.util.Base64
import android.util.Log
import com.cs407.cadence.data.SpotifyAuthState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SpotifyWebApiClient {
    private const val TAG = "SpotifyWebApiClient"
    private const val BASE_URL = "https://api.spotify.com/"
    private const val ACCOUNTS_BASE_URL = "https://accounts.spotify.com/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val accountsRetrofit = Retrofit.Builder()
        .baseUrl(ACCOUNTS_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val api: SpotifyWebApi = retrofit.create(SpotifyWebApi::class.java)
    private val accountsApi: SpotifyWebApi = accountsRetrofit.create(SpotifyWebApi::class.java)
    
    // get access token from oauth
    private fun getAccessToken(context: Context): String? {
        val token = SpotifyAuthState.getAccessToken(context)
        if (token != null) {
            Log.d(TAG, "Using OAuth token: ${token.take(20)}...")
        } else {
            Log.e(TAG, "No OAuth token found - user needs to authenticate")
        }
        return token
    }
    
    // validate if access token is working
    suspend fun validateToken(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken(context) ?: return@withContext false
                val response = api.getCurrentUser(authorization = "Bearer $accessToken")
                val isValid = response.isSuccessful
                if (!isValid) {
                    Log.e(TAG, "Token validation failed: ${response.code()}")
                    SpotifyAuthState.clearAuth(context)
                }
                isValid
            } catch (e: Exception) {
                Log.e(TAG, "Error validating token: ${e.message}", e)
                false
            }
        }
    }
    
    // map app genres to spotify seed genres
    private fun mapToSpotifyGenre(genre: String): String {
        return when (genre.lowercase()) {
            "electronic" -> "edm"
            "hip-hop" -> "hip-hop"
            "pop" -> "pop"
            "rock" -> "rock"
            "metal" -> "metal"
            "indie" -> "indie"
            else -> "edm" // default fallback
        }
    }
    
    // get tracks filtered by bpm range and genre
    suspend fun getTracksByBpmAndGenre(
        context: Context,
        genre: String,
        targetBpm: Int,
        bpmRange: Int = 10
    ): List<SpotifyTrack> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken(context)
                if (accessToken == null) {
                    Log.e(TAG, "Not authenticated with OAuth")
                    return@withContext emptyList()
                }
                
                val spotifyGenre = mapToSpotifyGenre(genre)
                
                // Fetch multiple batches for large track pool (for background BPM processing)
                val allTracks = mutableListOf<SpotifyTrack>()
                val seenTrackIds = mutableSetOf<String>()
                
                // Randomize offsets to get different tracks each time
                val randomOffsets = listOf(0, 50, 100, 150, 200, 250).shuffled().take(3)
                
                // Fetch 3 random batches of 50 = 150 tracks total
                for (offset in randomOffsets) {
                    try {
                        val batch = searchTracksByGenre(context, genre, 50, offset)
                        batch.forEach { track ->
                            if (track.id !in seenTrackIds) {
                                seenTrackIds.add(track.id)
                                allTracks.add(track)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching batch at offset $offset: ${e.message}")
                    }
                }
                
                Log.d(TAG, "Fetched ${allTracks.size} unique tracks for background BPM processing")
                
                // Shuffle tracks to randomize order
                allTracks.shuffle()
                
                // Filter out compilation albums and misclassified tracks
                val filteredTracks = allTracks.filter { track ->
                    !isLikelyMisclassified(track, genre)
                }
                
                Log.d(TAG, "Filtered to ${filteredTracks.size} tracks after removing compilations/misclassified")
                
                // Return shuffled and filtered tracks - BPM filtering will happen in background
                return@withContext filteredTracks
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting tracks: ${e.message}", e)
                emptyList()
            }
        }
    }
    
    private fun isLikelyMisclassified(track: SpotifyTrack, requestedGenre: String): Boolean {
        val albumName = track.album.name.lowercase()
        val trackName = track.name.lowercase()
        val artistName = track.artists.firstOrNull()?.name?.lowercase() ?: ""
        
        // Filter out compilation albums
        if (albumName.contains("compilation") || 
            albumName.contains("various artists") ||
            albumName.contains("mixed by") ||
            albumName.contains("dj mix")) {
            return true
        }
        
        // Filter out tracks with suspicious indicators
        val suspiciousKeywords = listOf(
            "chiptune", "8-bit", "remix compilation", "megamix"
        )
        
        if (suspiciousKeywords.any { trackName.contains(it) || albumName.contains(it) }) {
            return true
        }
        
        // Genre-specific filtering
        when (requestedGenre.lowercase()) {
            "reggae" -> {
                // Filter out electronic/dance tracks misclassified as reggae
                val electronicKeywords = listOf("edm", "electro", "dubstep", "dnb", "drum and bass", "techno")
                if (electronicKeywords.any { artistName.contains(it) || trackName.contains(it) }) {
                    return true
                }
            }
        }
        
        return false
    }
    
    // simple genre-based search fallback
    private suspend fun searchTracksByGenre(
        context: Context, 
        genre: String, 
        limit: Int = 50, 
        offset: Int = 0
    ): List<SpotifyTrack> {
        try {
            val accessToken = getAccessToken(context) ?: return emptyList()
            val spotifyGenre = mapToSpotifyGenre(genre)
            
            // Vary the year range to get different tracks each time
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val yearRanges = listOf(
                "year:${currentYear-2}-${currentYear}",      // Very recent
                "year:${currentYear-5}-${currentYear-2}",    // Recent
                "year:${currentYear-10}-${currentYear-5}"    // Older classics
            )
            val yearRange = yearRanges.random()
            
            val searchQuery = "genre:$spotifyGenre $yearRange"
            
            Log.d(TAG, "Searching: $searchQuery (offset: $offset)")
            
            val searchResponse = api.searchTracks(
                query = searchQuery,
                limit = limit,
                offset = offset,
                authorization = "Bearer $accessToken"
            )
            
            if (searchResponse.isSuccessful && searchResponse.body() != null) {
                return searchResponse.body()!!.tracks.items
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in genre search: ${e.message}")
        }
        return emptyList()
    }
    
    // old implementation
    private suspend fun getTracksByBpmAndGenreOld(
        context: Context,
        genre: String,
        targetBpm: Int,
        bpmRange: Int = 10
    ): List<SpotifyTrack> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken(context)
                if (accessToken == null) {
                    Log.e(TAG, "Not authenticated with OAuth")
                    return@withContext emptyList()
                }
                
                val minBpm = targetBpm - bpmRange
                val maxBpm = targetBpm + bpmRange
                val spotifyGenre = mapToSpotifyGenre(genre)
                
                // old failing api approaches removed
                return@withContext emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error in old implementation: ${e.message}", e)
                emptyList()
            }
        }
    }
    
    // get audio features for track ids
    suspend fun getAudioFeatures(context: Context, trackIds: List<String>): List<SpotifyAudioFeatures> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken(context)
                if (accessToken == null) {
                    Log.e(TAG, "Not authenticated with OAuth")
                    return@withContext emptyList()
                }
                
                val ids = trackIds.joinToString(",")
                val response = api.getAudioFeatures(
                    ids = ids,
                    authorization = "Bearer $accessToken"
                )
                
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.audio_features.filterNotNull()
                } else {
                    Log.e(TAG, "Failed to get audio features: ${response.code()}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting audio features: ${e.message}", e)
                emptyList()
            }
        }
    }
}
