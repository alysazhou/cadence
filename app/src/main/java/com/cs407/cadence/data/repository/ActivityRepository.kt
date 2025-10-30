package com.cs407.cadence.data
data class Activity(
    val name: String,
    val description: String,
    val compatibleGenres: List<String>
)
object ActivityRepository {
    // TODO: replace genres
    private val activities = listOf(
        Activity(
            name = "Walking",
            description = "Light and casual",
            compatibleGenres = listOf("Pop", "Rock", "Electronic", "Hip-Hop")
        ),
        Activity(
            name = "Jogging",
            description = "Steady and energetic",
            compatibleGenres = listOf("Metal", "Hip-Hop", "Rock")
        ),
        Activity(
            name = "Running",
            description = "Fast-paced and intense",
            compatibleGenres = listOf("Electronic", "Pop", "Indie")
        )
    )

    fun getAllActivities(): List<Activity> = activities

    fun getActivityNames(): List<String> = activities.map { it.name }

    fun findActivityByName(name: String): Activity? = activities.find { it.name == name }
}
