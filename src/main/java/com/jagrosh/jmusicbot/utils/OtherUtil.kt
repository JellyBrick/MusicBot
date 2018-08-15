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
package com.jagrosh.jmusicbot.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
object OtherUtil {

    val latestVersion: String?
        get() {
            try {
                OkHttpClient.Builder().build()
                        .newCall(Request.Builder().get().url("https://api.github.com/repos/jagrosh/MusicBot/releases/latest").build())
                        .execute().use { response ->
                            Objects.requireNonNull<ResponseBody>(response.body()).charStream().use { reader ->
                                val obj = JSONObject(JSONTokener(reader))
                                return obj.getString("tag_name")
                            }
                        }
            } catch (ex: IOException) {
                return null
            } catch (ex: JSONException) {
                return null
            } catch (ex: NullPointerException) {
                return null
            }

        }

    fun imageFromUrl(url: String?): InputStream? {
        if (url == null)
            return null
        try {
            val u = URL(url)
            val urlConnection = u.openConnection()
            urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36")
            return urlConnection.getInputStream()
        } catch (ignored: IOException) {
        } catch (ignored: IllegalArgumentException) {
        }

        return null
    }
}
