package com.example.growth.utils

// For accessing app resources/files
import android.content.Context
// The PlantPhoto table
import com.example.growth.model.PlantPhoto
// File handling
import java.io.File
// Coroutines for background tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
// FFmpeg libraries for video processing
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

// Creates a singleton helper class for making timelapse videos
object TimeLapseHelper {
    // Runs in the background to create a timelapse, returns true/false whether it succeeded
    suspend fun createTimeLapse(
        context: Context,
        photos: List<PlantPhoto>,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) { // Makes the code run on a background thread
        try {
            // Checks if there are at least 2 photos because the video can't be made with less
            if (photos.size < 2) return@withContext false

            // Create a temporary folder in app's cache to store input photo copies
            val tempDir = File(context.cacheDir, "timelapse_temp_${System.currentTimeMillis()}")
            if (!tempDir.exists()) tempDir.mkdirs() // Makes sure the temp folder exists

            try {
                // Loops through each photo with its position number
                photos.forEachIndexed { index, photo ->
                    val sourceFile = File(photo.photoPath) // Gets the original photo file
                    // Creates a numbered copy in temp folder
                    val destFile = File(tempDir, "${index + 1}.jpg")
                    // Actually copies the file
                    sourceFile.copyTo(destFile, overwrite = true)
                }

                // Build the FFmpeg video creation command
                val cmd = listOf(
                    "-y",
                    "-framerate", "2", // Uses 2 photos per second
                    "-i", "${tempDir.absolutePath}/%d.jpg", // Input photo pattern
                    "-c:v", "mpeg4", // Uses MPEG4 video format
                    "-r", "30", // Output video at 30 frames/sec
                    "-pix_fmt", "yuv420p",
                    "-q:v", "2", // Sets video quality (1-31, lower is better)
                    outputFile.absolutePath
                )

                // Runs the FFmpeg command to make the video
                val session = FFmpegKit.execute(cmd.joinToString(" "))

                // Checks if FFmpeg succeeded
                // If yes, returns true if output filed exists and isn't empty
                // If no, prints error logs and returns false
                if(ReturnCode.isSuccess(session.returnCode)) {
                    return@withContext outputFile.exists() && outputFile.length() > 0
                } else {
                    val error = session.allLogsAsString
                    println("FFmpeg error: $error")
                    return@withContext false
                }
            } finally {
                // Always cleans up temp files, success or fail
                tempDir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}