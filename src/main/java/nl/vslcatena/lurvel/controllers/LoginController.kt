package nl.vslcatena.lurvel.controllers

import nl.vslcatena.lurvel.connections.LdapConnection
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class LoginController {

    @PostMapping("/login")
    fun login(
        @RequestParam(value = "username")
        username: String,
        @RequestParam(value = "password")
        password: String
    ) = LdapConnection.login(username, password) ?: ResponseEntity<Any?>(null as Any?, HttpStatus.UNAUTHORIZED)
}