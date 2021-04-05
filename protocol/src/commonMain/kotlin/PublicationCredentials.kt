@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.him188.maven.central.publish.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class PublicationCredentials(
    @ProtoNumber(1) val pgpPublicKey: String,
    @ProtoNumber(2) val pgpPrivateKey: String,
    @ProtoNumber(3) val sonatypeUsername: String,
    @ProtoNumber(4) val sonatypePassword: String,
) {
    override fun toString(): String {
        return """PublicationCredentials(
                 |  pgpPublicKey='$pgpPublicKey', 
                 |  pgpPrivateKey='$pgpPrivateKey', 
                 |  sonatypeUsername='$sonatypeUsername', 
                 |  sonatypePassword='$sonatypePassword'
                 |)""".trimMargin()
    }
}
