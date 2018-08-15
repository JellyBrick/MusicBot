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
package com.jagrosh.jmusicbot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.utils.FinderUtil
import com.jagrosh.jmusicbot.utils.FormatUtil

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class SettcCmd(private val bot: Bot) : Command() {
    init {
        this.name = "settc"
        this.help = "sets the text channel for music commands"
        this.arguments = "<channel|NONE>"
        this.guildOnly = true
        this.category = bot.ADMIN
    }

    override fun execute(event: CommandEvent) {
        if (event.args.isEmpty()) {
            event.reply(event.client.error + " Please include a text channel or NONE")
        } else if (event.args.equals("none", ignoreCase = true)) {
            bot.clearTextChannel(event.guild)
            event.reply(event.client.success + " Music commands can now be used in any channel")
        } else {
            val list = FinderUtil.findTextChannel(event.args, event.guild)
            if (list.isEmpty())
                event.reply(event.client.warning + " No Text Channels found matching \"" + event.args + "\"")
            else if (list.size > 1)
                event.reply(event.client.warning + FormatUtil.listOfTChannels(list, event.args))
            else {
                bot.setTextChannel(list[0])
                event.reply(event.client.success + " Music commands can now only be used in <#" + list[0].id + ">")
            }
        }
    }

}
