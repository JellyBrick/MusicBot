/*
 * Copyrigh t 2016 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.HashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import com.jagrosh.jdautilities.command.Command.Category
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jmusicbot.audio.AudioHandler
import com.jagrosh.jmusicbot.gui.GUI
import com.jagrosh.jmusicbot.utils.FormatUtil
import com.jagrosh.jmusicbot.utils.Pair
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.ShutdownEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent
import net.dv8tion.jda.core.exceptions.PermissionException
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class Bot(val waiter: EventWaiter, private val config: Config) : ListenerAdapter() {

    private val settings: HashMap<String, Settings> = HashMap()
    private val lastNP: HashMap<Long, Pair<Long, Long>> = HashMap() // guild -> channel,message
    val audioManager: AudioPlayerManager
    val threadpool: ScheduledExecutorService
    var jda: JDA? = null
        private set
    private var gui: GUI? = null
    //private GuildsPanel panel;
    val MUSIC = Category("Music")
    val DJ = Category("DJ") { event ->
        if (event.author.id == event.client.ownerId)
            return@Category true
        if (event.guild == null)
            return@Category true
        if (event.member.hasPermission(Permission.MANAGE_SERVER))
            return@Category true
        val dj = event.guild.getRoleById(getSettings(event.guild).roleId)
        event.member.roles.contains(dj)
    }

    val ADMIN = Category("Admin") { event ->
        if (event.author.id == event.client.ownerId)
            return@Category true
        if (event.guild == null)
            return@Category true
        event.member.hasPermission(Permission.MANAGE_SERVER)
    }

    val OWNER = Category("Owner")

    init {
        audioManager = DefaultAudioPlayerManager()
        threadpool = Executors.newSingleThreadScheduledExecutor()
        AudioSourceManagers.registerRemoteSources(audioManager)
        AudioSourceManagers.registerLocalSource(audioManager)
        audioManager.source<YoutubeAudioSourceManager>(YoutubeAudioSourceManager::class.java).setPlaylistPageCount(10)
        try {
            val loadedSettings = JSONObject(String(Files.readAllBytes(Paths.get("serversettings.json"))))
            loadedSettings.keySet().forEach { id ->
                val o = loadedSettings.getJSONObject(id)

                settings[id] = Settings(
                        if (o.has("text_channel_id")) o.getString("text_channel_id") else null,
                        if (o.has("voice_channel_id")) o.getString("voice_channel_id") else null,
                        if (o.has("dj_role_id")) o.getString("dj_role_id") else null,
                        if (o.has("volume")) o.getInt("volume") else 100,
                        if (o.has("default_playlist")) o.getString("default_playlist") else null,
                        o.has("repeat") && o.getBoolean("repeat"))
            }
        } catch (e: IOException) {
            LoggerFactory.getLogger("Settings").warn("Failed to load server settings (this is normal if no settings have been set yet): $e")
        } catch (e: JSONException) {
            LoggerFactory.getLogger("Settings").warn("Failed to load server settings (this is normal if no settings have been set yet): $e")
        }

    }

    fun queueTrack(event: CommandEvent, track: AudioTrack): Int {
        return setUpHandler(event).addTrack(track, event.author)
    }

    fun setUpHandler(event: CommandEvent): AudioHandler {
        return setUpHandler(event.guild)
    }

    private fun setUpHandler(guild: Guild): AudioHandler {
        val handler: AudioHandler
        if (guild.audioManager.sendingHandler == null) {
            val player = audioManager.createPlayer()
            if (settings.containsKey(guild.id))
                player.volume = settings[guild.id]!!.volume
            handler = AudioHandler(player, guild, this)
            player.addListener(handler)
            guild.audioManager.sendingHandler = handler
            if (AudioHandler.USE_NP_REFRESH)
                threadpool.scheduleWithFixedDelay({ updateLastNP(guild.idLong) }, 0, 5, TimeUnit.SECONDS)
        } else
            handler = guild.audioManager.sendingHandler as AudioHandler
        return handler
    }

    fun resetGame() {
        val game = if (config.getGame() == null || config.getGame()!!.name.equals("none", ignoreCase = true)) null else config.getGame()
        if (jda!!.presence.game != game)
            jda!!.presence.game = game
    }

    fun setLastNP(m: Message) {
        lastNP[m.guild.idLong] = Pair(m.textChannel.idLong, m.idLong)
    }

    override fun onGuildMessageDelete(event: GuildMessageDeleteEvent?) {
        if (lastNP.containsKey(event!!.guild.idLong)) {
            val pair = lastNP[event.guild.idLong]
            if (pair?.value == event.messageIdLong)
                lastNP.remove(event.guild.idLong)
        }
    }

    private fun updateLastNP(guildId: Long) {
        val guild = jda!!.getGuildById(guildId) ?: return
        if (!lastNP.containsKey(guildId))
            return
        val pair = lastNP[guildId] ?: return
        val tc = guild.getTextChannelById(pair.key)
        if (tc == null) {
            lastNP.remove(guildId)
            return
        }
        try {
            tc.editMessageById(pair.value, FormatUtil.nowPlayingMessage(guild, config.success!!)).queue { lastNP.remove(guildId) }
        } catch (e: Exception) {
            lastNP.remove(guildId)
        }

    }

    fun updateTopic(guildId: Long, handler: AudioHandler) {
        val guild = jda!!.getGuildById(guildId) ?: return
        val tchan = guild.getTextChannelById(getSettings(guild).textId)
        if (tchan != null && guild.selfMember.hasPermission(tchan, Permission.MANAGE_CHANNEL)) {
            val otherText: String = if (tchan.topic == null || tchan.topic.isEmpty())
                "\u200B"
            else if (tchan.topic.contains("\u200B"))
                tchan.topic.substring(tchan.topic.lastIndexOf("\u200B"))
            else
                "\u200B\n " + tchan.topic
            val text = FormatUtil.topicFormat(handler) + otherText
            if (text != tchan.topic)
                try {
                    tchan.manager.setTopic(text).queue()
                } catch (ignored: PermissionException) {
                }

        }
    }

    fun shutdown() {
        audioManager.shutdown()
        threadpool.shutdownNow()
        jda!!.guilds.forEach { g ->
            g.audioManager.closeAudioConnection()
            val ah = g.audioManager.sendingHandler as AudioHandler
            ah.queue.clear()
            ah.player.destroy()
            updateTopic(g.idLong, ah)
        }
        jda!!.shutdown()
    }

    fun setGUI(gui: GUI) {
        this.gui = gui
    }

    override fun onShutdown(event: ShutdownEvent?) {
        if (gui != null)
            gui!!.dispose()
    }

    override fun onReady(event: ReadyEvent?) {
        this.jda = event!!.jda
        if (jda!!.guilds.isEmpty()) {
            val log = LoggerFactory.getLogger("MusicBot")
            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!")
            log.warn(event.jda.asBot().getInviteUrl(*JMusicBot.RECOMMENDED_PERMS))
        }
        //credit(event.jda)
        jda!!.guilds.forEach { guild ->
            try {
                val defpl = getSettings(guild).defaultPlaylist
                val vc = guild.getVoiceChannelById(getSettings(guild).voiceId)
                if (defpl != null && vc != null) {
                    if (setUpHandler(guild).playFromDefault())
                        guild.audioManager.openAudioConnection(vc)
                }
            } catch (ex: Exception) {
                System.err.println(ex)
            }
        }
    }

    override fun onGuildJoin(event: GuildJoinEvent?) {
        //credit(event!!.jda)
    }

    // make sure people aren't adding clones to dbots
    private fun credit(jda: JDA) {
        val dbots = jda.getGuildById(110373943822540800L) ?: return
        if (config.dBots)
            return
        jda.getTextChannelById(119222314964353025L)
                .sendMessage("<@113156185389092864>: This account is running JMusicBot. Please do not list bot clones on this server, <@" + config.ownerId + ">.").complete()
        dbots.leave().queue()
    }

    // settings

    fun getSettings(guild: Guild): Settings {
        return (settings as Map<String, Settings>).getOrDefault(guild.id, Settings.DEFAULT_SETTINGS)
    }

    fun setTextChannel(channel: TextChannel) {
        val s = settings[channel.guild.id]
        if (s == null) {
            settings[channel.guild.id] = Settings(channel.id, null, null, 100, null, false)
        } else {
            s.textId = channel.idLong
        }
        writeSettings()
    }

    fun setVoiceChannel(channel: VoiceChannel) {
        val s = settings[channel.guild.id]
        if (s == null) {
            settings[channel.guild.id] = Settings(null, channel.id, null, 100, null, false)
        } else {
            s.voiceId = channel.idLong
        }
        writeSettings()
    }

    fun setRole(role: Role) {
        val s = settings[role.guild.id]
        if (s == null) {
            settings[role.guild.id] = Settings(null, null, role.id, 100, null, false)
        } else {
            s.roleId = role.idLong
        }
        writeSettings()
    }

    fun setDefaultPlaylist(guild: Guild, playlist: String?) {
        val s = settings[guild.id]
        if (s == null) {
            settings[guild.id] = Settings(null, null, null, 100, playlist, false)
        } else {
            s.defaultPlaylist = playlist
        }
        writeSettings()
    }

    fun setVolume(guild: Guild, volume: Int) {
        val s = settings[guild.id]
        if (s == null) {
            settings[guild.id] = Settings(null, null, null, volume, null, false)
        } else {
            s.volume = volume
        }
        writeSettings()
    }

    fun setRepeatMode(guild: Guild, mode: Boolean) {
        val s = settings[guild.id]
        if (s == null) {
            settings[guild.id] = Settings(null, null, null, 100, null, mode)
        } else {
            s.repeatMode = mode
        }
        writeSettings()
    }

    fun clearTextChannel(guild: Guild) {
        val s = getSettings(guild)
        if (s !== Settings.DEFAULT_SETTINGS) {
            if (s.voiceId == 0L && s.roleId == 0L)
                settings.remove(guild.id)
            else
                s.textId = 0
            writeSettings()
        }
    }

    fun clearVoiceChannel(guild: Guild) {
        val s = getSettings(guild)
        if (s !== Settings.DEFAULT_SETTINGS) {
            if (s.textId == 0L && s.roleId == 0L)
                settings.remove(guild.id)
            else
                s.voiceId = 0
            writeSettings()
        }
    }

    fun clearRole(guild: Guild) {
        val s = getSettings(guild)
        if (s !== Settings.DEFAULT_SETTINGS) {
            if (s.voiceId == 0L && s.textId == 0L)
                settings.remove(guild.id)
            else
                s.roleId = 0
            writeSettings()
        }
    }

    private fun writeSettings() {
        val obj = JSONObject()
        settings.keys.forEach { key ->
            val o = JSONObject()
            val s = settings[key]
            if (s!!.textId != 0L)
                o.put("text_channel_id", java.lang.Long.toString(s.textId))
            if (s.voiceId != 0L)
                o.put("voice_channel_id", java.lang.Long.toString(s.voiceId))
            if (s.roleId != 0L)
                o.put("dj_role_id", java.lang.Long.toString(s.roleId))
            if (s.volume != 100)
                o.put("volume", s.volume)
            if (s.defaultPlaylist != null)
                o.put("default_playlist", s.defaultPlaylist)
            if (s.repeatMode)
                o.put("repeat", true)
            obj.put(key, o)
        }
        try {
            Files.write(Paths.get("serversettings.json"), obj.toString(4).toByteArray())
        } catch (ex: IOException) {
            LoggerFactory.getLogger("Settings").warn("Failed to write to file: $ex")
        }

    }

    //gui stuff
    /*public void registerPanel(GuildsPanel panel)
    {
        this.panel = panel;
        threadpool.scheduleWithFixedDelay(() -> updatePanel(), 0, 5, TimeUnit.SECONDS);
    }

    public void updatePanel()
    {
        System.out.println("updating...");
        Guild guild = jda.getGuilds().get(panel.getIndex());
        panel.updatePanel((AudioHandler)guild.getAudioManager().getSendingHandler());
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if(panel!=null)
            panel.updateList(event.getJDA().getGuilds());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        if(panel!=null)
            panel.updateList(event.getJDA().getGuilds());
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        ((GUI)panel.getTopLevelAncestor()).dispose();
    }*/

}
