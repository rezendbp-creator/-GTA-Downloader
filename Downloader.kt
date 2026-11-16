package com.example.fileloader

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

class Downloader(context: Context) {
    private val manager = context.applicationContext
        .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun enqueue(spec: FileSpec): Long {
        val uri = Uri.parse(spec.url)
        require(uri.scheme == "https" && !uri.host.isNullOrBlank()) {
            "Укажи корректную HTTPS-ссылку."
        }
        val forbidden = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
        val safeName = spec.name.map { if (it in forbidden) '_' else it }.joinToString("").trim()
        require(safeName.isNotBlank() && safeName != "." && safeName != "..") {
            "Укажи корректное имя файла."
        }
        val request = DownloadManager.Request(uri)
            .setTitle("GTA Downloader")
            .setDescription(safeName)
            .setMimeType("application/octet-stream")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
        return manager.enqueue(request)
    }

    fun query(id: Long) = DownloadManager.Query().setFilterById(id)
    fun manager() = manager
}
