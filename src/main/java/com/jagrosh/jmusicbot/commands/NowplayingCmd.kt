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

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.utils.FormatUtil
import net.dv8tion.jda.core.Permission
/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class NowplayingCmd(bot: Bot) : MusicCommand(bot) {

    init {
        this.name = "nowplaying"
        this.help = "shows the song that is currently playing"
        this.aliases = arrayOf("np", "current")
        this.botPermissions = arrayOf(Permission.MESSAGE_EMBED_LINKS)
    }

    public override fun doCommand(event: CommandEvent) {
        event.reply(FormatUtil.nowPlayingMessage(event.guild, event.client.success)) { bot.setLastNP(it) }
    }

}
