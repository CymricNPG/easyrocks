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

import net.npg.rocks.DBService
import net.npg.rocks.Database
import net.npg.rocks.Table
import net.npg.rocks.TableContext
import org.rocksdb.*
import java.nio.file.Path

class RocksDBService : DBService {
    init {
        RocksDB.loadLibrary()
    }

    override fun open(dbPath: Path, tableContexts: List<TableContext<*, *>>): Database {
        require(tableContexts.isNotEmpty())

        val cfOpts = ColumnFamilyOptions().optimizeUniversalStyleCompaction()
        val cfDescriptors = mutableListOf<ColumnFamilyDescriptor>()
        cfDescriptors.add(ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts))
        tableContexts.forEach { cfDescriptors += ColumnFamilyDescriptor(it.name.toByteArray(Charsets.UTF_8), cfOpts) }
        val columnFamilyHandles: List<ColumnFamilyHandle> = ArrayList()
        val dbOptions = getDefaultDBOptions()

        val rocksDB = RocksDB.open(
            dbOptions,
            dbPath.toAbsolutePath().toString(),
            cfDescriptors,
            columnFamilyHandles
        )
        return RocksDatabase(rocksDB, dbOptions, cfOpts, tableContexts, columnFamilyHandles)
    }


    fun getDefaultDBOptions(): DBOptions {
        return DBOptions()
            .setCreateIfMissing(true)
            .setCreateMissingColumnFamilies(true)
            // Set the maximum size of each log file (in bytes)
            .setMaxLogFileSize(100 * 1024 * 1024) // For example, 100 MB
            // Set the number of log files to keep
            .setKeepLogFileNum(5)
    }

}


class RocksDatabase(
    private val db: RocksDB,
    private val dbOptions: DBOptions,
    private val columnFamilyOptions: ColumnFamilyOptions,
    private val tables: Map<TableContext<*, *>, RocksTable<*, *>>
) : Database {

    constructor(
        db: RocksDB,
        dbOptions: DBOptions,
        columnFamilyOptions: ColumnFamilyOptions,
        tableContexts: List<TableContext<*, *>>,
        columnFamilyHandles: List<ColumnFamilyHandle>
    ) : this(
        db,
        dbOptions,
        columnFamilyOptions,
        createTables(tableContexts, columnFamilyHandles, db),
    )

    companion object {
        fun createTables(tableContexts: List<TableContext<*, *>>, columnFamilyHandles: List<ColumnFamilyHandle>, db: RocksDB): Map<TableContext<*, *>, RocksTable<*, *>> {
            return tableContexts.map {
                val foundHandle = columnFamilyHandles.find { handle -> handle.name.toString(Charsets.UTF_8) == it.name }
                if (foundHandle == null) {
                    error("Cannot find a table with name:" + it.name)
                }
                RocksTable(it, foundHandle, db)
            }.associateBy({ it.context }, { it })
        }
    }

    override fun close() {
        tables.values.forEach { it.handle.close() }
        db.close()
        dbOptions.close()
        columnFamilyOptions.close()
    }

    override fun <K, V> getTable(context: TableContext<K, V>): Table<K, V> {
        return tables[context] as Table<K, V>
    }

}

class RocksTable<K, V>(val context: TableContext<K, V>, val handle: ColumnFamilyHandle, val db: RocksDB) : Table<K, V> {

    override fun put(key: K, value: V) {
        db.put(handle, context.serializeKey(key), context.serializeValue(value))
    }

    override fun getAll(): Map<K, V> {
        val map = mutableMapOf<K, V>()
        val iterator: RocksIterator = db.newIterator(handle)
        iterator.seekToFirst()
        while (iterator.isValid()) {
            val key = context.deserializeKey(iterator.key())
            val value = context.deserializeValue(iterator.value())
            map[key] = value
            iterator.next()
        }
        return map
    }

    override fun delete(key: K): Boolean {
        db.delete(handle, context.serializeKey(key))
        return true
    }
}


class TableContextData<K, V>(
    override val name: String,
    override val serializeKey: (K) -> ByteArray,
    override val deserializeKey: (ByteArray) -> K,
    override val serializeValue: (V) -> ByteArray,
    override val deserializeValue: (ByteArray) -> V
) : TableContext<K, V> {
}
