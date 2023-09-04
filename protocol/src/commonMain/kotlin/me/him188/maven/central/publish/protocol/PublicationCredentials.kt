package me.him188.maven.central.publish.protocol

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Suppress("EXPERIMENTAL_API_USAGE")
@Serializable
data class PublicationCredentials @OptIn(ExperimentalSerializationApi::class) constructor(
    @ProtoNumber(1) val gpgPublicKey: String,
    @ProtoNumber(2) val gpgPrivateKey: String,
    @ProtoNumber(3) val sonatypeUsername: String,
    @ProtoNumber(4) val sonatypePassword: String,
) {
    override fun toString(): String {
        return """PublicationCredentials(
                 |  gpgPublicKey='$gpgPublicKey', 
                 |  gpgPrivateKey='$gpgPrivateKey', 
                 |  sonatypeUsername='$sonatypeUsername', 
                 |  sonatypePassword='$sonatypePassword',
                 |)""".trimMargin()
    }
}