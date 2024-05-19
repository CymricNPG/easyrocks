package net.npg.rocks.impl.net.npg.rocks.impl

import net.npg.rocks.TableContext
import net.npg.rocks.impl.RocksDBService
import net.npg.rocks.impl.RocksDatabase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.nio.file.Path

class RocksDBServiceTest {

    @field:TempDir
    lateinit var tempFolder: Path


    private lateinit var rocksDBService: RocksDBService

    @BeforeEach
    fun setUp() {
        rocksDBService = RocksDBService()
    }

    @Test
    fun `test getDefaultDBOptions sets correct options`() {
        val options = rocksDBService.getDefaultDBOptions()
        assertTrue(options.createIfMissing())
        assertTrue(options.createMissingColumnFamilies())
        assertEquals(100 * 1024 * 1024, options.maxLogFileSize())
        assertEquals(5, options.keepLogFileNum())
    }

    @Test
    fun `test open initializes RocksDatabase correctly`() {

        val tableContext: TableContext<ByteArray, ByteArray> = mock {
            on { name } doReturn "testTable"
        }
        val tableContexts = listOf(tableContext)


        val rocksDatabase = rocksDBService.open(tempFolder, tableContexts)

        assertNotNull(rocksDatabase)
        assertTrue(rocksDatabase is RocksDatabase)
        assertDoesNotThrow { rocksDatabase.close() }
    }
}