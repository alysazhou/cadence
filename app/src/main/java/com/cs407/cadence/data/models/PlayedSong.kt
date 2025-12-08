package com.cs407.cadence.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayedSong(
        val id: String,
        val name: String,
        val artist: String,
        val albumArtUrl: String?,
        val bpm: Int?
) : Parcelable
