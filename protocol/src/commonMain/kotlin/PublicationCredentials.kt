@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.him188.maven.central.publish.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public class PublicationCredentials(
    @ProtoNumber(1) public val gpgPublicKey: String,
    @ProtoNumber(2) public val gpgPrivateKey: String,
    @ProtoNumber(3) public val sonatypeUsername: String,
    @ProtoNumber(4) public val sonatypePassword: String,
)