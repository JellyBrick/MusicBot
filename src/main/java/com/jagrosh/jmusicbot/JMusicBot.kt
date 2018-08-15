/*
 * Copyright 2016 John Grosh (jagrosh).
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

import com.jagrosh.jdautilities.command.CommandClientBuilder
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.examples.command.AboutCommand
import com.jagrosh.jdautilities.examples.command.PingCommand
import com.jagrosh.jmusicbot.audio.AudioHandler
import com.jagrosh.jmusicbot.commands.*
import com.jagrosh.jmusicbot.gui.GUI
import com.jagrosh.jmusicbot.utils.OtherUtil
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Game
import org.slf4j.LoggerFactory
import java.awt.Color
import javax.security.auth.login.LoginException
import javax.swing.JOptionPane

/**
 *
 * @author John Grosh (jagrosh)
 */
internal object JMusicBot {
    val RECOMMENDED_PERMS = arrayOf(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.MESSAGE_EXT_EMOJI, Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE)
    private val LOG = LoggerFactory.getLogger("Startup")
    /**
     * @param args the command line arguments
     */
    @JvmStatic
    fun main(args: Array<String>) {
        // check run mode(s)
        var nogui = false
        for (arg in args)
            if ("-nogui".equals(arg, ignoreCase = true))
                nogui = true

        // Get version number
        val version: String = if (JMusicBot::class.java.getPackage() != null && JMusicBot::class.java.getPackage().implementationVersion != null)
            JMusicBot::class.java.getPackage().implementationVersion
        else
            "UNKNOWN"

        // Check for new version
        val latestVersion = OtherUtil.latestVersion
        if (latestVersion != null && latestVersion != version) {
            val msg = ("There is a new version of JMusicBot available!\n"
                    + "Current version: " + version + "\n"
                    + "New Version: " + latestVersion + "\n\n"
                    + "Please visit https://github.com/jagrosh/MusicBot/releases/latest to get the latest release.")
            if (nogui)
                LOG.warn(msg)
            else {
                try {
                    JOptionPane.showMessageDialog(null, msg, "JMusicBot", JOptionPane.WARNING_MESSAGE)
                } catch (e: Exception) {
                    nogui = true
                    LOG.warn("Switching to nogui mode. You can manually start in nogui mode by including the -nogui flag.")
                    LOG.warn(msg)
                }

            }
        }

        // load config
        val config = Config(nogui)

        // set up the listener
        val waiter = EventWaiter()
        val bot = Bot(waiter, config)

        val ab = AboutCommand(Color.BLUE.brighter(),
                "a music bot that is [easy to host yourself!](https://github.com/jagrosh/MusicBot) (v$version)",
                arrayOf("High-quality music playback", "FairQueue™ Technology", "Easy to host yourself"),
                *RECOMMENDED_PERMS)
        ab.setIsAuthor(false)
        ab.setReplacementCharacter("\uD83C\uDFB6")
        AudioHandler.STAY_IN_CHANNEL = config.stay
        AudioHandler.SONG_IN_STATUS = config.songInStatus
        AudioHandler.MAX_SECONDS = config.maxSeconds
        AudioHandler.USE_NP_REFRESH = !config.useNPImages()
        // set up the command client

        val cb = CommandClientBuilder()
                .setPrefix(config.prefix)
                .setAlternativePrefix(config.altPrefix)
                .setOwnerId(config.ownerId)
                .setEmojis(config.success, config.warning, config.error)
                .setHelpWord(config.help)
                .setLinkedCacheSize(200)
                .addCommands(
                        ab,
                        PingCommand(),
                        SettingsCmd(bot),

                        NowplayingCmd(bot),
                        PlayCmd(bot, config.loading),
                        PlaylistsCmd(bot),
                        QueueCmd(bot),
                        RemoveCmd(bot),
                        SearchCmd(bot, config.searching),
                        SCSearchCmd(bot, config.searching),
                        ShuffleCmd(bot),
                        SkipCmd(bot),

                        ForceskipCmd(bot),
                        PauseCmd(bot),
                        RepeatCmd(bot),
                        SkiptoCmd(bot),
                        StopCmd(bot),
                        VolumeCmd(bot),

                        SetdjCmd(bot),
                        SettcCmd(bot),
                        SetvcCmd(bot),

                        //GuildlistCommand(waiter),
                        AutoplaylistCmd(bot),
                        PlaylistCmd(bot),
                        SetavatarCmd(bot),
                        SetgameCmd(bot),
                        SetnameCmd(bot),
                        SetstatusCmd(bot),
                        ShutdownCmd(bot)
                )
        if (config.useEval())
            cb.addCommand(EvalCmd(bot))
        var nogame = false
        if (config.status != OnlineStatus.UNKNOWN)
            cb.setStatus(config.status)
        when {
            config.getGame() == null -> cb.useDefaultGame()
            config.getGame()!!.name.equals("none", ignoreCase = true) -> {
                cb.setGame(null)
                nogame = true
            }
            else -> cb.setGame(config.getGame())
        }
        val client = cb.build()

        if (!config.noGui) {
            try {
                val gui = GUI(bot)
                bot.setGUI(gui)
                gui.init()
            } catch (e: Exception) {
                LOG.error("Could not start GUI. If you are "
                        + "running on a server or in a location where you cannot display a "
                        + "window, please run in nogui mode using the -nogui flag.")
            }

        }

        LOG.info("Loaded config from " + config.configLocation!!)

        // attempt to log in and start
        try {
            JDABuilder(AccountType.BOT)
                    .setToken(config.token)
                    .setAudioEnabled(true)
                    .setGame(if (nogame) null else Game.playing("loading..."))
                    .setStatus(if (config.status == OnlineStatus.INVISIBLE || config.status == OnlineStatus.OFFLINE) OnlineStatus.INVISIBLE else OnlineStatus.DO_NOT_DISTURB)
                    .addEventListener(client)
                    .addEventListener(waiter)
                    .addEventListener(bot)
                    .buildAsync()
        } catch (ex: LoginException) {
            LOG.error(ex.toString() + "\nPlease make sure you are "
                    + "editing the correct config.txt file, and that you have used the "
                    + "correct token (not the 'secret'!)")
        } catch (ex: IllegalArgumentException) {
            LOG.error("Some aspect of the configuration is invalid: $ex")
        }

    }
}
