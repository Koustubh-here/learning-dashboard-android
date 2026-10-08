package com.example.learningdashboard.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException

data class CourseDto(
    val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessons: Int
)

interface CourseApi {
    suspend fun getCourses(): List<CourseDto>
}

interface AuthApi {
    suspend fun login(email: String, password: String)
}

/**
 * Mock API backed by assets/courses.json. Behaves like a real call: adds latency and
 * throws IOException when the device is actually offline (airplane mode in the demo
 * triggers the failure path). Swap for a Retrofit implementation without touching upper layers.
 */
class FakeCourseApi(private val context: Context) : CourseApi {
    override suspend fun getCourses(): List<CourseDto> = withContext(Dispatchers.IO) {
        delay(1000)
        if (!isOnline()) throw IOException("No internet connection")
        val json = context.assets.open("courses.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            CourseDto(o.getInt("id"), o.getString("title"), o.getString("instructor"),
                o.getInt("progress"), o.getInt("lessons"))
        }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

class FakeAuthApi : AuthApi {
    override suspend fun login(email: String, password: String) {
        delay(1000)
        if (email.trim().lowercase() != "test@example.com" || password != "password123") {
            throw IllegalArgumentException("Invalid email or password")
        }
    }
}
