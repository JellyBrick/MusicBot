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

import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionEvent
import net.dv8tion.jda.core.entities.Guild
import java.util.*

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
internal class GuildsPanel : JPanel() {

    private val guildList: JList<*>
    private var index = -1

    init {
        super.setLayout(GridBagLayout())

        guildList = JList<Any>()
        val guildQueue = JTextArea()
        guildList.model = DefaultListModel()
        guildList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        guildList.fixedCellHeight = 20
        guildList.preferredSize = Dimension(100, 300)
        guildQueue.preferredSize = Dimension(300, 300)
        guildQueue.isEditable = false
        val pane = JScrollPane()
        val pane2 = JScrollPane()
        pane.setViewportView(guildList)
        pane2.setViewportView(guildQueue)
        val c = GridBagConstraints()
        c.fill = GridBagConstraints.BOTH
        c.anchor = GridBagConstraints.LINE_START
        c.gridx = 0
        c.gridwidth = 1
        super.add(pane, c)
        c.gridx = 1
        c.gridwidth = 3
        super.add(pane2, c)
        //bot.registerPanel(this);
        guildList.addListSelectionListener {
            index = guildList.selectedIndex
            //bot.updatePanel();
        }
    }

    fun updateList(guilds: List<Guild>) {
        val strs = arrayOfNulls<String>(guilds.size)
        for (i in guilds.indices)
            strs[i] = guilds[i].name
        guildList.setListData(strs as Vector<out Nothing?>)
    }

    fun getIndex(): Int {
        return guildList.selectedIndex
    }

    /*public void updatePanel(AudioHandler handler) {
        StringBuilder builder = new StringBuilder("Now Playing: ");
        if(handler==null || handler.getCurrentTrack()==null)
        {
            builder.append("nothing");
        }
        else
        {
            builder.append(handler.getCurrentTrack().getTrack().getInfo().title)
                    .append(" [")
                    .append(FormatUtil.formatTime(handler.getCurrentTrack().getTrack().getDuration()))
                    .append("]\n");
            for(int i=0; i<handler.getQueue().size(); i++)
                builder.append("\n").append(i+1).append(". ").append(handler.getQueue().get(i).getTrack().getInfo().title);
        }
        guildQueue.setText(builder.toString());
        guildQueue.updateUI();
    }*/

}
