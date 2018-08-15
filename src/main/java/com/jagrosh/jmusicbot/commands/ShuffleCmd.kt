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
import com.jagrosh.jmusicbot.audio.AudioHandler

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class ShuffleCmd(bot: Bot) : MusicCommand(bot) {

    init {
        this.name = "shuffle"
        this.help = "shuffles songs you have added"
        this.beListening = true
        this.bePlaying = true
    }

    public override fun doCommand(event: CommandEvent) {
        val handler = event.guild.audioManager.sendingHandler as AudioHandler
        val s = handler.queue.shuffle(event.author.idLong)
        when (s) {
            0 -> event.reply(event.client.error + " You don't have any music in the queue to shuffle!")
            1 -> event.reply(event.client.warning + " You only have one song in the queue!")
            else -> event.reply(event.client.success + " You successfully shuffled your " + s + " entries.")
        }
    }

}
