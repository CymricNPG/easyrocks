/*
 *   Easy RocksDB Interface Playground
 *     Copyright (C) 2024 Roland Spatzenegger
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.npg.rocks.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals


class JsonTest {

    @field:TempDir
    lateinit var tempFolder: Path


    private lateinit var rocksDBService: RocksDBService

    @BeforeEach
    fun setUp() {
        rocksDBService = RocksDBService()
    }

    @Test
    fun `roundtrip test`() {
        val context = StringKeyJsonValueContext("testTable", TestClass::class)
        val contexts = listOf(context)

        rocksDBService.open(tempFolder, contexts).use { db ->
            val table = db.getTable(context)
            val obj = TestClass(34, "FASEL", intArrayOf(2, 6, 87, 9))
            table.put("Bla", obj)
            val result = table.getAll().values
            assertEquals(1, result.size)
            assertEquals(obj, result.first())
        }
    }

    @Test
    fun testSerializationAndDeserialization() {
        val keyClass = String::class.java
        val valueClass = String::class.java
        val context = JsonKeyValueContext("test", keyClass, valueClass)

        val key = "hello"
        val value = "world"

        // Serialize the key and value
        val serializedKey = context.serializeKey(key)
        val serializedValue = context.serializeValue(value)

        // Deserialize the key and value
        val deserializedKey = context.deserializeKey(serializedKey)
        val deserializedValue = context.deserializeValue(serializedValue)

        assertEquals(key, deserializedKey)
        assertEquals(value, deserializedValue)
    }
}

data class TestClass(val a: Int, val b: String, val c: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestClass

        if (a != other.a) return false
        if (b != other.b) return false
        if (!c.contentEquals(other.c)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = a
        result = 31 * result + b.hashCode()
        result = 31 * result + c.contentHashCode()
        return result
    }
}