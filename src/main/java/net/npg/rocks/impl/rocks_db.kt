package net.npg.rocks.impl

import net.npg.rocks.*
import org.rocksdb.*
import java.nio.file.Path
import org.rocksdb.ColumnFamilyHandle
import org.rocksdb.RocksDB
import org.rocksdb.RocksIterator

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
}
