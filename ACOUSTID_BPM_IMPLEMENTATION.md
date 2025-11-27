# AcoustID BPM Detection Implementation

## Overview

Due to Spotify Web API limitations (403 errors on `/v1/audio-features` endpoint and 404 on `/v1/recommendations`), we implemented an alternative BPM detection solution using AcoustID and MusicBrainz APIs.

## Architecture

### Flow Diagram

```
User selects workout (Running, 130-150 BPM, Electronic)
    ↓
SpotifyWebApiClient.getTracksByBpmAndGenre()
    ↓
1. Generate BPM-optimized search query
   BpmDatabase.getSearchQueryForBpmRange("electronic", 130, 150)
   → "electronic dance high energy uptempo 140 bpm"
    ↓
2. Search Spotify for tracks
   Spotify Search API (/v1/search) ✓ Works with OAuth
   → Returns ~50 electronic tracks
    ↓
3. Filter by actual BPM
   AcoustIdClient.filterTracksByBpm(tracks, targetBpm=140, tolerance=10)
       ↓
       For each track:
       - Query AcoustID with track name + artist
         GET https://api.acoustid.org/v2/lookup?meta=recordings
       - Extract MusicBrainz recording ID
       - Query MusicBrainz for tags
         GET https://musicbrainz.org/ws/2/recording/{id}?inc=tags
       - Parse BPM from tags
       - Keep track if BPM is 130-150
    ↓
4. Return filtered tracks (only those matching BPM)
    ↓
5. Play via Spotify Android SDK ✓ Works
```

## Components

### 1. BpmDatabase.kt

Generates BPM-optimized search queries to increase likelihood of finding tracks in target BPM range.

**Methods:**

- `getSearchQueryForBpmRange(genre: String, minBpm: Int, maxBpm: Int): String`

  - Returns genre-specific queries like:
    - "electronic dance high energy uptempo 140 bpm" for 130-150 BPM
    - "chill lo-fi downtempo 100 bpm" for 90-110 BPM

- `getActivityOptimizedQuery(activity: String, genre: String): String`
  - Returns activity-specific queries like:
    - "running workout high energy electronic fast tempo"

### 2. AcoustIdApi.kt

Defines Retrofit API interfaces for external BPM services.

**AcoustIdApi Interface:**

```kotlin
suspend fun lookupByMetadata(
    @Query("client") client: String,
    @Query("artist") artist: String,
    @Query("title") title: String,
    @Query("meta") meta: String = "recordings"
): Response<AcoustIdResponse>
```

**MusicBrainzApi Interface:**

```kotlin
suspend fun getRecording(
    @Path("id") id: String,
    @Query("fmt") format: String = "json",
    @Query("inc") includes: String = "tags"
): Response<MusicBrainzRecording>
```

### 3. AcoustIdClient.kt

Main client for BPM detection logic.

**Key Methods:**

**`getTrackBpm(trackName: String, artistName: String): Int?`**

1. Queries AcoustID API with track metadata
2. Extracts MusicBrainz recording ID from results
3. Queries MusicBrainz API for recording tags
4. Parses BPM from tags (looks for "bpm" tag or numeric tags)
5. Returns BPM as Int or null if not found

**`filterTracksByBpm(tracks: List<SpotifyTrack>, targetBpm: Int, bpmTolerance: Int): List<SpotifyTrack>`**

1. Takes list of Spotify tracks from search results
2. For each track (limited to first 10 to avoid rate limits):
   - Gets BPM using `getTrackBpm()`
   - Checks if BPM is within target range (targetBpm ± tolerance)
   - Adds to filtered list if matches
3. Returns filtered tracks
4. Fallback: Returns original tracks if none match (better than empty playlist)

### 4. SpotifyWebApiClient.kt

Updated main entry point for track retrieval.

**`getTracksByBpmAndGenre(context, genre, targetBpm, bpmRange)`**

1. Generates BPM-optimized search query
2. Searches Spotify API for tracks
3. **NEW:** Calls `AcoustIdClient.filterTracksByBpm()` to verify actual BPMs
4. Returns BPM-filtered tracks
5. Fallback to simple genre search if needed

## API Keys

### AcoustID

- **API Key:** `8XaBELgH` (demo key, can get your own at https://acoustid.org/api-key)
- **Rate Limits:** Free tier allows 3 requests/second
- **Base URL:** `https://api.acoustid.org/v2/`

### MusicBrainz

- **No API Key Required**
- **Rate Limits:** 1 request/second (respected with 100ms delays)
- **Base URL:** `https://musicbrainz.org/ws/2/`
- **User-Agent Required:** Set automatically by OkHttp

## Performance Considerations

### Current Implementation

- Checks first 10 tracks from search results
- 100ms delay between API calls (respects rate limits)
- Total time: ~2-3 seconds for 10 track lookups
- Parallel processing not implemented (could cause rate limit issues)

### Optimization Opportunities

1. **Local BPM Cache**

   - Store BPM in Room database
   - Cache lookups by Spotify track ID
   - Avoid repeated API calls for same tracks

2. **Batch Processing**

   - Pre-fetch BPM for popular tracks
   - Background job to build BPM database
   - Instant filtering for cached tracks

3. **Fallback Strategy**
   - Use BpmDatabase queries first (instant)
   - Only verify BPM for uncached tracks
   - Graceful degradation if APIs unavailable

## Limitations

### Current Constraints

- **Coverage:** Not all tracks have BPM data in MusicBrainz
- **Accuracy:** User-submitted tags may vary
- **Speed:** API calls add latency (~200ms per track)
- **Rate Limits:** Free tier limits concurrent requests

### Mitigation

- Smart search queries increase hit rate
- Fallback to unfiltered results if no matches
- Limited to 10 tracks to balance accuracy and speed
- 100ms delays prevent rate limit issues

## Testing

### Test Cases

1. **Running + Electronic (130-150 BPM)**
   - Should filter high-energy EDM tracks
   - Verify Crystal Castles - Baptism (120 BPM) is filtered OUT
2. **Walking + Pop (90-110 BPM)**
   - Should return slower-tempo pop songs
3. **Jogging + Rock (110-130 BPM)**
   - Should return mid-tempo rock tracks

### Debugging

Enable logging to see BPM filtering in action:

```
adb logcat | grep -E "(AcoustIdClient|SpotifyWebApiClient)"
```

Expected logs:

```
AcoustIdClient: Looking up BPM for 'Track Name' by 'Artist'
AcoustIdClient: Found MusicBrainz recording ID: abc-123
AcoustIdClient: Found BPM: 145 for 'Track Name'
AcoustIdClient: ✓ Track 'Track Name' matches BPM filter (145 BPM)
SpotifyWebApiClient: After BPM filtering: 7 tracks
```

## Future Enhancements

1. **Fingerprinting Support**

   - Use Chromaprint to generate audio fingerprints
   - More accurate matching than metadata
   - Requires audio analysis library

2. **Machine Learning**

   - Train model on Spotify preview URLs
   - Predict BPM from audio features
   - Offline BPM detection

3. **Hybrid Approach**
   - Use Spotify's playlists tagged with BPM
   - Community-curated workout playlists
   - Aggregate multiple data sources

## Troubleshooting

### No tracks returned

- Check OAuth token is valid
- Verify network connectivity
- Check AcoustID/MusicBrainz API status

### All tracks filtered out

- Increase `bpmTolerance` parameter
- Check if genre has tracks in target BPM range
- Review logs for BPM values found

### 403/404 errors

- AcoustID: Check API key
- MusicBrainz: Check rate limits (1 req/sec)

### Slow performance

- Reduce number of tracks checked (currently 10)
- Implement local BPM cache
- Pre-fetch popular tracks
