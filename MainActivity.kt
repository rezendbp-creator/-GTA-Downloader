package com.example.fileloader

import android.app.DownloadManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var downloader: Downloader
    private lateinit var urlInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var progress: ProgressBar
    private lateinit var status: TextView
    private var trackingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        downloader = Downloader(this)
        urlInput = findViewById(R.id.etUrl)
        nameInput = findViewById(R.id.etName)
        progress = findViewById(R.id.progressBar)
        status = findViewById(R.id.tvStatus)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val url = urlInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            if (url.isBlank() || name.isBlank()) {
                Toast.makeText(this, "Введи HTTPS-ссылку и имя файла.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val id = downloader.enqueue(FileSpec(url, name))
                progress.progress = 0
                status.text = "Загрузка добавлена в очередь…"
                track(id)
            } catch (e: Exception) {
                status.text = "Ошибка: ${e.message ?: "не удалось начать загрузку"}"
            }
        }
    }

    private fun track(id: Long) {
        trackingJob?.cancel()
        trackingJob = lifecycleScope.launch {
            while (isActive) {
                val cursor = downloader.manager().query(downloader.query(id))
                cursor.use { c ->
                    if (!c.moveToFirst()) {
                        status.text = "Загрузка не найдена."
                        return@launch
                    }
                    val downloaded = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val state = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    if (total > 0) {
                        progress.progress = ((downloaded * 100L) / total).toInt().coerceIn(0, 100)
                        status.text = "${formatBytes(downloaded)} / ${formatBytes(total)}"
                    } else {
                        status.text = "Загрузка… ${formatBytes(downloaded)}"
                    }
                    when (state) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            progress.progress = 100
                            status.text = "Готово! Файл сохранён в Downloads."
                            return@launch
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            status.text = "Ошибка загрузки (код $reason)."
                            return@launch
                        }
                        DownloadManager.STATUS_PAUSED -> status.text = "Загрузка приостановлена системой…"
                    }
                }
                delay(700)
            }
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "неизвестно"
        if (bytes == 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var index = 0
        while (value >= 1024 && index < units.lastIndex) { value /= 1024; index++ }
        return "%.1f %s".format(value, units[index])
    }

    override fun onDestroy() {
        trackingJob?.cancel()
        super.onDestroy()
    }
}
