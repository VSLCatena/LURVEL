package nl.vslcatena.lurvel.models

data class User(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val commissions: List<Commission>
)