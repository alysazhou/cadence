package com.cs407.cadence.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// acoustid
data class AcoustIdResponse(
    val status: String,
    val results: List<AcoustIdResult>?
)

data class AcoustIdResult(
    val id: String,
    val recordings: List<AcoustIdRecording>?
)

data class AcoustIdRecording(
    val id: String,
    val title: String?,
    val artists: List<AcoustIdArtist>?
)

data class AcoustIdArtist(
    val id: String,
    val name: String
)

// musicbrainz
data class MusicBrainzRecording(
    val id: String,
    val title: String,
    val length: Int?,
    val tags: List<MusicBrainzTag>?
)

data class MusicBrainzTag(
    val count: Int,
    val name: String
)

interface AcoustIdApi {
    @GET("v2/lookup")
    suspend fun lookupByMetadata(
        @Query("client") client: String,
        @Query("artist") artist: String,
        @Query("title") title: String,
        @Query("meta") meta: String = "recordings"
    ): Response<AcoustIdResponse>
}

// musicbrainz search response
data class MusicBrainzSearchResponse(
    val recordings: List<MusicBrainzRecording>?
)

interface MusicBrainzApi {
    @GET("recording/{id}")
    suspend fun getRecording(
        @retrofit2.http.Path("id") id: String,
        @Query("fmt") format: String = "json",
        @Query("inc") includes: String = "tags"
    ): Response<MusicBrainzRecording>
    
    @GET("recording")
    suspend fun searchRecordings(
        @Query("query") query: String,
        @Query("fmt") format: String = "json",
        @Query("limit") limit: Int = 1
    ): Response<MusicBrainzSearchResponse>
}

// theaudiodb search response
data class TheAudioDbResponse(
    val track: List<TheAudioDbTrack>?
)

data class TheAudioDbTrack(
    val strTrack: String?,
    val strArtist: String?,
    val strAlbum: String?,
    val intBPM: String?
)

interface TheAudioDbApi {
    @GET("searchtrack.php")
    suspend fun searchTrack(
        @Query("s") artist: String,
        @Query("t") track: String
    ): Response<TheAudioDbResponse>
}
