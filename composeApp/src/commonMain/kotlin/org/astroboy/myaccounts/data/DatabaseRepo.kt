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
import java.io.FileInputStream

class DatabaseRepo(
    private val filesDir: String,
    private val generateSalt: GenerateSalt,
) {

    private val Database.file get() = File("$filesDir/$name.db")

    // TODO: creation of a key is slow, cache it somewhere using KeyDataStore?
    //  getOrPut is pretty useful, use `password` as a key, don't use `salt`, for simplicity
    //  passwords shouldn't be reused for databases anyway
    suspend fun deriveKeyCipher(
        password: String,
        salt: ByteArray,
    ): AES.IvCipher {
        val provider = CryptographyProvider.Default
        val secretDerivation = provider.get(PBKDF2).secretDerivation(
            digest = SHA256,
            iterations = 480_000,
            outputSize = 32.bytes,
            salt = ByteString(salt),
        )

        val secret = secretDerivation.deriveSecret(password.toByteArray())
        val decoder = provider.get(AES.CBC).keyDecoder()
        return decoder.decodeFromByteStringBlocking(AES.Key.Format.RAW, secret).cipher()
    }

    suspend fun openDatabase(database: Database): Accounts {
        val salt = ByteArray(SALT_LENGTH)
        val token: ByteArray

        FileInputStream(database.file).use {
            it.read(salt)
            token = it.readBytes()
        }

        val json = deriveKeyCipher(database.password, salt)
            .decrypt(token)
            .decodeToString()

        return Json.decodeFromString(json)
    }

    fun closeDatabase(database: Database) {
        // TODO: remove key from cache
    }

    suspend fun createDatabase(database: Database) {
        val salt = generateSalt()
        val key = deriveKeyCipher(database.password, salt)

        val json = Json.encodeToString(database.accounts)
        val token = key.encrypt(json.toByteArray())

        database.file.createNewFile()
        database.file.writeBytes(salt + token)
    }

    fun deleteDatabase(database: Database) = database.file.delete()

    fun renameDatabase(
        database: Database,
        newName: String,
    ) = database.file.renameTo(database.file.parentFile / File(newName))

    suspend fun saveDatabase(
        oldDatabase: Database,
        newDatabase: Database,
    ) {
        deleteDatabase(oldDatabase)
        createDatabase(newDatabase)
    }
}

operator fun File.div(other: File) = File(this, other.path)
