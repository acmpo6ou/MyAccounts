package data

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.astroboy.myaccounts.data.Database
import org.astroboy.myaccounts.data.DatabaseRepo
import org.astroboy.myaccounts.data.GenerateSalt
import org.astroboy.myaccounts.data.SALT_LENGTH
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.junit.Before
import java.io.File
import java.io.FileInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalResourceApi::class)
class DatabaseRepoTest : BaseTest() {

    @Suppress("unused")
    private val filesDir = FILES_DIR_PATH

    @RelaxedMockK
    private lateinit var generateSalt: GenerateSalt

    @InjectMockKs
    private lateinit var databaseRepo: DatabaseRepo

    @Before
    fun setup() {
        every { generateSalt() } returns TestData.salt
    }

    private fun copyDatabase() {
        File("$RESOURCE_PATH/main.db").copyTo(File("$FILES_DIR_PATH/main.db"))
    }

    @Test
    fun `create database`() = runTest {
        val database = Database("main", "123", TestData.accounts)
        databaseRepo.createDatabase(database)

        val salt = ByteArray(SALT_LENGTH)
        val token: ByteArray

        FileInputStream(File(FILES_DIR_PATH, "main.db")).use {
            it.read(salt)
            token = it.readBytes()
        }

        val json = databaseRepo.deriveKeyCipher(database.password, salt)
            .decrypt(token)
            .decodeToString()

        assertEquals(TestData.JSON, json)
    }

    @Test
    fun `delete database`() {
        copyDatabase()
        val file = File("$FILES_DIR_PATH/main.db")
        assertTrue(file.exists())

        databaseRepo.deleteDatabase(Database("main"))
        assertFalse(file.exists())
    }
}
