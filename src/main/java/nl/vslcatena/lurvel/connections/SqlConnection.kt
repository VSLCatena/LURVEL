//package nl.vslcatena.lurvel.connections
//
//import nl.vslcatena.lurvel.models.Committee
//import nl.vslcatena.lurvel.models.User
//import nl.vslcatena.lurvel.utils.Env
//import java.sql.Connection
//import java.sql.DriverManager
//import java.util.*
//
//object SqlConnection {
//    private var sqlConntection: Connection? = null
//
//    fun getConnection(): Connection {
//        if(sqlConntection == null) {
//            val properties = Properties()
//            properties["user"] = Env.MYSQL_USERNAME
//            properties["password"] = Env.MYSQL_PASSWORD
//
//            sqlConntection = DriverManager.getConnection(
//                "jdbc:${Env.MYSQL_DOMAIN}",
//                properties
//            )
//        }
//        return sqlConntection!!
//    }
//
//
//    private fun saveComittee(comittee: Committee) {
//        val ptsd = getConnection()
//            .prepareStatement(
//                "INSERT INTO users (id, name, email, phone) " +
//                        "VALUES(?, ?, ?, ?) " +
//                        "ON DUPLICATE KEY UPDATE " +
//                        "name=?, email=?, phone=?")
//
//        ptsd.apply {
//            // insert
//            setString(1, user.id)
//            setString(2, user.name)
//            setString(3, user.email)
//            setString(4, user.phoneNumber)
//
//            // update
//            setString(5, user.name)
//            setString(6, user.email)
//            setString(7, user.phoneNumber)
//        }
//    }
//
//    fun saveUser(user: User) {
//        val ptsdUser = getConnection()
//            .prepareStatement(
//                "INSERT INTO users (id, name, email, phone) " +
//                        "VALUES(?, ?, ?, ?) " +
//                        "ON DUPLICATE KEY UPDATE " +
//                        "name=?, email=?, phone=?")
//
//        ptsdUser.apply {
//            // insert
//            setString(1, user.id)
//            setString(2, user.name)
//            setString(3, user.email)
//            setString(4, user.phoneNumber)
//
//            // update
//            setString(5, user.name)
//            setString(6, user.email)
//            setString(7, user.phoneNumber)
//        }
//        ptsdUser.execute()
//
//        val ptsdUserComittees = getConnection()
//            .prepareStatement(
//                "INSERT INTO user_committee (user_id, committee_id) " +
//                        "VALUES(?, ?)" +
//                        "ON DUPLICATE KEY UPDATE " +
//                        "user_id=?"
//            )
//
//        user.committees.forEach { committee ->
//            ptsdUserComittees.apply {
//                // insert
//                setString(1, user.id)
//                setString(2, committee.)
//            }
//        }
//    }
//
//    fun getUserFromId(id: String) {
//
//    }
//}