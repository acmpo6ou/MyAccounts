package data

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.astroboy.myaccounts.data.Database
import org.astroboy.myaccounts.data.DatabaseRepo
import org.astroboy.myaccounts.data.GenerateSalt
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.junit.Before
import java.io.File
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
    fun `open database`() = runTest {
        copyDatabase()
        val database = Database("main", "123")

        val accounts = databaseRepo.openDatabase(database)
        assertEquals(TestData.accounts, accounts)
    }

    @Test
    fun `create database`() = runTest {
        assertFalse(File("$FILES_DIR_PATH/main.db").exists())

        val database = Database("main", "123", TestData.accounts)
        databaseRepo.createDatabase(database)

        val accounts = databaseRepo.openDatabase(Database("main", "123"))
        assertEquals(TestData.accounts, accounts)
    }

    @Test
    fun `delete database`() {
        copyDatabase()
        val file = File("$FILES_DIR_PATH/main.db")
        assertTrue(file.exists())

        databaseRepo.deleteDatabase(Database("main"))
        assertFalse(file.exists())
    }

    @Test
    fun `rename database`() {
        copyDatabase()
        val oldFile = File("$FILES_DIR_PATH/main.db")
        val newFile = File("$FILES_DIR_PATH/test.db")

        assertTrue(oldFile.exists())
        assertFalse(newFile.exists())

        databaseRepo.renameDatabase(Database("main"), "test")
        assertFalse(oldFile.exists())
        assertTrue(newFile.exists())
    }

    @Test
    fun `save database`() = runTest {
        copyDatabase()
        val oldDatabase = Database("main", "123", TestData.accounts)
        val newAccounts = mapOf(TestData.account.accountName to TestData.account)
        val newDatabase = Database("test", "321", newAccounts)

        databaseRepo.saveDatabase(oldDatabase, newDatabase)
        assertFalse(File("$FILES_DIR_PATH/main.db").exists())

        val accounts = databaseRepo.openDatabase(Database("test", "321"))
        assertEquals(newAccounts, accounts)
    }

    @Test
    fun `save database when its name didn't change`() = runTest {
        copyDatabase()
        val oldDatabase = Database("main", "123", TestData.accounts)
        val newAccounts = mapOf(TestData.account.accountName to TestData.account)
        val newDatabase = oldDatabase.copy(accounts = newAccounts)

        // For saveDatabase(), it's important to first delete old database and then create new one,
        // not the other way around. Because if the name of the database didn't change during saving,
        // the database file will be removed.
        databaseRepo.saveDatabase(oldDatabase, newDatabase)

        val accounts = databaseRepo.openDatabase(Database("main", "123"))
        assertEquals(newAccounts, accounts)
    }
}
