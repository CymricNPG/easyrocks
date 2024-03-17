package net.npg.rocks

interface Database : AutoCloseable {
    fun <K, V> getTable(context: TableContext<K, V>): Table<K, V>
}