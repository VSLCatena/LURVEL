package nl.vslcatena.lurvel.connections

import nl.vslcatena.lurvel.utils.Env
import nl.vslcatena.lurvel.models.Committee
import nl.vslcatena.lurvel.models.User
import nl.vslcatena.lurvel.utils.UIDConverter
import java.util.*
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.directory.Attribute
import javax.naming.directory.DirContext
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls


object LdapConnection {
    private fun createServiceLdapContext(): DirContext {
        // Here we create a context with our service user
        return createLdapContext(
            Env.LDAP_SERVICEACCOUNT_USERNAME,
            Env.LDAP_SERVICEACCOUNT_PASSWORD
        )
    }

    private fun createLdapContext(dn: String, password: String): DirContext {
        val props = Properties()
        // The class that does the connection (For us that is LDAP)
        props[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
        // The domain of our DC server
        props[Context.PROVIDER_URL] = Env.LDAP_DOMAIN
        // The DN of our LDAP user
        props[Context.SECURITY_PRINCIPAL] = dn
        // The password of our LDAP user
        props[Context.SECURITY_CREDENTIALS] = password

        return InitialDirContext(props).apply {
            addToEnvironment("java.naming.ldap.attributes.binary","objectGUID")
        }
    }

    fun getUser(dn: String): User? {
        val context = createServiceLdapContext()
        try {
            return getUser(dn, context)
        } finally {
            context.close()
        }
    }

    fun getUserCommitteesByUid(uid: String): List<Committee>? {
        val context = createServiceLdapContext()
        try {
            val controls = SearchControls()
            controls.searchScope = SearchControls.SUBTREE_SCOPE
            controls.returningAttributes = arrayOf("memberOf")

            val result = context.search(Env.LDAP_USER_DC, "(objectGUID=$uid)", controls)
            if (!result.hasMore()) return null

            val attributeMap = result.next().attributes.all.toMap()
            return committeesFromAttribute(attributeMap["memberOf"] as? List<*>)
        } finally {
            context.close()
        }
    }

    fun getUser(dn: String, withContext: DirContext): User? {
        // Now we retrieve the user data attributes
        val detailedRawResults = withContext
            .getAttributes(dn, arrayOf("objectGUID", "memberOf", "cn", "mail", "employeeNumber", "telephoneNumber"))

        // Convert it to a map
        val attributeMap = detailedRawResults.all.toMap()

        // Then we loop through all their groups and check if any of them is a committees
        val userCommittees = committeesFromAttribute(attributeMap["memberOf"] as? List<*>)

        // And at last we create the user
        return User(
            UIDConverter.bytesToUid(attributeMap["objectGUID"] as ByteArray),
            attributeMap["employeeNumber"]?.toString(),
            attributeMap["cn"] as String,
            attributeMap["telephoneNumber"] as? String,
            attributeMap["mail"] as? String,
            userCommittees.map { it.id }
        )
    }

    private fun committeesFromAttribute(listOfCommittees: List<*>?): List<Committee> {
        // cache all committees so we don't have to grab them over and over again
        val allCommittees = getAllCommittees()

        // Then for every committee we get in the parameter we check if it exists in the committee list
        return listOfCommittees?.mapNotNull { allCommittees[it] } ?: emptyList()
    }

    fun login(mail: String, password: String): User? {
        // Here we set up what we want to grab
        val controls = SearchControls()
        controls.searchScope = SearchControls.SUBTREE_SCOPE
        controls.returningAttributes = arrayOf("distinguishedName")

        // I don't know if filter injections are a thing...
        if (!mail.matches(Regex("^[a-zA-Z0-9.@]+$"))) return null

        // We search through the user group where the email is the given email
        val serviceContext = createServiceLdapContext()

        val dn: String

        try {
            val rawResults = serviceContext
                .search(Env.LDAP_USER_DC, "(mail=$mail)", controls)

            // If we can't find any user with this we return null
            if (!rawResults.hasMore()) return null

            // We grab the DN of the user
            dn = rawResults.nextElement().attributes.get("distinguishedName").get() as String
        } finally {
            serviceContext.close()
        }

        // And we try to log the user in with their DN and password
        val userContext = try {
            createLdapContext(dn, password)
        } catch(e: Exception) {
            // If we failed creating a context that means we weren't able to log in
            return null
        }

        return getUser(dn, withContext = userContext).also {
            userContext.close()
        }
    }

    private var committeesCache: Map<String, Committee>? = null
    private var apiCommitteesCache: Collection<Committee>? = null
    private var lastUpdated: Long = 0
    fun getAllCommittees(): Map<String, Committee> {
        // We expect the software to run for days, this means we want to refresh the committees once a day
        if (committeesCache != null && lastUpdated + 24 * 60 * 60 * 1000 > System.currentTimeMillis())
            return committeesCache!!

        // Here we set up the things we want to grab
        val controls = SearchControls()
        controls.searchScope = SearchControls.SUBTREE_SCOPE
        controls.returningAttributes = arrayOf("objectGUID", "cn", "distinguishedName")

        // We grab all the groups which are a committee
        val context = createServiceLdapContext()
        val allCommittees = HashMap<String, Committee>()
        try {
            val results = context.search(Env.LDAP_COMMITTEE_DC, "(memberOf=${Env.LDAP_COMMITTEE_DN})", controls)

            while (results.hasMore()) {
                val result = results.nextElement().attributes.all.toMap()
                // Then we create a new committee with its name and DN
                allCommittees[result["distinguishedName"] as String] =
                    Committee(
                        UIDConverter.bytesToUid(result["objectGUID"] as ByteArray),
                        result["cn"] as String
                    )
            }
        } finally {
            context.close()
        }

        committeesCache = allCommittees
        apiCommitteesCache = allCommittees.values


        lastUpdated = System.currentTimeMillis()
        return allCommittees
    }

    fun getApiCommittees(): Collection<Committee> {
        getAllCommittees()

        return apiCommitteesCache!!
    }

    fun NamingEnumeration<out Attribute>.toMap(): Map<String, Any> {
        val results = HashMap<String, Any>()

        // We loop over all attributes
        while (this.hasMore()) {
            val attribute = this.nextElement()

            // If the attribute is just a single element we just add that
            if (attribute.size() == 1) {
                results[attribute.id] = attribute.get()
            } else {
                // If not we create a list containing all attributes
                val subResults = ArrayList<Any>()
                val subAttributes = attribute.all
                while (subAttributes.hasMore()) {
                    subResults.add(subAttributes.next())
                }
                results[attribute.id] = subResults
            }
        }

        return results
    }
}