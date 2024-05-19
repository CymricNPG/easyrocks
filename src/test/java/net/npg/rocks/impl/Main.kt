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

import java.nio.file.Path


fun main() {
    val dbService = RocksDBService()
    val tableContextData = TableContextData<String, String>("test",
        { s -> s.toByteArray(Charsets.UTF_8) },
        { s -> s.toString(Charsets.UTF_8) },
        { s -> s.toByteArray(Charsets.UTF_8) },
        { s -> s.toString(Charsets.UTF_8) })

    dbService.open(Path.of("test"), listOf(tableContextData)).use { db ->
        val table = db.getTable(tableContextData)
        for (i in 1..100) {
            val key = "Hossa" + i
            val value = "bla" + i
            table.put(key, value)
        }
        table.getAll().forEach { (k, v) -> println("$k -> $v") }
    }

}