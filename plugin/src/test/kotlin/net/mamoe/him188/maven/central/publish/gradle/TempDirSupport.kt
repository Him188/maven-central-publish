package net.mamoe.him188.maven.central.publish.gradle

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

private object C

fun createTempDirSmart(): File {
    fun impl(): File {
        System.getenv("test.temp.dir")?.let { dir ->
            println("Using temp dir: $dir")
            return Files.createTempDirectory(Path.of(dir), "junit").toFile()
        }

        C::class.java.classLoader.getResource("local.tmpDir.txt")?.readText()?.takeIf { it.isNotBlank() }?.let { dir ->
            println("Using temp dir: $dir")
            return Files.createTempDirectory(Path.of(dir), "junit").toFile()
        }

        println("Using system default temp dir.")
        return Files.createTempDirectory("junit").toFile()
    }

    deleteHook
    return impl().also {
        println("Creating temp dir(which will be deleted on exit): ${it.absolutePath}")
        filesToDelete.add(it.absolutePath)
    }
}

private val filesToDeleteLock = ReentrantLock()
private val filesToDelete: MutableCollection<String> = HashSet()

private val deleteHook by lazy {
    Runtime.getRuntime().addShutdownHook(thread(false) {
        for (file in filesToDeleteLock.withLock { filesToDelete.toList() }) {
            File(file).deleteRecursively()
        }
    })
}

fun File.registerDeleteHook() {
    filesToDeleteLock.withLock {
        filesToDelete.add(this.absolutePath)
    }
}