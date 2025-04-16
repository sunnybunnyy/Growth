package com.example.growth.utils

import android.content.Context
import com.example.growth.model.PlantPhoto
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode

object TimeLapseHelper {
    suspend fun createTimeLapse(
        context: Context,
        photos: List<PlantPhoto>,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (photos.size < 2) return@withContext false

            // Create a temporary directory for input files
            val tempDir = File(context.cacheDir, "timelapse_temp_${System.currentTimeMillis()}")
            if (!tempDir.exists()) tempDir.mkdirs()

            try {
                // Copy photos to temp directory with sequential numbering

                photos.forEachIndexed { index, photo ->
                    val sourceFile = File(photo.photoPath)
                    val destFile = File(tempDir, "${index + 1}.jpg")
                    sourceFile.copyTo(destFile, overwrite = true)
                }

                // Build FFmpeg command
                val cmd = listOf(
                    "-y",
                    "-framerate", "2", // 2 frames per second
                    "-i", "${tempDir.absolutePath}/%d.jpg", // Input pattern
                    "-c:v", "mpeg4",
                    "-r", "30",
                    "-pix_fmt", "yuv420p",
                    "-q:v", "2", // Quality setting (1-31, lower is better)
                    outputFile.absolutePath
                )

                // Execute FFmpeg command using FFmpegKit
                val session = FFmpegKit.execute(cmd.joinToString(" "))

                // Check if execution was successful
                if(ReturnCode.isSuccess(session.returnCode)) {
                    return@withContext outputFile.exists() && outputFile.length() > 0
                } else {
                    val error = session.allLogsAsString
                    println("FFmpeg error: $error")
                    return@withContext false
                }
            } finally {
                // Clean up temp files
                tempDir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}