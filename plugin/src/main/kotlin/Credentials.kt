package net.mamoe.him188.maven.central.publish.gradle

import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.him188.maven.central.publish.protocol.PublicationCredentials
import org.gradle.api.Project

internal object Credentials {
    private fun find(project: Project, propName: String): Any? {
        return project.findProperty(propName)
            ?: System.getProperty(propName, null)
            ?: System.getenv(propName)
    }

    fun findCredentials(project: Project): PublicationCredentials? {
        val info = find(project, "PUBLICATION_CREDENTIALS")
            ?: find(project, "PUBLICATION.CREDENTIALS")
            ?: find(project, "publication.credentials")
            ?: return null

        @Suppress("EXPERIMENTAL_API_USAGE")
        return ProtoBuf.decodeFromHexString(PublicationCredentials.serializer(), info.toString())
    }

    fun check(credentials: PublicationCredentials) {
        check(credentials.pgpPublicKey.trimStart().startsWith(PGP_PUBLIC_KEY_BEGIN)) { "Invalid PGP public key" }
        check(credentials.pgpPublicKey.trimEnd().endsWith(PGP_PUBLIC_KEY_END)) { "Invalid PGP public key" }

        check(credentials.pgpPrivateKey.trimStart().startsWith(PGP_PRIVATE_KEY_BEGIN)) { "Invalid PGP private key" }
        check(credentials.pgpPrivateKey.trimEnd().endsWith(PGP_PRIVATE_KEY_END)) { "Invalid PGP private key" }

        check(credentials.sonatypePassword.isNotBlank()) { "Sonatype password must not be empty." }
        check(credentials.sonatypeUsername.isNotBlank()) { "Sonatype username must not be empty." }
    }

    const val PGP_PUBLIC_KEY_BEGIN = "-----BEGIN PGP PUBLIC KEY BLOCK-----"
    const val PGP_PUBLIC_KEY_END = "-----END PGP PUBLIC KEY BLOCK-----"
    const val PGP_PRIVATE_KEY_BEGIN = "-----BEGIN PGP PRIVATE KEY BLOCK-----"
    const val PGP_PRIVATE_KEY_END = "-----END PGP PRIVATE KEY BLOCK-----"
}