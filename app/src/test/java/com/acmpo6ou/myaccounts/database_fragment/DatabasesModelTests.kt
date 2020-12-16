/*
 * Copyright (c) 2020. Kolvakh Bohdan
 * This file is part of MyAccounts.
 *
 * MyAccounts is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyAccounts is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.acmpo6ou.myaccounts.database_fragment

import com.acmpo6ou.myaccounts.core.Account
import com.acmpo6ou.myaccounts.core.Database
import com.acmpo6ou.myaccounts.core.DatabasesModel
import com.acmpo6ou.myaccounts.getDatabaseMap
import com.github.javafaker.Faker
import com.macasaet.fernet.StringValidator
import com.macasaet.fernet.Token
import com.macasaet.fernet.Validator
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.temporal.TemporalAmount

class DatabasesTests {
    private val faker = Faker()

    @Test
    fun `Database class should have isOpen property set to false when password is null`(){
        // we didn't  pass the password so it will be null by default
        val database = Database(faker.name().toString())

        // if password is null then database is closed
        assertFalse(
                "Password of Database is null but isOpen is not false!",
                database.isOpen,
        )
    }

    @Test
    fun `Database class should have isOpen property set to true when password is NOT null`(){
        // we passed the password, so it is not null
        val database = Database(faker.name().toString(), "Some password")

        // when password is not null database is opened
        assertTrue(
                "Password of Database is NOT null but isOpen is false!",
                database.isOpen,
        )
    }
}

class DatabasesModelTests {
    private val faker = Faker()

    // this is where DatabasesModel will create delete and edit databases during testing
    // /dev/shm/ is a fake in-memory file system
    private val accountsDir = "/dev/shm/accounts/"
    private val SRC_DIR = "$accountsDir/src/"

    lateinit var model: DatabasesModel
    lateinit var salt: ByteArray
    private val jsonDatabase =
            "{\"gmail\":{\"account\":\"gmail\",\"name\":\"Tom\",\"email\":"+
            "\"tom@gmail.com\",\"password\":\"123\",\"date\":\"01.01.1990\","+
            "\"comment\":\"My gmail account.\"}}"


    /**
     * This method creates empty src folder in a fake file system, it ensures that
     * directory will be empty.
     */
    @Before
    fun setUpScrFolder(){
        val srcFolder = File(SRC_DIR)
        val accountsFolder = File(accountsDir)

        // here we delete accounts folder if it already exists to ensure that it will
        // be empty as is needed for our tests
        if(accountsFolder.exists()){
            accountsFolder.deleteRecursively()
        }

        // then we create accounts folder and src inside it
        srcFolder.mkdirs()
    }

    @Before
    fun setUp(){
        model = DatabasesModel(SRC_DIR)
        salt = "0123456789abcdef".toByteArray() // 16 bytes of salt
    }

    /**
     * This is a helper method that will copy our test  databases from sampledata folder to
     * the fake file system.
     *
     * @param[name] name of the database that we want to copy to the fake file system
     */
    private fun copyDatabase(name: String ="database"){
        // this are were we want to copy database .bin and .db files
        val binDestination = File("$SRC_DIR$name.bin")
        val dbDestination = File("$SRC_DIR$name.db")

        // this are the database files that we want to copy
        val binFile = File("sampledata/src/$name.bin")
        val dbFile = File("sampledata/src/$name.db")

        // here we copy database files to the fake file system
        binFile.copyTo(binDestination)
        dbFile.copyTo(dbDestination)
    }

    /**
     * This is a helper method that simply creates empty database.
     *
     * It creates database with name `main` and password `123`.
     * Also it uses createDatabase method of DatabasesModel class for this.
     */
    private fun createEmptyDatabase(){
        // instantiate empty database with name, password and salt
        val database = Database(
                "main",
                "123",
                salt
        )
        model.createDatabase(database)
    }

    /**
     * This method decrypts given string.
     *
     * @param[string] string to decrypt.
     * @param[password] password for decryption.
     * @param[salt] salt for decryption.
     * @return decrypted string.
     */
    private fun decryptStr(string: String, password: String, salt: ByteArray): String{
        val key = model.deriveKey(password, salt)
        val validator: Validator<String> = object : StringValidator {
            // this checks whether our encrypted json string is expired or not
            // in our app we don't care about expiration so we return Instant.MAX.epochSecond
            override fun getTimeToLive(): TemporalAmount {
                return Duration.ofSeconds(Instant.MAX.epochSecond)
            }
        }
        val token = Token.fromString(string)
        return token.validateAndDecrypt(key, validator)
    }

    /**
     * This helper method encrypts given map using [password] and [salt].
     *
     * @param[map] database map to encrypt.
     * @param[password] password for encryption.
     * @param[salt] salt for encryption.
     * @return encrypted json string of database map.
     */
    private fun encryptStr(map: Map<String, Account>, password: String, salt: ByteArray): String{
        val key = model.deriveKey(password, salt)
        val data = model.dumps(map)
        val token = Token.generate(key, data)
        return token.serialise()
    }
    @Test
    fun `dumps should return empty string when passed empty map`(){
        val dumpStr = model.dumps(mapOf())
        assertTrue(dumpStr.isEmpty())
    }

    @Test
    fun `dumps should return serialized string when passed non empty map`(){
        // create database with account that we will serialize
        val database = getDatabaseMap()

        // serialize database and check resulting json string
        val dumpStr = model.dumps(database)
        val expectedStr = jsonDatabase
        assertEquals(
                "Incorrect serialization! dumps method",
                expectedStr,
                dumpStr
        )
    }

    @Test
    fun `loads should return empty map when passed empty string`(){
        val loadMap = model.loads("")
        assertTrue(loadMap.isEmpty())
    }

    @Test
    fun `loads should return non empty map when passed non empty string`(){
        // load database map from json string
        val map = model.loads(jsonDatabase)

        // get database map that we expect
        val expectedMap = getDatabaseMap()

        assertEquals(
                "Incorrect deserialization! loads method",
                expectedMap,
                map,
        )
    }
    @Test
    fun `encryptDatabase should return encrypted json string when given Database`(){
        // get database map
        val dataMap = getDatabaseMap()

        // create database
        val database = Database(
                faker.name().toString(),
                faker.lorem().sentence(),
                salt,
                dataMap
        )

        // get encrypted json string
        val jsonStr = model.encryptDatabase(database)

        // here we decrypt the json string using salt and password we defined earlier
        // to check if it were encrypted correctly
        val data = decryptStr(jsonStr, database.password!!, database.salt!!)

        assertEquals(
                "encryptDatabase has returned incorrectly encrypted json string!",
                jsonDatabase,
                data
        )
    }

    @Test
    fun `createDatabase should create salt file given Database instance`(){
        createEmptyDatabase()

        // this is a salt file that createDatabase should create for us
        val actualBin = File("$SRC_DIR/main.bin").readBytes()

        assertEquals(
                "createDatabase created incorrect salt file!",
                String(salt),
                String(actualBin)
        )
    }

    @Test
    fun `createDatabase should create database file given Database instance`(){
        // create database using createDatabase
        val database = Database(
                "main",
                "123",
                salt,
                getDatabaseMap()
        )
        model.createDatabase(database)

        // this is a .db file that createDatabase should create for us
        val actualDb = File("$SRC_DIR/main.db").readBytes()

        // here we decrypt data saved to .db file to check that it was encrypted correctly
        val data = decryptStr(String(actualDb), "123", salt)
        assertEquals(
                "createDatabase creates incorrectly encrypted database!",
                jsonDatabase,
                data
        )
    }

    @Test
    fun `deleteDatabase removes db and bin files from disk`(){
        // create empty database so that we can delete it using deleteDatabase
        createEmptyDatabase()

        model.deleteDatabase("main")

        // files that should be deleted
        val binFile = File("$SRC_DIR/main.bin")
        val dbFile = File("$SRC_DIR/main.db")

        assertFalse(
                "deleteDatabase doesn't delete .bin file",
                binFile.exists()
        )
        assertFalse(
                "deleteDatabase doesn't delete .db file",
                dbFile.exists()
        )
    }

    @Test
    fun `decryptDatabase should return decrypted and deserialized map given string`(){
        // encrypt database so we can check how decryptDatabase will decrypt it
        val expectedMap = getDatabaseMap()
        val encryptedJson = encryptStr(
                expectedMap,
                "123",
                salt
        )

        val map = model.decryptDatabase(encryptedJson, "123", salt)
        assertEquals(
                "Incorrect decryption! decryptDatabase method",
                expectedMap,
                map
        )
    }

    @Test
    fun `openDatabase should return Database instance with non empty data property`(){
        // here we copy `main` database to the fake file system so that we can open it later
        copyDatabase("main")

        // create corresponding Database instance
        val db = Database(
                "main",
                "123",
                salt
        )

        val actualDatabase = model.openDatabase(db)
        val expectedDatabase = Database(
                "main",
                "123",
                salt,
                getDatabaseMap()
        )

        assertEquals(
                "openDatabase returns incorrect database!",
                expectedDatabase,
                actualDatabase
        )
    }

    /**
     * Helper method used by saveDatabase test to create old database and to call saveDatabase
     * passing through new database.
     */
    private fun setUpSaveDatabase(){
        // this database will be deleted by saveDatabase
        val db = Database(
                "test",
                "123",
                salt
        )
        model.createDatabase(db)

        // this database will be created by saveDatabase
        val newDb = Database(
                "test2",
                "321",
                salt.reversedArray(),
                getDatabaseMap()
        )

        // save newDb deleting db
        model.saveDatabase("test", newDb)
    }

    @Test
    fun `saveDatabase should delete files of old database`(){
        setUpSaveDatabase()

        // check that there is no longer test.db and test.bin files
        val oldDb = File("$SRC_DIR/test.db")
        val oldBin = File("$SRC_DIR/test.bin")

        assertFalse(
                ".db file of old database is not deleted by saveDatabase method!",
                oldDb.exists()
        )
        assertFalse(
                ".bin file of old database is not deleted by saveDatabase method!",
                oldBin.exists()
        )
    }

    @Test
    fun `saveDatabase should create new, non empty database file`(){
        setUpSaveDatabase()

        // this is a .db file that saveDatabase should create for us
        val actualDb = File("$SRC_DIR/test2.db").readBytes()

        // created .db file must not be empty
        assertTrue(
                "saveDatabase created empty .db file!",
                actualDb.isNotEmpty()
        )

        // here we decrypt data saved to .db file to check that it was encrypted correctly
        val data = decryptStr(String(actualDb), "321", salt.reversedArray())
        assertEquals(
                "saveDatabase creates incorrect database!",
                jsonDatabase,
                data
        )

    }

    @Test
    fun `saveDatabase should create new, non empty salt file`(){
        setUpSaveDatabase()

        // this is a .bin file that saveDatabase should create for us
        val actualBin = File("$SRC_DIR/test2.bin").readBytes()

        // created .bin file must not be empty
        assertTrue(
                "saveDatabase created empty .bin file!",
                actualBin.isNotEmpty()
        )

        // .bin file must have appropriate content (i.e. salt)
        assertEquals(
                "saveDatabase created .bin file with incorrect salt!",
                String(salt.reversedArray()),
                String(actualBin)
        )
    }

    @Test
    fun `getDatabases should return list of Databases that reside in SRC_DIR`(){
        // first we copy some database to our fake file system
        copyDatabase("main")
        copyDatabase("crypt")
        copyDatabase("database")

        // then we get databases and check the result
        val databases = model.getDatabases()
        val expectedDatabases = listOf(
                Database("database"),
                Database("crypt"),
                Database("main"),
        )
        assertEquals(
                "getDatabases returns incorrect list of Databases!",
                expectedDatabases,
                databases
        )
    }

    @Test
    fun `exportDatabase should export database tar to given location`(){
        copyDatabase("main")

        // export database `main` to the fake file system
        val destination = accountsDir
        model.exportDatabase("main", destination)

        // check that database tar file was exported properly
        val exportedTar = String(
                File("$destination/main.tar").readBytes()
        )
        val expectedDb = String(
                File("$SRC_DIR/main.db").readBytes()
        )
        val expectedBin = String(
                File("$SRC_DIR/main.bin").readBytes()
        )

        // check that files are present and they reside in `src` folder
        assertTrue(
                "exportDatabase incorrect export: tar file doesn't contain .db file!",
                "src/main.db" in exportedTar
        )
        assertTrue(
                "exportDatabase incorrect export: tar file doesn't contain .bin file!",
                "src/main.bin" in exportedTar
        )

        // check that files have appropriate content
        assertTrue(
                "exportDatabase incorrect export: content of .db file is incorrect!",
                expectedDb in exportedTar
        )
        assertTrue(
                "exportDatabase incorrect export: content of .bin file is incorrect!",
                expectedBin in exportedTar
        )
    }

    @Test
    fun `importDatabase should extract given tar file to src folder`(){
        // import database
        model.importDatabase("sampledata/tar/main.tar")

        // check that all database files are imported correctly
        val expectedBin = String(salt)
        val expectedDb = String(
                File("sampledata/src/main.db").readBytes()
        )

        val actualBin = String(
                File("$SRC_DIR/main.bin").readBytes()
        )
        val actualDb = String(
                File("$SRC_DIR/main.db").readBytes()
        )

        assertEquals(
                "importDatabase incorrectly imported .db file!",
                expectedDb,
                actualDb
        )
        assertEquals(
                "importDatabase incorrectly imported .bin file!",
                expectedBin,
                actualBin
        )
    }

    @Test
    fun `importDatabase should extract database files only from given tar file`(){
        model.importDatabase("sampledata/tar/main.tar")

        // there is no other files in parent of `src` folder
        val srcParent = File(accountsDir)
        val filesList = srcParent.list()

        assertEquals(
                "importDatabase must extract only .db and .bin files from given tar!",
                1, // there must be only one directory – `src`
                filesList.size
        )
        assertEquals(
                "importDatabase must extract only .db and .bin files from given tar!",
                "src", // the only directory must be `src`
                filesList.first()
        )
    }

    private lateinit var database: Database
    private lateinit var databaseJson: String
    fun setupDatabaseAndJson(){
        database = Database(
            faker.name().toString(),
            faker.lorem().sentence(),
            salt,
            getDatabaseMap(),
            1
        )
        databaseJson =
            "{\"name\":\"${database.name}\",\"password\":\"${database.password}\"" +
            ",\"salt\":${Json.encodeToString(salt)},\"data\":$jsonDatabase,\"index\":1}"
    }
    @Test
    fun `dumpDatabase should serialize given Database`(){
        setupDatabaseAndJson()
        val actualJson = model.dumpDatabase(database)
        assertEquals(databaseJson, actualJson)
    }

    @Test
    fun `loadDatabase should deserialize given database json string`(){
        setupDatabaseAndJson()
        val actualDatabase = model.loadDatabase(databaseJson)
        assertEquals(database.toString(), actualDatabase.toString())
    }
}