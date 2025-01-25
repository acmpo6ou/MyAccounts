package org.astroboy.myaccounts.data

import dev.whyoleg.cryptography.BinarySize.Companion.bytes
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.PBKDF2
import dev.whyoleg.cryptography.algorithms.SHA256
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class DatabaseRepo(
    private val filesDir: String,
    private val generateSalt: GenerateSalt,
) {

    private val Database.file get() = File("$filesDir/$name.db")

    // TODO: creation of a key is slow, cache it somewhere using KeyDataStore?
    private suspend fun deriveKey(
        password: String,
        salt: ByteArray,
    ): AES.CBC.Key {
        val provider = CryptographyProvider.Default
        val secretDerivation = provider.get(PBKDF2).secretDerivation(
            digest = SHA256,
            iterations = 480_000,
            outputSize = 32.bytes,
            salt = ByteString(salt),
        )

        val secret = secretDerivation.deriveSecret(password.toByteArray())
        val decoder = provider.get(AES.CBC).keyDecoder()
        return decoder.decodeFromByteStringBlocking(AES.Key.Format.RAW, secret)
    }

    suspend fun createDatabase(database: Database) {
        val salt = generateSalt()
        val key = deriveKey(database.password, salt)

        val json = Json.encodeToString(database.accounts)
        val token = key.cipher().encrypt(json.toByteArray())

        database.file.createNewFile()
        database.file.writeBytes(salt + token)
    }
}
