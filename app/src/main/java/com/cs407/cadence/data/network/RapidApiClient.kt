package com.cs407.cadence.data.network

import android.util.Log
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// Response model based on API documentation
data class RapidApiResponse(
    val id: String?,
    val key: String?,
    val mode: String?,
    val camelot: String?,
    val tempo: Int?,  // BPM as integer
    val duration: String?,
    val popularity: Int?,
    val energy: Int?,
    val danceability: Int?,
    val happiness: Int?,
    val acousticness: Int?,
    val instrumentalness: Int?,
    val liveness: Int?,
    val speechiness: Int?,
    val loudness: String?
)

// Retrofit API interface using Spotify ID endpoint
interface RapidApiService {
    @GET("pktx/spotify/{spotifyId}")
    suspend fun getTrackAnalysis(
        @Path("spotifyId") spotifyId: String,
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") apiHost: String = "track-analysis.p.rapidapi.com"
    ): Response<RapidApiResponse>
}

object RapidApiClient {
    private const val TAG = "RapidApiClient"
    private const val BASE_URL = "https://track-analysis.p.rapidapi.com/"
    private const val RATE_LIMIT_DELAY_MS = 1000L // 1 request per second
    
    private var apiKey: String = ""
    private var lastRequestTime = 0L
    
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
    
    private val api: RapidApiService = retrofit.create(RapidApiService::class.java)
    
    fun setApiKey(key: String) {
        apiKey = key
        Log.d(TAG, "API key configured")
    }
    
    // Rate limiting to respect API limits
    private suspend fun respectRateLimit() {
        val now = System.currentTimeMillis()
        val timeSinceLastRequest = now - lastRequestTime
        if (timeSinceLastRequest < RATE_LIMIT_DELAY_MS) {
            val waitTime = RATE_LIMIT_DELAY_MS - timeSinceLastRequest
            Log.d(TAG, "Rate limiting: waiting ${waitTime}ms")
            delay(waitTime)
        }
        lastRequestTime = System.currentTimeMillis()
    }
    
    // Get BPM for a single track using Spotify ID
    suspend fun getTrackBpm(spotifyId: String): Int? {
        if (apiKey.isEmpty()) {
            Log.e(TAG, "API key not set")
            return null
        }
        
        return try {
            respectRateLimit()
            
            Log.d(TAG, "Fetching BPM for Spotify track: $spotifyId")
            val response = api.getTrackAnalysis(spotifyId, apiKey)
            
            Log.d(TAG, "Response code: ${response.code()}, message: ${response.message()}")
            Log.d(TAG, "Response body: ${response.body()}")
            Log.d(TAG, "Error body: ${response.errorBody()?.string()}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val tempo = body.tempo
                if (tempo != null && tempo > 0) {
                    Log.d(TAG, "✓ Found BPM: $tempo for track $spotifyId")
                    tempo
                } else {
                    Log.w(TAG, "⊘ No tempo data in response for track $spotifyId")
                    null
                }
            } else {
                Log.e(TAG, "API request failed: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching BPM for track $spotifyId: ${e.message}", e)
            null
        }
    }
    
    // Batch get BPMs for multiple tracks
    suspend fun getTrackBpms(spotifyIds: List<String>): Map<String, Int> {
        val results = mutableMapOf<String, Int>()
        
        for (id in spotifyIds) {
            val bpm = getTrackBpm(id)
            if (bpm != null) {
                results[id] = bpm
            }
        }
        
        Log.d(TAG, "Fetched BPM data for ${results.size}/${spotifyIds.size} tracks")
        return results
    }
}
