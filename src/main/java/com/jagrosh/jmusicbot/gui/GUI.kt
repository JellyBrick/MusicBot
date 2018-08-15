/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
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

import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.JFrame
import javax.swing.JTabbedPane
import javax.swing.WindowConstants
import com.jagrosh.jmusicbot.Bot


/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class GUI(private val bot: Bot) : JFrame() {

    private val console: ConsolePanel = ConsolePanel()

    init {
        GuildsPanel()
    }

    fun init() {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        title = "JMusicBot"
        val tabs = JTabbedPane()
        //tabs.add("Guilds", guilds);
        tabs.add("Console", console)
        contentPane.add(tabs)
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent) {}
            override fun windowClosing(e: WindowEvent) {
                bot.shutdown()
            }

            override fun windowClosed(e: WindowEvent) {}
            override fun windowIconified(e: WindowEvent) {}
            override fun windowDeiconified(e: WindowEvent) {}
            override fun windowActivated(e: WindowEvent) {}
            override fun windowDeactivated(e: WindowEvent) {}
        })
    }
}
