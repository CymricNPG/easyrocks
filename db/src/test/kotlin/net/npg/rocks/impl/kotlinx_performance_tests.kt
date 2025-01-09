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

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

class KotlinXJSONPerformanceTest {

    private val JSON = kotlinx.serialization.json.Json { encodeDefaults = true }

    @field:TempDir
    lateinit var tempFolder: Path


    private lateinit var rocksDBService: RocksDBService

    @BeforeTest
    fun setUp() {
        rocksDBService = RocksDBService()
    }

    @Test
    fun serializeDeserialize() {
        val aList = createTestData()
        for (i in 0..10) {
            val serialized = aList.map { JSON.encodeToString(it) }.toList()
            val deserialized: List<A> = serialized.map { JSON.decodeFromString(it) }
            assertContentEquals(aList, deserialized)
        }
        speedTestSerialize(aList)
    }

    private fun createTestData(): List<A> {
        val random = Random
        val aList = (0..100000).map { i ->
            A(
                a = random.nextInt(),
                b = "random-string-$i",
                c = B(random.nextDouble(), random.nextDouble(), random.nextDouble()),
                d = B(random.nextDouble(), random.nextDouble(), random.nextDouble()),
                e = doubleArrayOf(random.nextDouble(), random.nextDouble(), random.nextDouble())
            )
        }
        return aList
    }

    private fun speedTestSerialize(aList: List<A>) {
        val start = System.nanoTime()
        val serialized = aList.map { JSON.encodeToString(it) }.toList()
        val deserialized: List<A> = serialized.map { JSON.decodeFromString(it) }
        val end = System.nanoTime()
        assertNotNull(deserialized)
        println("Time (serialize/deserialize): " + (end - start) / 1000.0 / 1000.0 + " ms")
    }
//
//    @RepeatedTest(2)
//    fun rocks() {
//        preRunRocks()
//        val context = StringKeyJsonValueContext("testTable", A::class)
//        val contexts = listOf(context)
//        val aList = createTestData()
//        var start = System.nanoTime()
//        rocksDBService.open(tempFolder, contexts).use { db ->
//            val table = db.getTable(context)
//            aList.forEach { a -> table.put(a.b, a) }
//        }
//        val end = System.nanoTime()
//        println("Write: " + (end - start) / 1000.0 / 1000.0)
//        val start2 = System.nanoTime()
//        rocksDBService.open(tempFolder, contexts).use { db ->
//            val table = db.getTable(context)
//            val result = table.getAll()
//            assertNotNull(result)
//        }
//        val end2 = System.nanoTime()
//        println("Read: " + (end2 - start2) / 1000.0 / 1000.0)
//    }
//
//    private fun preRunRocks() {
//        val context = StringKeyJsonValueContext("testTable", A::class)
//        val contexts = listOf(context)
//        val aList = createTestData()
//
//        rocksDBService.open(tempFolder, contexts).use { db ->
//            val table = db.getTable(context)
//            aList.forEach { a -> table.put(a.b, a) }
//            val result = table.getAll()
//            assertNotNull(result)
//        }
//    }
}
