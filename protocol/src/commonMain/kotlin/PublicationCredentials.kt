package net.mamoe.him188.maven.central.publish.protocol

import kotlinx.serialization.Serializable

@Serializable
class PublicationCredentials(
    val gpgPublicKey: String,
    val gpgPrivateKey: String,
    val sonatypeUsername: String,
    val sonatypePassword: String,
)