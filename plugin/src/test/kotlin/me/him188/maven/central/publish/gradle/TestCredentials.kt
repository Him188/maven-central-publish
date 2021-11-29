package me.him188.maven.central.publish.gradle

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.protobuf.ProtoBuf
import me.him188.maven.central.publish.protocol.PublicationCredentials
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import me.him188.maven.central.publish.gradle.Credentials.check as credentialsCheck

val credentialsHex = TestCredentials::class.java.classLoader.getResource("credentials.txt")!!.readText()

@OptIn(ExperimentalSerializationApi::class)
val credentials = ProtoBuf.decodeFromHexString<PublicationCredentials>(credentialsHex)

internal class TestCredentials {
    @Test
    fun `can check valid credentials`() {
        credentialsCheck(credentials)
    }

    @Test
    fun `can check invalid credentials`() {
        assertFailsWith<IllegalStateException> { credentialsCheck(credentials.copy(pgpPublicKey = "")) }
        assertFailsWith<IllegalStateException> { credentialsCheck(credentials.copy(pgpPrivateKey = "")) }
        assertFailsWith<IllegalStateException> { credentialsCheck(credentials.copy(sonatypeUsername = "")) }
        assertFailsWith<IllegalStateException> { credentialsCheck(credentials.copy(sonatypePassword = "")) }

        assertFailsWith<IllegalStateException> { credentialsCheck(credentials.copy(pgpPublicKey = "a")) }
        assertFailsWith<IllegalStateException> { credentialsCheck(credentials.copy(pgpPrivateKey = "b")) }

        assertFailsWith<IllegalStateException> { credentialsCheck(credentials.copy(pgpPublicKey = Credentials.PGP_PUBLIC_KEY_BEGIN)) }
        assertFailsWith<IllegalStateException> { credentialsCheck(credentials.copy(pgpPrivateKey = Credentials.PGP_PRIVATE_KEY_BEGIN)) }

        assertFailsWith<IllegalStateException> { credentialsCheck(credentials.copy(pgpPublicKey = Credentials.PGP_PUBLIC_KEY_END)) }
        assertFailsWith<IllegalStateException> { credentialsCheck(credentials.copy(pgpPrivateKey = Credentials.PGP_PRIVATE_KEY_END)) }
    }
}