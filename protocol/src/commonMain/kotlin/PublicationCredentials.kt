package net.mamoe.him188.maven.central.publish.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class PublicationCredentials(
    val gpgPublicKey: String,
    val gpgPrivateKey: String,
    val sonatypeUsername: String,
    val sonatypePassword: String,
) {
    companion object {
        private val json = Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }

        fun loadFrom(string: String): PublicationCredentials {
            return json.decodeFromString(serializer(), string)
        }
    }
}