package com.cs407.cadence.data.network

import android.util.Log
import com.cs407.cadence.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AcoustIdClient {
    private const val TAG = "AcoustIdClient"
    private const val ACOUSTID_BASE_URL = "https://api.acoustid.org/"
    private const val MUSICBRAINZ_BASE_URL = "https://musicbrainz.org/ws/2/"
    private const val THEAUDIODB_BASE_URL = "https://www.theaudiodb.com/api/v1/json/2/"
    
    // api keys
    private val ACOUSTID_API_KEY = BuildConfig.ACOUSTID_API_KEY
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val musicBrainzClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "Cadence/1.0 (https://github.com/alysazhou/cadence)")
                .build()
            chain.proceed(request)
        }
        .build()
    
    private val acoustIdRetrofit = Retrofit.Builder()
        .baseUrl(ACOUSTID_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val musicBrainzRetrofit = Retrofit.Builder()
        .baseUrl(MUSICBRAINZ_BASE_URL)
        .client(musicBrainzClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val theAudioDbRetrofit = Retrofit.Builder()
        .baseUrl(THEAUDIODB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val acoustIdApi: AcoustIdApi = acoustIdRetrofit.create(AcoustIdApi::class.java)
    private val musicBrainzApi: MusicBrainzApi = musicBrainzRetrofit.create(MusicBrainzApi::class.java)
    private val theAudioDbApi: TheAudioDbApi = theAudioDbRetrofit.create(TheAudioDbApi::class.java)
    
    // get bpm using theaudiodb api with musicbrainz fallback
    suspend fun getTrackBpm(trackName: String, artistName: String): Int? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Looking up BPM for '$trackName' by '$artistName'")
                
                // try theaudiodb api first
                val response = theAudioDbApi.searchTrack(
                    artist = artistName,
                    track = trackName
                )
                
                Log.d(TAG, "TheAudioDB response code: ${response.code()}, successful: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body() != null) {
                    val tracks = response.body()?.track
                    Log.d(TAG, "TheAudioDB returned ${tracks?.size ?: 0} tracks")
                    
                    if (!tracks.isNullOrEmpty()) {
                        val firstTrack = tracks.firstOrNull()
                        Log.d(TAG, "Track data: ${firstTrack?.strTrack}, BPM field: ${firstTrack?.intBPM}")
                        
                        val bpm = firstTrack?.intBPM?.toIntOrNull()
                        if (bpm != null && bpm > 0) {
                            Log.d(TAG, "✓ Found BPM: $bpm for '$trackName' via TheAudioDB")
                            return@withContext bpm
                        }
                    }
                } else {
                    Log.e(TAG, "TheAudioDB API error: ${response.code()} - ${response.message()}")
                }
                
                Log.d(TAG, "No BPM found via TheAudioDB, trying MusicBrainz...")
                
                // fallback to musicbrainz
                val searchQuery = "artist:\"$artistName\" AND recording:\"$trackName\""
                val searchResponse = musicBrainzApi.searchRecordings(
                    query = searchQuery,
                    limit = 1
                )
                
                if (!searchResponse.isSuccessful || searchResponse.body() == null) {
                    return@withContext null
                }
                
                val recordings = searchResponse.body()?.recordings
                if (recordings.isNullOrEmpty()) {
                    return@withContext null
                }
                
                val recordingId = recordings.firstOrNull()?.id ?: return@withContext null
                Log.d(TAG, "Found MusicBrainz recording ID: $recordingId")
                
                val musicBrainzResponse = musicBrainzApi.getRecording(recordingId)
                
                if (!musicBrainzResponse.isSuccessful || musicBrainzResponse.body() == null) {
                    return@withContext null
                }
                
                val tags = musicBrainzResponse.body()?.tags
                val bpmTag = tags?.firstOrNull { tag ->
                    tag.name.contains("bpm", ignoreCase = true) ||
                    tag.name.matches(Regex("\\d{2,3}"))
                }
                
                val bpm = bpmTag?.name?.filter { it.isDigit() }?.toIntOrNull()
                
                if (bpm != null) {
                    Log.d(TAG, "✓ Found BPM: $bpm for '$trackName' via MusicBrainz")
                }
                
                return@withContext bpm
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting BPM: ${e.message}", e)
                null
            }
        }
    }
    
    // filter spotify tracks by bpm, continue checking until enough matches
    suspend fun filterTracksByBpm(
        tracks: List<SpotifyTrack>,
        targetBpm: Int,
        bpmTolerance: Int = 10
    ): List<SpotifyTrack> {
        return withContext(Dispatchers.IO) {
            val filteredTracks = mutableListOf<SpotifyTrack>()
            val minBpm = targetBpm - bpmTolerance
            val maxBpm = targetBpm + bpmTolerance
            val targetTrackCount = 20
            
            var tracksChecked = 0
            var tracksProcessed = 0
            
            // keep checking tracks until enough matches or run out
            for (track in tracks) {
                if (filteredTracks.size >= targetTrackCount) {
                    break
                }
                
                try {
                    tracksProcessed++
                    val artistName = track.artists.firstOrNull()?.name ?: continue
                    val bpm = getTrackBpm(track.name, artistName)
                    
                    tracksChecked++
                    
                    if (bpm == null) {
                        Log.d(TAG, "⊘ Track '${track.name}' has no BPM data, skipping")
                        continue // skip and check next track
                    }
                    
                    if (bpm in minBpm..maxBpm) {
                        Log.d(TAG, "✓ Track '${track.name}' matches BPM filter ($bpm BPM)")
                        filteredTracks.add(track)
                    } else {
                        Log.d(TAG, "✗ Track '${track.name}' outside range ($bpm BPM, need $minBpm-$maxBpm)")
                    }
                    
                    kotlinx.coroutines.delay(100)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking track ${track.name}: ${e.message}")
                }
            }
            
            Log.d(TAG, "Found ${filteredTracks.size} matching tracks after checking $tracksChecked/$tracksProcessed tracks")
            
            filteredTracks
        }
    }
}
