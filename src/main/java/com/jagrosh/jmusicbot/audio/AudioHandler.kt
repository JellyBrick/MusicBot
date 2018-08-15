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
package com.jagrosh.jmusicbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import java.util.HashSet
import java.util.LinkedList
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.playlist.Playlist
import com.jagrosh.jmusicbot.queue.FairQueue
import net.dv8tion.jda.core.audio.AudioSendHandler
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.User

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class AudioHandler(val player: AudioPlayer, guild: Guild, private val bot: Bot) : AudioEventAdapter(), AudioSendHandler {
    private val guildId: Long = guild.idLong
    val queue: FairQueue<QueuedTrack> = FairQueue()
    val votes: MutableSet<String>
    private val defaultQueue: MutableList<AudioTrack>
    private var lastFrame: AudioFrame? = null
    var requester: Long = 0
        private set

    val isMusicPlaying: Boolean
        get() = bot.jda!!.getGuildById(guildId).selfMember.voiceState.inVoiceChannel() && player.playingTrack != null

    init {
        votes = HashSet()
        defaultQueue = LinkedList()
    }

    fun addTrack(track: AudioTrack, user: User): Int {
        return if (player.playingTrack == null) {
            requester = user.idLong
            player.playTrack(track)
            -1
        } else
            queue.add(QueuedTrack(track, user.idLong))
    }

    fun stopAndClear() {
        queue.clear()
        defaultQueue.clear()
        player.stopTrack()
        //current = null;
    }

    fun playFromDefault(): Boolean {
        if (!defaultQueue.isEmpty()) {
            player.playTrack(defaultQueue.removeAt(0))
            return true
        }
        val guild = bot.jda!!.getGuildById(guildId)
        if (bot.getSettings(guild).defaultPlaylist == null)
            return false
        val pl = Playlist.loadPlaylist(bot.getSettings(guild).defaultPlaylist!!)
        if (pl == null || pl.items.isEmpty())
            return false
        pl.loadTracks(bot.audioManager, { at ->
            if (player.playingTrack == null)
                player.playTrack(at as AudioTrack?)
            else
                defaultQueue.add(at as AudioTrack)
        }, {
            if (pl.tracks!!.isEmpty() && !STAY_IN_CHANNEL)
                guild.audioManager.closeAudioConnection()
        })
        return true
    }

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {
        if (endReason == AudioTrackEndReason.FINISHED && bot.getSettings(bot.jda!!.getGuildById(guildId)).repeatMode) {
            queue.add(QueuedTrack(track!!.makeClone(), requester))
        }
        requester = 0
        if (queue.isEmpty) {
            if (!playFromDefault()) {
                if (SONG_IN_STATUS)
                    bot.resetGame()
                if (!STAY_IN_CHANNEL)
                    bot.threadpool.submit { bot.jda!!.getGuildById(guildId).audioManager.closeAudioConnection() }
                bot.updateTopic(guildId, this)
            }
        } else {
            val qt = queue.pull()
            requester = qt.identifier
            player!!.playTrack(qt.track)
        }
    }

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {
        votes.clear()
        if (SONG_IN_STATUS) {
            if (bot.jda!!.guilds.stream().filter { g -> g.selfMember.voiceState.inVoiceChannel() }.count() <= 1)
                bot.jda!!.presence.game = Game.listening(track!!.info.title)
            else
                bot.resetGame()
        }
        bot.updateTopic(guildId, this)
    }

    override fun canProvide(): Boolean {
        lastFrame = player.provide()
        return lastFrame != null
    }

    override fun provide20MsAudio(): ByteArray {
        return lastFrame!!.data
    }

    override fun isOpus(): Boolean {
        return true
    }

    companion object {
        var STAY_IN_CHANNEL: Boolean = false
        var SONG_IN_STATUS: Boolean = false
        var MAX_SECONDS: Long = -1
        var USE_NP_REFRESH: Boolean = false

        fun isTooLong(track: AudioTrack): Boolean {
            return if (MAX_SECONDS <= 0) false else Math.round(track.duration / 1000.0) > MAX_SECONDS
        }
    }
}
