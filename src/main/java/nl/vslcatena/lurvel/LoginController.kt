package nl.vslcatena.lurvel

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
    ): Any {
        return LdapConnection.findLdapUser(username, password) ?: "Invalid credentials"
    }
}