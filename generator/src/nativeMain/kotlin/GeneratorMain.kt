@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.him188.maven.central.publish.generator

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.him188.maven.central.publish.protocol.PublicationCredentials
import platform.posix.*

private val SONATYPE_TXT_TIPS = """Please edit sonatype.txt following the rules:
                    |First line: Sonatype username
                    |Second line: Sonatype password
                    |Third line: package group (optional)
                    |
                    |If you set package group, this will be the default when configuring projects. If you use only one group name with this account, you can set it for convenience.
                    |If not set, each time configuring project you need to set by `packageGroup = `
                """.trimMargin()

fun main() = runPrintErrorMessage {
    println("Working dir: ${getCurrentDir() ?: "unknown"}")

    val (sonatypeUsername, sonatypePassword, packageGroup) =
        (readFile("sonatype.txt")
            ?: run {
                writeFile("sonatype.txt", "\n")
                error(SONATYPE_TXT_TIPS)
            })
            .apply { check(count { it == '\n' } >= 1) { "Bad sonatype.txt. $SONATYPE_TXT_TIPS" } }
            .split('\n')
            .map { it.trim() }
            .let {
                if (it.size > 2) it
                else it + ""
            }


    println("Sonatype username: $sonatypeUsername")
    println("Sonatype password: $sonatypePassword")
    println("Sonatype package group: $packageGroup")

    val gpgPublic = readFileOrFail("keys.gpg.pub")
    println("GPG public key length is ${gpgPublic.length}")

    val gpgPrivate = readFileOrFail("keys.gpg")
    println("GPG private key length is ${gpgPrivate.length}")

    val data = PublicationCredentials(
        pgpPublicKey = gpgPublic,
        pgpPrivateKey = gpgPrivate,
        sonatypeUsername = sonatypeUsername,
        sonatypePassword = sonatypePassword,
        packageGroup = packageGroup,
    )

    val format = ProtoBuf

    val string = format.encodeToHexString(PublicationCredentials.serializer(), data)

    println("Your credentials is:")
    println()
    println(string)
    println()
    writeFile("credentials.txt", string)
    println("Saved as credentials.txt")
    println()
    println("This is not encrypted. Keep it safe.")
}

private inline fun runPrintErrorMessage(block: () -> Unit) {
    runCatching(block).onFailure { e ->
        val msg = e.message ?: e.cause?.message
        if (e is IllegalStateException && msg != null)
            println(msg)
        else e.printStackTrace()
    }
    getchar()
}

private fun readFileOrFail(path: String): String = readFile(path) ?: error("File $path does not exist")

private fun readFile(path: String): String? {
    val builder = StringBuilder()
    val file = fopen(path, "r") ?: return null

    try {
        memScoped {
            val readBufferLength = 64 * 1024
            val buffer = allocArray<ByteVar>(readBufferLength)
            var line = fgets(buffer, readBufferLength, file)?.toKString()
            while (line != null) {
                builder.append(line)
                fgets(buffer, readBufferLength, file)?.toKString().also { line = it }
            }
        }
    } finally {
        fclose(file)
    }

    return builder.toString()
}

private fun writeFile(path: String, text: String): Boolean {
    val file = fopen(path, "w") ?: return false
    try {
        memScoped {
            if (fputs(text, file) == EOF) return false
        }
    } finally {
        fclose(file)
    }
    return true
}

private fun getCurrentDir(): String? {
    memScoped {
        val byte = allocArray<ByteVar>(PATH_MAX)
        return getcwd(byte, PATH_MAX)?.toKString()
    }
}