package com.cs407.cadence.data

// bpm database for workout track searches
object BpmDatabase {
    
    data class BpmTrack(
        val name: String,
        val artist: String,
        val bpm: Int,
        val genre: String
    )
    
    // get search query optimized for bpm range
    fun getSearchQueryForBpmRange(genre: String, minBpm: Int, maxBpm: Int): String {
        val targetBpm = (minBpm + maxBpm) / 2
        
        return when {
            // high bpm (140-150) for running
            targetBpm >= 140 -> {
                when (genre.lowercase()) {
                    "electronic", "edm" -> "daft punk skrillex deadmau5 calvin harris"
                    "hip-hop" -> "eminem nicki minaj migos drake fast"
                    "pop" -> "lady gaga katy perry rihanna dance"
                    "rock" -> "green day blink 182 sum 41 fast"
                    "metal" -> "metallica slayer megadeth fast"
                    else -> "$genre popular fast"
                }
            }
            // medium bpm (110-130) for jogging
            targetBpm >= 110 -> {
                when (genre.lowercase()) {
                    "electronic", "edm" -> "kygo avicii martin garrix chainsmokers"
                    "hip-hop" -> "kendrick lamar j cole drake moderate"
                    "pop" -> "bruno mars justin timberlake ariana grande"
                    "rock" -> "foo fighters red hot chili peppers"
                    "metal" -> "iron maiden slipknot moderate"
                    else -> "$genre popular moderate"
                }
            }
            // low bpm (90-110) for walking
            else -> {
                when (genre.lowercase()) {
                    "electronic", "edm" -> "odesza disclosure flume chill"
                    "hip-hop" -> "drake post malone chill"
                    "pop" -> "ed sheeran adele sam smith"
                    "rock" -> "coldplay u2 radiohead slow"
                    "metal" -> "black sabbath metallica slow"
                    else -> "$genre popular slow"
                }
            }
        }
    }
    
    // get multiple search queries to expand track pool
    fun getMultipleSearchQueries(genre: String, minBpm: Int, maxBpm: Int): List<String> {
        val targetBpm = (minBpm + maxBpm) / 2
        
        return when {
            // high bpm (140-150) for running
            targetBpm >= 140 -> {
                when (genre.lowercase()) {
                    "electronic", "edm" -> listOf(
                        "daft punk discovery",
                        "skrillex recess",
                        "deadmau5 4x4",
                        "calvin harris motion",
                        "pendulum immersion",
                        "prodigy fat",
                        "chemical brothers",
                        "justice cross"
                    )
                    "hip-hop" -> listOf(
                        "eminem kamikaze",
                        "kendrick lamar",
                        "migos culture",
                        "travis scott",
                        "lil wayne"
                    )
                    "pop" -> listOf(
                        "lady gaga fame",
                        "katy perry prism",
                        "rihanna loud",
                        "britney spears",
                        "kesha"
                    )
                    "rock" -> listOf(
                        "green day dookie",
                        "blink 182 enema",
                        "offspring americana",
                        "sum 41",
                        "ramones"
                    )
                    else -> listOf(
                        "$genre fast tempo",
                        "$genre energetic",
                        "$genre high energy"
                    )
                }
            }
            // medium bpm (110-130) for jogging
            targetBpm >= 110 -> {
                when (genre.lowercase()) {
                    "electronic", "edm" -> listOf(
                        "kygo cloud nine",
                        "avicii true",
                        "martin garrix",
                        "chainsmokers memories",
                        "zedd clarity",
                        "david guetta"
                    )
                    "hip-hop" -> listOf(
                        "drake views",
                        "j cole forest",
                        "kanye west graduation",
                        "post malone",
                        "logic"
                    )
                    "pop" -> listOf(
                        "bruno mars unorthodox",
                        "justin timberlake",
                        "ariana grande sweetener",
                        "dua lipa",
                        "charlie puth"
                    )
                    "rock" -> listOf(
                        "foo fighters",
                        "red hot chili peppers",
                        "arctic monkeys",
                        "kings of leon",
                        "killers"
                    )
                    else -> listOf(
                        "$genre moderate tempo",
                        "$genre popular hits"
                    )
                }
            }
            // low bpm (90-110) for walking
            else -> {
                when (genre.lowercase()) {
                    "electronic", "edm" -> listOf(
                        "odesza summer",
                        "disclosure settle",
                        "flume skin",
                        "bonobo migration",
                        "tycho dive"
                    )
                    "hip-hop" -> listOf(
                        "drake take care",
                        "post malone beerbongs",
                        "tyler creator",
                        "childish gambino",
                        "frank ocean"
                    )
                    "pop" -> listOf(
                        "ed sheeran divide",
                        "adele 21",
                        "sam smith lonely",
                        "john legend",
                        "billie eilish"
                    )
                    "rock" -> listOf(
                        "coldplay parachutes",
                        "u2 joshua",
                        "radiohead ok computer",
                        "oasis wonderwall",
                        "snow patrol"
                    )
                    else -> listOf(
                        "$genre slow tempo",
                        "$genre chill"
                    )
                }
            }
        }
    }
    
    // get search terms based on activity and genre
    fun getActivityOptimizedQuery(activity: String, genre: String): String {
        return when (activity.lowercase()) {
            "running" -> {
                when (genre.lowercase()) {
                    "electronic", "edm" -> "workout running edm high energy"
                    "hip-hop" -> "workout hip hop running"
                    "pop" -> "workout pop running energetic"
                    "rock" -> "workout rock running"
                    "metal" -> "workout metal running"
                    else -> "workout running $genre"
                }
            }
            "jogging" -> {
                when (genre.lowercase()) {
                    "electronic", "edm" -> "jogging electronic music"
                    "hip-hop" -> "jogging hip hop"
                    "pop" -> "jogging pop music"
                    "rock" -> "jogging rock"
                    "metal" -> "jogging metal"
                    else -> "jogging $genre music"
                }
            }
            "walking" -> {
                when (genre.lowercase()) {
                    "electronic", "edm" -> "walking chill electronic"
                    "hip-hop" -> "walking chill hip hop"
                    "pop" -> "walking pop easy"
                    "rock" -> "walking rock easy"
                    "metal" -> "walking metal easy"
                    else -> "walking $genre"
                }
            }
            else -> {
                "$activity $genre music"
            }
        }
    }
}
