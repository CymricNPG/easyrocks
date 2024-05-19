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

package net.npg.rocks

import java.nio.file.Path

interface DBService {
    fun open(dbPath: Path, tableContexts: List<TableContext<*, *>>): Database
}

interface Database : AutoCloseable {
    fun <K, V> getTable(context: TableContext<K, V>): Table<K, V>
}

interface Table<K, V> {

    fun put(key: K, value: V)

    fun getAll(): Map<K, V>
}

class TableContext<K, V>(
    val name: String,
    val serializeKey: (K) -> ByteArray,
    val deserializeKey: (ByteArray) -> K,
    val serializeValue: (V) -> ByteArray,
    val deserializeValue: (ByteArray) -> V
) {
}


