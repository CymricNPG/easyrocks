package net.npg.rocks

import java.nio.file.Path

interface DBService {
    fun open(dbPath: Path, tableContexts: List<TableContext<*, *>>): Database
}
