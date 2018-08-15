/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.gui

import java.awt.EventQueue
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.swing.JTextArea

/**
 *
 * @author Lawrence Dol
 */
internal class TextAreaOutputStream private constructor(txtara: JTextArea, maxlin: Int) : OutputStream() {

    // *************************************************************************************************
    // INSTANCE MEMBERS
    // *************************************************************************************************

    private val oneByte: ByteArray                                                    // array for write(int val);
    private var appender: Appender? = null                                                   // most recent action

    constructor(txtara: JTextArea) : this(txtara, 1000)

    init {
        if (maxlin < 1) {
            throw IllegalArgumentException("TextAreaOutputStream maximum lines must be positive (value=$maxlin)")
        }
        oneByte = ByteArray(1)
        appender = Appender(txtara, maxlin)
    }

    /** Clear the current console text area.  */
    @Synchronized
    fun clear() {
        if (appender != null) {
            appender!!.clear()
        }
    }

    @Synchronized
    override fun close() {
        appender = null
    }

    @Synchronized
    override fun flush() {
    }

    @Synchronized
    override fun write(`val`: Int) {
        oneByte[0] = `val`.toByte()
        write(oneByte, 0, 1)
    }

    @Synchronized
    override fun write(ba: ByteArray) {
        write(ba, 0, ba.size)
    }

    @Synchronized
    override fun write(ba: ByteArray, str: Int, len: Int) {
        if (appender != null) {
            appender!!.append(bytesToString(ba, str, len))
        }
    }

    //@edu.umd.cs.findbugs.annotations.SuppressWarnings("DM_DEFAULT_ENCODING")
    private fun bytesToString(ba: ByteArray, str: Int, len: Int): String {
        return String(ba, str, len, StandardCharsets.UTF_8)
    }

    // *************************************************************************************************
    // STATIC MEMBERS
    // *************************************************************************************************

    internal class Appender(private val textArea: JTextArea, private val maxLines: Int                                                   // maximum lines allowed in text area
    ) : Runnable {
        private val lengths: LinkedList<Int> = LinkedList()                                                    // length of lines within text area
        private val values: MutableList<String>                                                     // values waiting to be appended

        private var curLength: Int = 0                                                  // length of current line
        private var clear: Boolean = false
        private var queue: Boolean = false

        init {
            values = ArrayList()

            curLength = 0
            clear = false
            queue = true
        }

        @Synchronized
        fun append(`val`: String) {
            values.add(`val`)
            if (queue) {
                queue = false
                EventQueue.invokeLater(this)
            }
        }

        @Synchronized
        fun clear() {
            clear = true
            curLength = 0
            lengths.clear()
            values.clear()
            if (queue) {
                queue = false
                EventQueue.invokeLater(this)
            }
        }

        // MUST BE THE ONLY METHOD THAT TOUCHES textArea!
        @Synchronized
        override fun run() {
            if (clear) {
                textArea.text = ""
            }
            values.stream().peek { `val` -> curLength += `val`.length }.peek { `val` ->
                if (`val`.endsWith(EOL1) || `val`.endsWith(EOL2)) {
                    if (lengths.size >= maxLines) {
                        textArea.replaceRange("", 0, lengths.removeFirst())
                    }
                    lengths.addLast(curLength)
                    curLength = 0
                }
            }.forEach { textArea.append(it) }
            values.clear()
            clear = false
            queue = true
        }

        companion object {

            private const val EOL1 = "\n"
            private val EOL2 = System.getProperty("line.separator", EOL1)
        }
    }

} /* END PUBLIC CLASS */