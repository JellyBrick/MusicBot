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
import net.dv8tion.jda.core.OnlineStatus

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class SetstatusCmd(bot: Bot) : Command() {

    init {
        this.name = "setstatus"
        this.help = "sets the status the bot displays"
        this.arguments = "<status>"
        this.ownerCommand = true
        this.category = bot.OWNER
    }

    override fun execute(event: CommandEvent) {
        try {
            val status = OnlineStatus.fromKey(event.args)
            if (status == OnlineStatus.UNKNOWN) {
                event.replyError("Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`")
            } else {
                event.jda.presence.status = status
                event.replySuccess("Set the status to `" + status.key.toUpperCase() + "`")
            }
        } catch (e: Exception) {
            event.reply(event.client.error + " The status could not be set!")
        }

    }

}
