package nl.vslcatena.lurvel

import nl.vslcatena.lurvel.connections.LdapConnection
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class LurvelMain

fun main(args: Array<String>) {
    SpringApplication.run(LurvelMain::class.java, *args)
}