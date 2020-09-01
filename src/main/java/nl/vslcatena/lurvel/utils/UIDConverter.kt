package nl.vslcatena.lurvel.utils

object UIDConverter {
    fun bytesToUid(byteArray: ByteArray): String {
        return byteArray
            .map {
                (it.toInt() and 0xFF) // Convert from signed byte to unsigned "byte"
                    .toString(16) // Then convert it to hexadecimal
                    .padStart(2, '0') // Pad it with '0' to 2 characters (this can happen if it is < 16)
            }
            .joinToString("") // Then we join it back to a string

        // This should always be 32 characters
    }

    fun escapedUid(uid: String): String {
        return uid.chunked(2).joinToString("") { "\\$it" }
    }
}