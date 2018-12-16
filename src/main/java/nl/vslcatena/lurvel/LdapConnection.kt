package nl.vslcatena.lurvel

import nl.vslcatena.lurvel.models.Commission
import nl.vslcatena.lurvel.models.User
import java.util.*
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.directory.*


object LdapConnection {
    private var serviceLdapContext: DirContext? = null
    val commissions = ArrayList<Commission>()

    fun setup() {
        commissions.addAll(getCommissions())
    }

    fun getServiceLdapContext(): DirContext {
        if (serviceLdapContext == null) {
            serviceLdapContext = createServiceLdapContext()
        }
        return serviceLdapContext!!
    }

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

        return InitialDirContext(props)
    }

    fun findLdapUser(mail: String, password: String): Any? {
        // Here we set up what we want to grab
        val controls = SearchControls()
        controls.searchScope = SearchControls.SUBTREE_SCOPE
        controls.returningAttributes = arrayOf("distinguishedName")

        // I don't know if filter injections are a thing...
        if (!mail.matches(Regex("^[a-zA-Z0-9.@]+$"))) return null

        // We search through the user group where the email is the given email
        val rawResults = getServiceLdapContext()
            .search(Env.LDAP_USER_DC, "(mail=$mail)", controls)

        // If we can't find any user with this we return null
        if (!rawResults.hasMore()) return null

        // We grab the DN of the user
        val dn = rawResults.nextElement().attributes.get("distinguishedName").get() as String

        // And we try to log the user in with their DN and password
        val userContext = try {
            createLdapContext(dn, password)
        } catch(e: Exception) {
            // If we failed creating a context that means we weren't able to log in
            return null
        }

        // Now we retrieve the user data attributes
        val detailedRawResults = userContext
            .getAttributes(dn, arrayOf("memberOf", "cn", "mail", "description", "telephoneNumber"))

        // Convert it to a map
        val attributeMap = detailedRawResults.all.toMap()

        // Then we loop through all their groups and check if any of them is a commission
        val userCommissions = ArrayList<Commission>()
        (attributeMap["memberOf"] as List<String>).forEach { commissionString ->
            val commission = commissions.find { it.dn == commissionString }
            if(commission != null) {
                userCommissions.add(commission)
            }
        }

        // And at last we create the user
        return User(
            attributeMap["description"] as String,
            attributeMap["cn"] as String,
            attributeMap["telephoneNumber"] as? String,
            attributeMap["mail"] as? String,
            userCommissions
        )
    }

    private fun getCommissions(): List<Commission> {
        // Here we set up the things we want to grab
        val controls = SearchControls()
        controls.searchScope = SearchControls.SUBTREE_SCOPE
        controls.returningAttributes = arrayOf("cn", "distinguishedName")

        // We grab all the groups which are a commission
        val results = getServiceLdapContext()
            .search(Env.LDAP_COMMISSION_DC, "(memberOf=${Env.LDAP_COMMISSION_DN})", controls)

        val commissions = ArrayList<Commission>()
        while (results.hasMore()) {
            val result = results.nextElement().attributes.all.toMap()
            // Then we create a new commission with its name and DN
            commissions.add(
                Commission(
                    result["cn"] as String,
                    result["distinguishedName"] as String
                )
            )
        }

        return commissions
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