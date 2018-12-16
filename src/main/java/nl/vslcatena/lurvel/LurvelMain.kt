package nl.vslcatena.lurvel

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class LurvelMain

fun main(args: Array<String>) {
    LdapConnection.setup()
    SpringApplication.run(LurvelMain::class.java, *args)
}