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


