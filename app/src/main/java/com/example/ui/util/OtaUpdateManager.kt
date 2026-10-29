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

data class OtaCheckResult(
    val info: OtaUpdateInfo?,
    val statusMessage: String,
    val isSuccess: Boolean
)

data class OtaUpdateInfo(
    val tagName: String,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isUpdateAvailable: Boolean
)

object OtaUpdateManager {

    /**
     * Checks GitHub API for the latest release in the target repository with detailed diagnostics.
     */
    suspend fun checkUpdateStatus(
        repoPath: String,
        currentVersionName: String
    ): OtaCheckResult = withContext(Dispatchers.IO) {
        val cleanRepo = repoPath.trim()
            .removePrefix("https://github.com/")
            .removePrefix("http://github.com/")
            .removePrefix("/")
            .ifBlank { "pincha1212/Pulse_appMusic" }

        if (!cleanRepo.contains("/")) {
            return@withContext OtaCheckResult(null, "Ruta de repositorio no válida.", false)
        }

        try {
            var jsonStr = ""
            var lastResponseCode = 0

            // 1. Intentar /releases/latest
            try {
                val conn = (URL("https://api.github.com/repos/$cleanRepo/releases/latest").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "PulseMusicPlayer-OTA")
                    connectTimeout = 8000
                    readTimeout = 8000
                }
                lastResponseCode = conn.responseCode
                if (lastResponseCode == 200) {
                    jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                }
            } catch (_: Exception) {}

            // 2. Fallback a lista general de /releases
            if (jsonStr.isBlank()) {
                try {
                    val listConn = (URL("https://api.github.com/repos/$cleanRepo/releases").openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        instanceFollowRedirects = true
                        setRequestProperty("Accept", "application/vnd.github.v3+json")
                        setRequestProperty("User-Agent", "PulseMusicPlayer-OTA")
                        connectTimeout = 8000
                        readTimeout = 8000
                    }
                    lastResponseCode = listConn.responseCode
                    if (lastResponseCode == 200) {
                        val listStr = listConn.inputStream.bufferedReader().use { it.readText() }
                        val array = JSONArray(listStr)
                        if (array.length() > 0) {
                            jsonStr = array.getJSONObject(0).toString()
                        }
                    }
                } catch (_: Exception) {}
            }

            if (jsonStr.isBlank()) {
                // Si la API REST de GitHub devolvió 403 (Rate Limit) o falló,
                // consultar la ruta web pública (sin límite de 60 req/hora)
                val webResult = checkViaWebEndpoint(cleanRepo, currentVersionName)
                if (webResult != null) {
                    return@withContext webResult
                }

                val errorMsg = when (lastResponseCode) {
                    404 -> "No se encontraron releases publicadas aún en GitHub."
                    403 -> "Límite de consultas a GitHub excedido temporalmente. Espera unos minutos."
                    else -> "No hay releases disponibles o GitHub no respondió."
                }
                return@withContext OtaCheckResult(null, errorMsg, false)
            }

            val json = JSONObject(jsonStr)
            val rawTag = json.optString("tag_name", "")
            val rawName = json.optString("name", "")
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

            // Extraer versión limpia usando regex para contemplar tags como 'v4.2.1', 'true', o nombres
            val versionRegex = Regex("""(\d+\.\d+(\.\d+)?)""")
            val parsedOnlineVersion = versionRegex.find(rawTag)?.value
                ?: versionRegex.find(rawName)?.value
                ?: rawTag.removePrefix("v").trim()

            val currentClean = versionRegex.find(currentVersionName)?.value ?: currentVersionName.removePrefix("v").trim()
            val isNewer = isVersionNewer(parsedOnlineVersion, currentClean)

            val displayTag = if (rawTag.isNotBlank() && rawTag != "true") rawTag else "v$parsedOnlineVersion"

            val info = OtaUpdateInfo(
                tagName = displayTag,
                versionName = parsedOnlineVersion,
                downloadUrl = downloadUrl,
                releaseNotes = releaseNotes,
                isUpdateAvailable = isNewer && downloadUrl.isNotBlank()
            )

            val message = if (downloadUrl.isBlank()) {
                "Se detectó $displayTag pero aún no se adjuntó el archivo APK compilado en GitHub."
            } else if (isNewer) {
                "¡Nueva versión $displayTag disponible para instalar!"
            } else {
                "¡Pulse v$currentClean ya está en la versión más reciente!"
            }

            OtaCheckResult(info, message, true)
        } catch (e: Exception) {
            val errorText = when (e) {
                is java.net.UnknownHostException -> "Sin conexión a internet o sin acceso a GitHub."
                is java.net.SocketTimeoutException -> "Tiempo de espera agotado al conectar con GitHub."
                else -> "Error de conexión: ${e.localizedMessage ?: "Fallo de red"}"
            }
            OtaCheckResult(null, errorText, false)
        }
    }

    suspend fun checkForUpdates(
        repoPath: String,
        currentVersionName: String
    ): OtaUpdateInfo? = checkUpdateStatus(repoPath, currentVersionName).info

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
     * Consulta pública web directa de GitHub (releases/latest y releases.atom)
     * para bypass total del límite de 60 consultas/hora de la API REST.
     */
    private fun checkViaWebEndpoint(cleanRepo: String, currentVersionName: String): OtaCheckResult? {
        try {
            // A) Redirección web: https://github.com/$cleanRepo/releases/latest
            var detectedTag = ""
            try {
                val webConn = (URL("https://github.com/$cleanRepo/releases/latest").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    instanceFollowRedirects = false
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    connectTimeout = 8000
                    readTimeout = 8000
                }
                val redirectLoc = webConn.getHeaderField("Location") ?: ""
                if (redirectLoc.contains("/releases/tag/")) {
                    detectedTag = redirectLoc.substringAfter("/releases/tag/").trim()
                }
            } catch (_: Exception) {}

            // B) Si no hay redirect, consultar feed público Atom
            if (detectedTag.isBlank()) {
                try {
                    val atomConn = (URL("https://github.com/$cleanRepo/releases.atom").openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                        connectTimeout = 8000
                        readTimeout = 8000
                    }
                    if (atomConn.responseCode == 200) {
                        val xml = atomConn.inputStream.bufferedReader().use { it.readText() }
                        val tagMatch = Regex("""/releases/tag/([^"'\s<]+)""").find(xml)
                        if (tagMatch != null) {
                            detectedTag = tagMatch.groupValues[1]
                        }
                    }
                } catch (_: Exception) {}
            }

            if (detectedTag.isBlank()) return null

            // C) Verificar APKs posibles: app-release.apk, app-debug.apk
            val candidates = listOf("app-release.apk", "app-debug.apk", "app-release-unsigned.apk")
            var verifiedDownloadUrl = ""
            for (filename in candidates) {
                val candidateUrl = "https://github.com/$cleanRepo/releases/download/$detectedTag/$filename"
                try {
                    val headConn = (URL(candidateUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "HEAD"
                        instanceFollowRedirects = false
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                        connectTimeout = 5000
                        readTimeout = 5000
                    }
                    if (headConn.responseCode in listOf(200, 301, 302)) {
                        verifiedDownloadUrl = candidateUrl
                        break
                    }
                } catch (_: Exception) {}
            }

            if (verifiedDownloadUrl.isBlank()) {
                // Si HEAD falló por restricciones de red, fallback a download url estándar
                verifiedDownloadUrl = "https://github.com/$cleanRepo/releases/download/$detectedTag/app-debug.apk"
            }

            val versionRegex = Regex("""(\d+\.\d+(\.\d+)?)""")
            val parsedVersion = versionRegex.find(detectedTag)?.value ?: detectedTag.removePrefix("v").trim()
            val currentClean = versionRegex.find(currentVersionName)?.value ?: currentVersionName.removePrefix("v").trim()
            val isNewer = isVersionNewer(parsedVersion, currentClean)

            val displayTag = if (detectedTag.isNotBlank() && detectedTag != "true") detectedTag else "v$parsedVersion"

            val info = OtaUpdateInfo(
                tagName = displayTag,
                versionName = parsedVersion,
                downloadUrl = verifiedDownloadUrl,
                releaseNotes = "Actualización detectada desde GitHub Releases.",
                isUpdateAvailable = isNewer
            )

            val msg = if (isNewer) {
                "¡Nueva versión $displayTag disponible!"
            } else {
                "¡Pulse v$currentClean ya está en la versión más reciente!"
            }

            return OtaCheckResult(info, msg, true)
        } catch (_: Exception) {
            return null
        }
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
