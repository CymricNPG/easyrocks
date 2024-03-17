package net.npg.rocks

interface Table<K, V> {

    fun put(key: K, value: V)

    fun getAll(): Map<K, V>
}