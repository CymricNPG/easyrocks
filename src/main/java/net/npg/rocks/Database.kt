package net.npg.rocks

import org.rocksdb.ColumnFamilyOptions
import org.rocksdb.DBOptions
import org.rocksdb.RocksDB

class Database(
    val db: RocksDB,
    val dbOptions: DBOptions,
    val columnFamilyOptions: ColumnFamilyOptions,
    val tables: Map<TableContext<*, *>, Table<*, *>>
) : AutoCloseable {
    constructor(
        db: RocksDB,
        dbOptions: DBOptions,
        columnFamilyOptions: ColumnFamilyOptions,
        tables: List<Table<*, *>>
    ) : this(db, dbOptions, columnFamilyOptions, tables.associateBy({ it.context }, { it })) {
    }

    override fun close() {
        tables.values.forEach { it.handle.close() }
        db.close()
        dbOptions.close()
        columnFamilyOptions.close()
    }

    fun <K, V> getTable(context: TableContext<K, V>): Table<K, V> {
        return tables[context] as Table<K, V>
    }

}
