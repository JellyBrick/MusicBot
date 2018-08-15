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

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class RepeatCmd(private val bot: Bot) : Command() {
    init {
        this.name = "repeat"
        this.help = "re-adds music to the queue when finished"
        this.arguments = "[on|off]"
        this.guildOnly = true
        this.category = bot.DJ
    }

    override fun execute(event: CommandEvent) {
        val value: Boolean = if (event.args.isEmpty()) {
            !bot.getSettings(event.guild).repeatMode
        } else if (event.args.equals("true", ignoreCase = true) || event.args.equals("on", ignoreCase = true)) {
            true
        } else if (event.args.equals("false", ignoreCase = true) || event.args.equals("off", ignoreCase = true)) {
            false
        } else {
            event.replyError("Valid options are `on` or `off` (or leave empty to toggle)")
            return
        }
        bot.setRepeatMode(event.guild, value)
        event.replySuccess("Repeat mode is now `" + (if (value) "ON" else "OFF") + "`")
    }

}
