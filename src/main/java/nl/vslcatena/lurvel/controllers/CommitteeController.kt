package nl.vslcatena.lurvel.controllers

import nl.vslcatena.lurvel.connections.LdapConnection
import nl.vslcatena.lurvel.utils.UIDConverter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CommitteeController {

    @GetMapping("/committees")
    fun requestCommittees(
        @RequestParam(value = "userId", required = false)
        userId: String? = null
    ) : Any? {
        if (userId == null)
            return LdapConnection.getApiCommittees()

        val userCommittees = LdapConnection.getUserCommitteesByUid(UIDConverter.escapedUid(userId))

        // We only want to return the id's of committees
        return userCommittees?.map { it.id }
    }

}