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
package com.jagrosh.jmusicbot

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class Settings {

    var textId: Long = 0
    var voiceId: Long = 0
    var roleId: Long = 0
    var volume: Int = 0
    var defaultPlaylist: String? = null
    var repeatMode: Boolean = false

    constructor(textId: String?, voiceId: String?, roleId: String?, volume: Int, defaultPlaylist: String?, repeatMode: Boolean) {
        try {
            this.textId = java.lang.Long.parseLong(textId)
        } catch (e: NumberFormatException) {
            this.textId = 0
        }

        try {
            this.voiceId = java.lang.Long.parseLong(voiceId)
        } catch (e: NumberFormatException) {
            this.voiceId = 0
        }

        try {
            this.roleId = java.lang.Long.parseLong(roleId)
        } catch (e: NumberFormatException) {
            this.roleId = 0
        }

        this.volume = volume
        this.defaultPlaylist = defaultPlaylist
        this.repeatMode = repeatMode
    }

    private constructor() {
        this.textId = 0.toLong()
        this.voiceId = 0.toLong()
        this.roleId = 0.toLong()
        this.volume = 100
        this.defaultPlaylist = null
        this.repeatMode = false
    }

    companion object {
        val DEFAULT_SETTINGS = Settings()
    }
}
