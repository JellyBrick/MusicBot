/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.queue

import java.util.ArrayList
import java.util.HashSet

/**
 *
 * @author John Grosh (jagrosh)
 * @param <T>
</T> */
class FairQueue<T : Queueable> {
    val list = ArrayList<T>()
    private val set = HashSet<Long>()

    val isEmpty: Boolean
        get() = list.isEmpty()

    fun add(item: T): Int {
        var lastIndex: Int = list.size - 1
        while (lastIndex > -1) {
            if (list[lastIndex].identifier == item.identifier)
                break
            lastIndex--
        }
        lastIndex++
        set.clear()
        while (lastIndex < list.size) {
            if (set.contains(list[lastIndex].identifier))
                break
            set.add(list[lastIndex].identifier)
            lastIndex++
        }
        list.add(lastIndex, item)
        return lastIndex
    }

    fun size(): Int {
        return list.size
    }

    fun pull(): T {
        return list.removeAt(0)
    }

    fun getList(): List<T> {
        return list
    }

    operator fun get(index: Int): T {
        return list[index]
    }

    fun remove(index: Int) {
        list.removeAt(index)
    }

    fun removeAll(identifier: Long): Int {
        var count = 0
        for (i in list.indices.reversed()) {
            if (list[i].identifier == identifier) {
                list.removeAt(i)
                count++
            }
        }
        return count
    }

    fun clear() {
        list.clear()
    }

    fun shuffle(identifier: Long): Int {
        val iset = ArrayList<Int>()
        for (i in list.indices) {
            if (list[i].identifier == identifier)
                iset.add(i)
        }
        for (j in iset.indices) {
            val first = iset[j]
            val second = iset[(Math.random() * iset.size).toInt()]
            val temp = list[first]
            list[first] = list[second]
            list[second] = temp
        }
        return iset.size
    }

    fun skip(number: Int) {
        if (number > 0) {
            list.subList(0, number).clear()
        }
    }
}
