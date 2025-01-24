package org.astroboy.myaccounts.data

import dev.whyoleg.cryptography.BinarySize.Companion.bytes
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.PBKDF2
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

@Serializable
data class Database(
    val name: String,
    val password: String = "",
    val accounts: Accounts = mapOf(),
) {
    @Transient
    // TODO: how to access filesDir here?
    private val file = File("$name.db")

    val isOpen get() = password.isNotEmpty()
    val isSaved: Boolean get() = TODO("implement")

    private suspend fun deriveKey(): AES.CBC.Key {
        val salt = CryptographyRandom.nextBytes(16)
        val provider = CryptographyProvider.Default
        val secretDerivation = provider.get(PBKDF2).secretDerivation(
            digest = SHA256,
            iterations = 480_000,
            outputSize = 32.bytes,
            salt = ByteString(appSalt),
        )

        val secret = secretDerivation.deriveSecret(password.toByteArray())
        val decoder = provider.get(AES.CBC).keyDecoder()
        return decoder.decodeFromByteStringBlocking(AES.Key.Format.RAW, secret)
    }

    fun create() {
    }
}
