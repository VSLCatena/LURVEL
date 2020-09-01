package nl.vslcatena.lurvel

import nl.vslcatena.lurvel.connections.LdapConnection
import nl.vslcatena.lurvel.utils.Env
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class LurvelMain

fun main(args: Array<String>) {
    val application = SpringApplication(LurvelMain::class.java)
    application.setDefaultProperties(mapOf(
        "server.port" to Env.SERVER_PORT,
        "server.error.whitelabel.enabled" to "false",
    ))
    application.run(*args)
}