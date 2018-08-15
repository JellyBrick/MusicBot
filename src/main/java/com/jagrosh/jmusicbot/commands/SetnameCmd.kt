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
package com.jagrosh.jmusicbot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import net.dv8tion.jda.core.exceptions.RateLimitedException

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class SetnameCmd(bot: Bot) : Command() {

    init {
        this.name = "setname"
        this.help = "sets the name of the bot"
        this.arguments = "<name>"
        this.ownerCommand = true
        this.category = bot.OWNER
    }

    override fun execute(event: CommandEvent) {
        try {
            val oldname = event.selfUser.name
            event.selfUser.manager.setName(event.args).complete(false)
            event.reply(event.client.success + " Name changed from `" + oldname + "` to `" + event.args + "`")
        } catch (e: RateLimitedException) {
            event.reply(event.client.error + " Name can only be changed twice per hour!")
        } catch (e: Exception) {
            event.reply(event.client.error + " That name is not valid!")
        }

    }

}
