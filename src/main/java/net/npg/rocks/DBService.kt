package net.npg.rocks

import org.rocksdb.*
import java.nio.file.Path

class DBService {
    init {
        RocksDB.loadLibrary()
    }

    fun open(dbPath: Path, tableContexts: List<TableContext<*,*>>): Database {
        require(tableContexts.isNotEmpty())

        val cfOpts = ColumnFamilyOptions().optimizeUniversalStyleCompaction()
        val cfDescriptors = mutableListOf<ColumnFamilyDescriptor>()
        cfDescriptors.add(ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts))

        val tables = mutableListOf<Table<*,*>>()
        tableContexts.map { Table(it, cfOpts) }.forEach { tables += it }
        tables.forEach { cfDescriptors += it.cfDescriptor }

        val columnFamilyHandleList: List<ColumnFamilyHandle> = ArrayList()
        val dbOptions = DBOptions()
            .setCreateIfMissing(true)
            .setCreateMissingColumnFamilies(true)
            // Set the maximum size of each log file (in bytes)
            .setMaxLogFileSize(100 * 1024 * 1024) // For example, 100 MB
            // Set the number of log files to keep
            .setKeepLogFileNum(5)

        val rocksDB = RocksDB.open(
            dbOptions,
            dbPath.toAbsolutePath().toString(),
            cfDescriptors,
            columnFamilyHandleList
        )
        val database = Database(rocksDB, dbOptions, cfOpts, tables)
        tables.forEach {
            val foundHandle = columnFamilyHandleList.find { handle -> handle.name.toString(Charsets.UTF_8) == it.context.name }
            if (foundHandle != null) {
                it.handle = foundHandle
                it.db = database
            } else {
                error("Cannot find a table with name:" + it.context.name)
            }
        }

        return database
    }

}