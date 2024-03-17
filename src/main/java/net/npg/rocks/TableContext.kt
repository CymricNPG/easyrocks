package net.npg.rocks

class TableContext<K,V>(
    val name: String,
    val serializeKey: (K) -> ByteArray,
    val deserializeKey: (ByteArray) -> K,
    val serializeValue: (V) -> ByteArray,
    val deserializeValue: (ByteArray) -> V) {
}


