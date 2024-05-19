/*
 *   Easy RocksDB Interface Playground
 *     Copyright (C) 2024 Roland Spatzenegger
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.npg.rocks.impl

import com.alibaba.fastjson2.JSON
import net.npg.rocks.TableContext
import kotlin.reflect.KClass

class JsonKeyValueContext<K, V>(
    override val name: String,
    override val serializeKey: (K) -> ByteArray,
    override val deserializeKey: (ByteArray) -> K,
    override val serializeValue: (V) -> ByteArray,
    override val deserializeValue: (ByteArray) -> V
) : TableContext<K, V> {
    constructor(name: String, keyClass: Class<K>, valueClass: Class<V>) : this(
        name,
        { key -> JSON.toJSONBytes(key) },
        { data -> JSON.parseObject(data, keyClass) },
        { value -> JSON.toJSONBytes(value) },
        { data -> JSON.parseObject(data, valueClass) }
    )
}


class StringKeyJsonValueContext<V : Any>(
    override val name: String,
    override val serializeKey: (String) -> ByteArray,
    override val deserializeKey: (ByteArray) -> String,
    override val serializeValue: (V) -> ByteArray,
    override val deserializeValue: (ByteArray) -> V
) : TableContext<String, V> {
    constructor(name: String, valueClass: KClass<V>) : this(
        name,
        { key -> key.toByteArray(Charsets.UTF_8) },
        { value -> value.toString(Charsets.UTF_8) },
        { value -> JSON.toJSONBytes(value) },
        { data -> JSON.parseObject(data, valueClass.javaObjectType) }
    )
}
