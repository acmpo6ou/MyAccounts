package data

import io.mockk.MockKAnnotations
import org.junit.Before
import java.io.File

const val FILES_DIR_PATH = "/tmp/MyAccounts/"
const val RESOURCE_PATH = "src/commonTest/composeResources/files/"

abstract class BaseTest {

    @Before
    fun baseSetup() {
        setupFilesDir()
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    private fun setupFilesDir() = File(FILES_DIR_PATH).run {
        deleteRecursively()
        mkdir()
    }
}
