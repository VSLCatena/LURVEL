package nl.vslcatena.lurvel

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object Env {
    var LDAP_DOMAIN: String = ""
    var LDAP_SERVICEACCOUNT_USERNAME: String = ""
    var LDAP_SERVICEACCOUNT_PASSWORD: String = ""
    var LDAP_USER_DC: String = ""
    var LDAP_COMMISSION_DC: String = ""
    var LDAP_COMMISSION_DN: String = ""

    init {
        val file = File(System.getProperty("user.dir") + File.separator + ".env")

        // If an .env file doesn't exist, create one using the .env.example file in our resource folder
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(javaClass.classLoader.getResourceAsStream(".env.example").bufferedReader().readText())
        }

        // Else we read all files
        val lines = file.readLines()
        lines.forEach { line ->
            // We split all lines to key and value
            val (key, value) = line.split('=', limit = 2)
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