package com.example.ui.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class OtaUpdateInfo(
    val tagName: String,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isUpdateAvailable: Boolean
)

object OtaUpdateManager {

    /**
     * Checks GitHub API for the latest release in the target repository.
     * e.g. repo: "rfbarzola25/PulseMusicPlayer" or "owner/repository"
     */
    suspend fun checkForUpdates(
        repoPath: String,
        currentVersionName: String
    ): OtaUpdateInfo? = withContext(Dispatchers.IO) {
        val cleanRepo = repoPath.trim().removePrefix("https://github.com/").removePrefix("/")
        if (cleanRepo.isBlank() || !cleanRepo.contains("/")) return@withContext null

        try {
            var jsonStr = ""
            val connection = (URL("https://api.github.com/repos/$cleanRepo/releases/latest").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "PulseMusicPlayer-OTA")
                connectTimeout = 8000
                readTimeout = 8000
            }

            if (connection.responseCode == 200) {
                jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                // Fallback to releases list
                val listConn = (URL("https://api.github.com/repos/$cleanRepo/releases").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "PulseMusicPlayer-OTA")
                    connectTimeout = 8000
                    readTimeout = 8000
                }
                if (listConn.responseCode == 200) {
                    val listStr = listConn.inputStream.bufferedReader().use { it.readText() }
                    val array = JSONArray(listStr)
                    if (array.length() > 0) {
                        jsonStr = array.getJSONObject(0).toString()
                    }
                }
            }

            if (jsonStr.isBlank()) return@withContext null
            val json = JSONObject(jsonStr)

            val tagName = json.optString("tag_name", "v3.0.0")
            val releaseNotes = json.optString("body", "Nueva versión disponible.")
            val assets: JSONArray? = json.optJSONArray("assets")

            var downloadUrl = ""
            if (assets != null && assets.length() > 0) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
                if (downloadUrl.isBlank() && assets.length() > 0) {
                    downloadUrl = assets.getJSONObject(0).optString("browser_download_url", "")
                }
            }

            val onlineVersionClean = tagName.removePrefix("v").trim()
            val currentVersionClean = currentVersionName.removePrefix("v").trim()

            val isNewer = isVersionNewer(onlineVersionClean, currentVersionClean)

            OtaUpdateInfo(
                tagName = tagName,
                versionName = onlineVersionClean,
                downloadUrl = downloadUrl,
                releaseNotes = releaseNotes,
                isUpdateAvailable = isNewer && downloadUrl.isNotBlank()
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun isVersionNewer(online: String, current: String): Boolean {
        try {
            val onlineParts = online.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

            for (i in 0 until maxOf(onlineParts.size, currentParts.size)) {
                val o = onlineParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (o > c) return true
                if (o < c) return false
            }
        } catch (_: Exception) {}
        return false
    }

    /**
     * Downloads the APK via Android's native system DownloadManager and prompts installation.
     */
    fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        versionName: String,
        onStatusMessage: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                onStatusMessage("Permite instalar aplicaciones desconocidas en los ajustes para continuar")
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
        }

        try {
            val destinationFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "Pulse_Update_$versionName.apk"
            )
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Pulse Music Player v$versionName")
                setDescription("Descargando actualización inalámbrica OTA...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationUri(Uri.fromFile(destinationFile))
                setMimeType("application/vnd.android.package-archive")
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            onStatusMessage("Descargando actualización v$versionName...")

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                    if (id == downloadId) {
                        try {
                            context.unregisterReceiver(this)
                        } catch (_: Exception) {}

                        onStatusMessage("Descarga completada. Abriendo instalador...")
                        installApk(context, destinationFile)
                    }
                }
            }

            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            )

        } catch (e: Exception) {
            onStatusMessage("Error al descargar: ${e.localizedMessage}")
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) return

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
