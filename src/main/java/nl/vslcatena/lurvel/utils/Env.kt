package nl.vslcatena.lurvel.utils

import java.io.File

object Env {
    var LDAP_DOMAIN: String = ""
    var LDAP_SERVICEACCOUNT_USERNAME: String = ""
    var LDAP_SERVICEACCOUNT_PASSWORD: String = ""
    var LDAP_USER_BASE_DN: String = ""
    var LDAP_COMMITTEE_BASE_DN: String = ""
    var LDAP_COMMITTEE_DN: String = ""

    var SERVER_PORT: String = ""


    init {
        val file = FileUtils.fromExternal(".env")

        // If an .env file doesn't exist, create one using the .env.example file in our resource folder
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(
                FileUtils.inputFromResources(".env.example")
                    ?.bufferedReader()
                    ?.readText()
                    ?: ""
            )
        }

        // Else we read all files
        val lines = file.readLines()
        lines.forEach { line ->
            // We split all lines to key and value
            val split = line.split('=', limit = 2)
            if (split.size < 2) return@forEach
            val (key, value) = split
            try {
                // We try to find a field with the key
                val field = this.javaClass.getDeclaredField(key.trim())
                // And set it to the value
                field.set(this, value.trim())
            } catch(e: NoSuchFieldException) {
                e.printStackTrace()
            }
        }
    }
}