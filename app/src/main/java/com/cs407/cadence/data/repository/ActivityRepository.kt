package com.cs407.cadence.data
data class Activity(val name: String, val description: String, val compatibleGenres: List<String>)

object ActivityRepository {
    private val activities =
            listOf(
                    Activity(
                            name = "Walking",
                            description = "100-120 BPM",
                            compatibleGenres = listOf("Pop", "Indie", "R&B", "Jazz", "Reggae")
                    ),
                    Activity(
                            name = "Jogging",
                            description = "120-140 BPM",
                            compatibleGenres =
                                    listOf("Electronic", "Hip-Hop", "Rock", "Pop", "Funk")
                    ),
                    Activity(
                            name = "Running",
                            description = "140-160 BPM",
                            compatibleGenres =
                                    listOf("Drum and Bass", "Techno", "Dubstep", "Metal", "Trance")
                    )
            )

    fun getAllActivities(): List<Activity> = activities

    fun getActivityNames(): List<String> = activities.map { it.name }

    fun findActivityByName(name: String): Activity? = activities.find { it.name == name }
}
