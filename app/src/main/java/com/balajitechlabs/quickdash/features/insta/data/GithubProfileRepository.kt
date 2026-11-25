/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/insta/data
 * File: GithubProfileRepository.kt
 * Description: Repository fetching and caching GitHub user profiles, READMEs, and followers off the UI thread.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.insta.data

import android.util.Log
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.insta.presentation.GithubProfileCache
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubProfileRepository @Inject constructor(
    private val userStore: UserStore
) {
    private val cache = ConcurrentHashMap<String, GithubProfileCache>()

    suspend fun fetchProfile(targetUsername: String): Result<GithubProfileCache> = withContext(Dispatchers.IO) {
        val cacheKey = targetUsername.lowercase()
        cache[cacheKey]?.let { return@withContext Result.success(it) }

        var userConn: HttpURLConnection? = null
        try {
            val token = userStore.githubAccessToken.first()
            userConn = (URL("https://api.github.com/users/$targetUsername").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                if (token.isNotBlank()) setRequestProperty("Authorization", "token $token")
            }

            when (val code = userConn.responseCode) {
                403 -> return@withContext Result.failure(IllegalStateException("RATE_LIMIT"))
                404 -> return@withContext Result.failure(IllegalStateException("NOT_FOUND"))
                !in 200..299 -> return@withContext Result.failure(IllegalStateException("ERROR_$code"))
            }

            val userObj = JSONObject(userConn.inputStream.bufferedReader().use { it.readText() })
            val login = userObj.optString("login", targetUsername)
            val profile = GithubProfileCache(
                login = login,
                name = userObj.optString("name", login),
                avatarUrl = userObj.optString("avatar_url", ""),
                bio = userObj.optString("bio", "No bio description."),
                followers = userObj.optInt("followers", 0),
                following = userObj.optInt("following", 0),
                blogUrl = userObj.optString("blog", ""),
                githubUrl = userObj.optString("html_url", "https://github.com/$login"),
                readmeContent = fetchReadme(login),
                followersList = fetchFollowers(login, token)
            )
            cache[login.lowercase()] = profile
            Result.success(profile)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            userConn?.disconnect()
        }
    }

    private fun fetchReadme(login: String): String {
        val branches = listOf("main", "master")
        for (branch in branches) {
            var conn: HttpURLConnection? = null
            try {
                conn = (URL("https://raw.githubusercontent.com/$login/$login/$branch/README.md")
                    .openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3000
                    readTimeout = 3000
                }
                if (conn.responseCode == 200) {
                    return conn.inputStream.bufferedReader().use { it.readText() }
                }
            } catch (_: Exception) {
                // try next branch
            } finally {
                conn?.disconnect()
            }
        }
        return "No profile README found."
    }

    private fun fetchFollowers(login: String, token: String): List<Pair<String, String>> {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL("https://api.github.com/users/$login/followers?per_page=12")
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
                if (token.isNotBlank()) setRequestProperty("Authorization", "token $token")
            }
            if (conn.responseCode != 200) return emptyList()
            val arr = JsonParser.parseString(conn.inputStream.bufferedReader().use { it.readText() }).asJsonArray
            arr.mapNotNull { el ->
                val obj = el.asJsonObject
                val fLogin = obj.get("login")?.asString.orEmpty()
                val fAvatar = obj.get("avatar_url")?.asString.orEmpty()
                if (fLogin.isNotBlank()) fLogin to fAvatar else null
            }
        } catch (e: Exception) {
            Log.w("GithubProfileRepo", "Failed to fetch followers: ${e.message}")
            emptyList()
        } finally {
            conn?.disconnect()
        }
    }
}
