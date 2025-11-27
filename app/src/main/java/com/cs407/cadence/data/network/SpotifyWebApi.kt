package com.cs407.cadence.data.network

import retrofit2.Response
import retrofit2.http.*

data class SpotifyAuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)

data class SpotifySearchResponse(
    val tracks: SpotifyTracks
)

data class SpotifyTracks(
    val items: List<SpotifyTrack>
)

data class SpotifyTrack(
    val id: String,
    val name: String,
    val uri: String,
    val artists: List<SpotifyArtist>,
    val album: SpotifyAlbum
)

data class SpotifyArtist(
    val name: String
)

data class SpotifyAlbum(
    val name: String,
    val images: List<SpotifyImage>
)

data class SpotifyImage(
    val url: String,
    val height: Int,
    val width: Int
)

data class SpotifyAudioFeaturesResponse(
    val audio_features: List<SpotifyAudioFeatures?>
)

data class SpotifyAudioFeatures(
    val id: String,
    val tempo: Double,
    val energy: Double,
    val danceability: Double
)

data class SpotifyRecommendationsResponse(
    val tracks: List<SpotifyTrack>
)
interface SpotifyWebApi {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Header("Authorization") authorization: String
    ): Response<SpotifyAuthResponse>

    @GET("v1/search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 50,
        @Header("Authorization") authorization: String
    ): Response<SpotifySearchResponse>

    @GET("v1/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<Any>

    @GET("v1/audio-features")
    suspend fun getAudioFeatures(
        @Query("ids") ids: String,
        @Header("Authorization") authorization: String
    ): Response<SpotifyAudioFeaturesResponse>
    
    @GET("v1/audio-features/{id}")
    suspend fun getSingleAudioFeatures(
        @Path("id") id: String,
        @Header("Authorization") authorization: String
    ): Response<SpotifyAudioFeatures>

    @GET("v1/recommendations")
    suspend fun getRecommendations(
        @Query("seed_genres") genres: String? = null,
        @Query("seed_tracks") tracks: String? = null,
        @Query("seed_artists") artists: String? = null,
        @Query("target_tempo") targetTempo: Int,
        @Query("min_tempo") minTempo: Int,
        @Query("max_tempo") maxTempo: Int,
        @Query("limit") limit: Int = 50,
        @Header("Authorization") authorization: String
    ): Response<SpotifyRecommendationsResponse>
}
