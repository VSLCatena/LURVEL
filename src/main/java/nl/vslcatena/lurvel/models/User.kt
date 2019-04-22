package nl.vslcatena.lurvel.models

import java.util.*

data class User(
    val id: UUID,
    val description: String?,
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val committees: List<Committee>
)