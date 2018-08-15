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
class SetdjCmd(private val bot: Bot) : Command() {
    init {
        this.name = "setdj"
        this.help = "sets the DJ role for certain music commands"
        this.arguments = "<rolename|NONE>"
        this.guildOnly = true
        this.category = bot.ADMIN
    }

    override fun execute(event: CommandEvent) {
        if (event.args.isEmpty()) {
            event.reply(event.client.error + " Please include a role name or NONE")
        } else if (event.args.equals("none", ignoreCase = true)) {
            bot.clearRole(event.guild)
            event.reply(event.client.success + " DJ role cleared; Only Admins can use the DJ commands.")
        } else {
            val list = FinderUtil.findRole(event.args, event.guild)
            if (list.isEmpty())
                event.reply(event.client.warning + " No Roles found matching \"" + event.args + "\"")
            else if (list.size > 1)
                event.reply(event.client.warning + FormatUtil.listOfRoles(list, event.args))
            else {
                bot.setRole(list[0])
                event.reply(event.client.success + " DJ commands can now be used by users with the **" + list[0].name + "** role.")
            }
        }
    }

}
