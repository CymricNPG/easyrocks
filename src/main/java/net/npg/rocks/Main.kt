package net.npg.rocks

import net.npg.rocks.impl.RocksDBService
import java.nio.file.Path


fun main() {
    val dbService = RocksDBService()
    val tableContext = TableContext<String, String>("test",
        { s -> s.toByteArray(Charsets.UTF_8) },
        { s -> s.toString(Charsets.UTF_8) },
        { s -> s.toByteArray(Charsets.UTF_8) },
        { s -> s.toString(Charsets.UTF_8) })

    dbService.open(Path.of("test"), listOf(tableContext)).use { db ->
        val table = db.getTable(tableContext)
        for (i in 1..100) {
            val key = "Hossa" + i
            val value = "bla" + i
            table.put(key, value)
        }
        table.getAll().forEach { (k, v) -> println("$k -> $v") }
    }

}