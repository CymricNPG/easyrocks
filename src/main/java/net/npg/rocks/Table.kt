package net.npg.rocks

import org.rocksdb.ColumnFamilyDescriptor
import org.rocksdb.ColumnFamilyHandle
import org.rocksdb.ColumnFamilyOptions
import org.rocksdb.RocksIterator

class Table<K, V>(val context: TableContext<K, V>, val cfOpts: ColumnFamilyOptions, val cfDescriptor: ColumnFamilyDescriptor) {
    constructor(context: TableContext<K, V>, cfOpts: ColumnFamilyOptions)
            : this(context, cfOpts, ColumnFamilyDescriptor(context.name.toByteArray(Charsets.UTF_8), cfOpts))

    lateinit var handle: ColumnFamilyHandle
    lateinit var db: Database

    fun put(key: K, value: V) {
        db.db.put(handle, context.serializeKey(key), context.serializeValue(value))
    }

    fun getAll(): Map<K, V> {
        val map = mutableMapOf<K, V>()
        val iterator: RocksIterator = db.db.newIterator(handle)
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