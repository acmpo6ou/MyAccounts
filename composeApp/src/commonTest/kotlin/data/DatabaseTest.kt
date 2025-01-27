package data

import org.astroboy.myaccounts.data.Database
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatabaseTest {

    @Test
    fun `is open`() {
        assertFalse(Database("main").isOpen)
        assertTrue(Database("main", "123").isOpen)
    }
}
