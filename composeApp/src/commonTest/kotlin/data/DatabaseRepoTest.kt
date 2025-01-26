package data

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import myaccounts.composeapp.generated.resources.Res
import org.astroboy.myaccounts.data.Database
import org.astroboy.myaccounts.data.DatabaseRepo
import org.astroboy.myaccounts.data.GenerateSalt
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.junit.Before
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalResourceApi::class)
class DatabaseRepoTest : BaseTest() {

    private val filesDir = FILES_DIR_PATH

    @RelaxedMockK
    private lateinit var generateSalt: GenerateSalt

    @InjectMockKs
    private lateinit var databaseRepo: DatabaseRepo

    @Before
    fun setup() {
        every { generateSalt() } returns salt
    }

    @Test
    fun `create database`() = runTest {
        val database = Database("main", "123", accounts)
        databaseRepo.createDatabase(database)

        val actual = File(FILES_DIR_PATH, "main.db")

        // check if the file even exists,
        // because assertContentEquals succeeds if both arrays are null
        assertTrue(actual.exists())

        val expected = File("$RESOURCE_PATH/main.db").readBytes()
        assertContentEquals(expected, actual.readBytes())
    }
}
