package nl.vslcatena.lurvel.utils

import java.io.File
import java.io.InputStream
import java.nio.file.FileSystems

object FileUtils {
    fun inputFromResources(fileName: String): InputStream? {
        return javaClass.classLoader.getResourceAsStream(fileName)
    }

    fun fromExternal(fileName: String): File {
        return FileSystems.getDefault().getPath(fileName).toFile()
    }
}