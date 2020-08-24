package nl.vslcatena.lurvel.models


data class User(
    val id: String,
    val memberNumber: String?,
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val committees: List<String>,
)