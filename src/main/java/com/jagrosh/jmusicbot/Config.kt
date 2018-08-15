/*
 * Copyright 2016 John Grosh (jagrosh)
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

import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.swing.JOptionPane

/**
 *
 * @author John Grosh (jagrosh)
 */
class Config(nogui: Boolean) {
    var noGui: Boolean = false
        private set
    var configLocation: String? = null
        private set
    var prefix: String? = null
        private set
    var altPrefix: String? = null
        private set
    var token: String? = null
        private set
    var ownerId: String? = null
        private set
    var success: String? = null
        get() = if (field == null) "\uD83C\uDFB6" else field
    var warning: String? = null
        get() = if (field == null) "\uD83D\uDCA1" else field
    var error: String? = null
        get() = if (field == null) "\uD83D\uDEAB" else field
    private var game: String? = null
    var help: String? = null
        get() = if (field == null) "help" else field
    private var loadingEmoji: String? = null
    private var searchingEmoji: String? = null
    var stay: Boolean = false
        private set
    var dBots: Boolean = false
        private set
    var songInStatus: Boolean = false
        private set
    private var useEval: Boolean = false
    private var npimages: Boolean = false
    var maxSeconds: Long = 0
        private set
    var status = OnlineStatus.UNKNOWN
        private set

    val loading: String
        get() = if (loadingEmoji == null) "\u231A" else loadingEmoji!!

    val searching: String
        get() = if (searchingEmoji == null) "\uD83D\uDD0E" else searchingEmoji!!

    init {
        this.noGui = nogui
        var lines: MutableList<String>
        try {
            configLocation = Paths.get("config.txt").toFile().absolutePath
            lines = Files.readAllLines(Paths.get("config.txt"), StandardCharsets.UTF_8)
            //System.out.println("[INFO] Loading config: "+location);
            for (line in lines) {
                val parts = line.split("=".toRegex(), 2).toTypedArray()
                val key = parts[0].trim { it <= ' ' }.toLowerCase()
                val value = if (parts.size > 1) parts[1].trim { it <= ' ' } else null
                when (key) {
                    "token" -> token = value
                    "prefix" -> prefix = value
                    "altprefix" -> altPrefix = value
                    "owner" -> ownerId = value
                    "success" -> success = value
                    "warning" -> warning = value
                    "error" -> error = value
                    "loading" -> loadingEmoji = value
                    "searching" -> searchingEmoji = value
                    "game" -> game = value
                    "help" -> help = value
                    "songinstatus" -> songInStatus = "true".equals(value!!, ignoreCase = true)
                    "stayinchannel" -> stay = "true".equals(value!!, ignoreCase = true)
                    "eval" -> useEval = "true".equals(value!!, ignoreCase = true)
                    "dbots" -> dBots = "110373943822540800" == value
                    "npimages" -> npimages = "true".equals(value!!, ignoreCase = true)
                    "maxtime" -> try {
                        maxSeconds = java.lang.Long.parseLong(value!!)
                    } catch (ignored: NumberFormatException) {
                    }

                    "status" -> status = OnlineStatus.fromKey(value)
                }
            }
        } catch (ex: IOException) {
            alert("'config.txt' was not found at $configLocation!")
            lines = LinkedList()
        }

        var write = false
        if (token == null || token!!.isEmpty()) {
            token = prompt("Please provide a bot token."
                    + "\nInstructions for obtaining a token can be found here:"
                    + "\nhttps://github.com/jagrosh/MusicBot/wiki/Getting-a-Bot-Token."
                    + "\nBot Token: ")
            if (token == null) {
                alert("No token provided! Exiting.")
                System.exit(0)
            } else {
                lines.add("token=" + token!!)
                write = true
            }
        }
        if (ownerId == null || !ownerId!!.matches("\\d{17,20}".toRegex())) {
            ownerId = prompt("Owner ID was missing, or the provided owner ID is not valid."
                    + "\nPlease provide the User ID of the bot's owner."
                    + "\nInstructions for obtaining your User ID can be found here:"
                    + "\nhttps://github.com/jagrosh/MusicBot/wiki/Finding-Your-User-ID"
                    + "\nOwner User ID: ")
            if (ownerId == null || !ownerId!!.matches("\\d{17,20}".toRegex())) {
                alert("Invalid User ID! Exiting.")
                System.exit(0)
            } else {
                lines.add("owner=" + ownerId!!)
                write = true
            }
        }
        if (write) {
            val builder = StringBuilder()
            lines.forEach { s -> builder.append(s).append("\r\n") }
            try {
                Files.write(Paths.get("config.txt"), builder.toString().trim { it <= ' ' }.toByteArray())
            } catch (ex: IOException) {
                alert("Failed to write new config options to config.txt: " + ex
                        + "\nPlease make sure that the files are not on your desktop or some other restricted area.")
            }

        }
    }

    fun getGame(): Game? {
        if (game == null || game!!.isEmpty())
            return null
        if (game!!.toLowerCase().startsWith("playing"))
            return Game.playing(game!!.substring(7).trim { it <= ' ' })
        if (game!!.toLowerCase().startsWith("listening to"))
            return Game.listening(game!!.substring(12).trim { it <= ' ' })
        if (game!!.toLowerCase().startsWith("listening"))
            return Game.listening(game!!.substring(9).trim { it <= ' ' })
        return if (game!!.toLowerCase().startsWith("watching")) Game.watching(game!!.substring(8).trim { it <= ' ' }) else Game.playing(game!!)
    }

    fun useEval(): Boolean {
        return useEval
    }

    fun useNPImages(): Boolean {
        return npimages
    }

    private fun alert(message: String) {
        if (noGui)
            LOG.warn(message)
        else {
            try {
                JOptionPane.showMessageDialog(null, message, "JMusicBot", JOptionPane.WARNING_MESSAGE)
            } catch (e: Exception) {
                noGui = true
                alert("Switching to nogui mode. You can manually start in nogui mode by including the -nogui flag.")
                alert(message)
            }

        }
    }

    private fun prompt(content: String): String {
        return if (noGui) {
            val scanner = Scanner(System.`in`)
            println(content)
            scanner.next()
        } else {
            return try {
                JOptionPane.showInputDialog(null, content, "JMusicBot", JOptionPane.WARNING_MESSAGE)
            } catch (e: Exception) {
                noGui = true
                alert("Switching to nogui mode. You can manually start in nogui mode by including the -nogui flag.")
                prompt(content)
            }

        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Config::class.java)
    }
}
