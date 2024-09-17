package com.example.contextmonitor

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private lateinit var btnSelectVideo: Button
    private lateinit var tvHeartRate: TextView
    private lateinit var btnReadCSV: Button
    private lateinit var tvRespiratoryRate: TextView
    private lateinit var videoPicker: ActivityResultLauncher<Intent>
    private lateinit var csvPicker: ActivityResultLauncher<Intent>

    // Lists to hold the accelerometer values from the three files
    private val accelValuesX = mutableListOf<Float>()
    private val accelValuesY = mutableListOf<Float>()
    private val accelValuesZ = mutableListOf<Float>()

    // Variables to track which file is being selected
    private var fileSelectionStep = 0


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Realm.init(this)
        setContentView(R.layout.activity_main)
        // Set references to UI elements

        btnSelectVideo = findViewById(R.id.btnSelectVideo)
        tvHeartRate = findViewById(R.id.tvHeartRate)
        btnReadCSV = findViewById(R.id.btnReadCSV)
        tvRespiratoryRate = findViewById(R.id.tvRespiratoryRate)

        // Video Picker
        videoPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    lifecycleScope.launch {
                        val heartRate = heartRateCalculator(it)
                        tvHeartRate.text = "Heart Rate: $heartRate BPM"
                    }
                }
            }
        }

        // Initialize CSV picker
        csvPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    lifecycleScope.launch {
                        when (fileSelectionStep) {
                            0 -> {
                                // Read CSV for X-axis values
                                readCSV(it, contentResolver, accelValuesX)
                                fileSelectionStep = 1
                                promptForNextFile("Y")
                            }
                            1 -> {
                                // Read CSV for Y-axis values
                                readCSV(it, contentResolver, accelValuesY)
                                fileSelectionStep = 2
                                promptForNextFile("Z")
                            }
                            2 -> {
                                // Read CSV for Z-axis values
                                readCSV(it, contentResolver, accelValuesZ)
                                fileSelectionStep = 0 // Reset for next operation
                                computeAndDisplayRespiratoryRate()
                            }
                        }
                    }
                }
            }
        }

        // Button Listener for Video Picker
        btnSelectVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            videoPicker.launch(intent)
            Toast.makeText(
                this,
                "Please select a video for heart rate computation.",
                Toast.LENGTH_LONG
            ).show()
        }
        // Set click listener for CSV button to select CSV files for respiratory rate computation
        btnReadCSV.setOnClickListener {
            promptForNextFile("X")
        }
        // Button to navigate to SymptomsScreen
        val symptomsButton = findViewById<Button>(R.id.btnSymptoms)
        symptomsButton.setOnClickListener {
            // Heart rate and respiratory rate TextViews
            val heartRate = extractHeartRate(tvHeartRate)
            val respiratoryRate = extractRespiratoryRate(tvRespiratoryRate)


            val intent = Intent(this, SymptomsScreen::class.java)
            // Pass heart rate and respiratory rate to SymptomsScreen
            intent.putExtra("heartrate", heartRate)
            intent.putExtra("respiratoryrate", respiratoryRate)
            startActivity(intent)
        }

    }

    // Prompt to select the next CSV file for respiratory rate calculation
    private fun promptForNextFile(coordinate: String) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        csvPicker.launch(intent)
        Toast.makeText(
            this,
            "Please select the CSV file for $coordinate coordinate.",
            Toast.LENGTH_LONG
        ).show()

    }


    // Function to read CSV data for accelerometer values
    private suspend fun readCSV(uri: Uri, contentResolver: ContentResolver, accelValues: MutableList<Float>) {
        withContext(Dispatchers.IO) {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.forEachLine { line ->
                            val value = line.toFloatOrNull()
                            value?.let { accelValues.add(it) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CSVReader", "Error reading CSV: ${e.message}")
            }
        }
    }

    // Compute and display respiratory rate based on accelerometer data
    private fun computeAndDisplayRespiratoryRate() {
        val respiratoryRate = respiratoryRateCalculator(accelValuesX, accelValuesY, accelValuesZ)
        tvRespiratoryRate.text = "Respiratory Rate: $respiratoryRate breaths/min"
    }


    // Extract heart rate from TextView
    private fun extractHeartRate(textView: TextView): Int {
        return textView.text.toString()
            .substringAfter(":")
            .substringBefore("BPM")
            .trim()
            .toIntOrNull() ?: 0
    }

    // Extract respiratory rate from TextView
    private fun extractRespiratoryRate(textView: TextView): Int {
        // Extract the respiratory rate after the colon and before "breaths/min"
        return textView.text.toString()
            .substringAfter(":")
            .substringBefore("breaths/min")
            .trim()
            .toIntOrNull() ?: 0
    }


    // Heart rate calculation from video
    @RequiresApi(Build.VERSION_CODES.P)
    private suspend fun heartRateCalculator(uri: Uri): Int {
        return withContext(Dispatchers.IO) {
            var result = 0
            val retriever = MediaMetadataRetriever()
            val frameList = ArrayList<Bitmap>()
            try {
                val fileDescriptor = contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor
                fileDescriptor?.let {
                    retriever.setDataSource(it)
                    val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                    val frameDuration = min(duration!!.toInt(), 425)
                    var i = 10
                    while (i < frameDuration) {
                        val bitmap = retriever.getFrameAtIndex(i)
                        bitmap?.let { frameList.add(it) }
                        i += 15
                    }
                }
            } finally {
                retriever.release()
                var redBucket: Long
                var pixelCount: Long = 0
                val a = mutableListOf<Long>()
                for (i in frameList) {
                    redBucket = 0
                    for (y in 350 until 450) {
                        for (x in 350 until 450) {
                            val c: Int = i.getPixel(x, y)
                            pixelCount++
                            redBucket += Color.red(c) + Color.blue(c) + Color.green(c)
                        }
                    }
                    a.add(redBucket)
                }
                val b = mutableListOf<Long>()
                for (i in 0 until a.lastIndex - 5) {
                    val temp = (a[i] + a[i + 1] + a[i + 2] + a[i + 3] + a[i + 4]) / 4
                    b.add(temp)
                }
                var x = b[0]
                var count = 0
                for (i in 1 until b.lastIndex) {
                    val p = b[i]
                    if ((p - x) > 3500) {
                        count++
                    }
                    x = b[i]
                }
                result = ((count.toFloat()) * 60).toInt() / 4
            }
            result
        }
    }
    // Respiratory rate calculator using accelerometer data
    private fun respiratoryRateCalculator(
        accelValuesX: MutableList<Float>,
        accelValuesY: MutableList<Float>,
        accelValuesZ: MutableList<Float>
    ): Int {
        var previousValue = 10f
        var k = 0
        for (i in 11 until accelValuesY.size) {
            val currentValue = sqrt(
                accelValuesZ[i].toDouble().pow(2.0) +
                        accelValuesX[i].toDouble().pow(2.0) +
                        accelValuesY[i].toDouble().pow(2.0)
            ).toFloat()
            if (abs(previousValue - currentValue) > 0.15) {
                k++
            }
            previousValue = currentValue
        }
        val ret = (k.toDouble() / 45.0)
        return (ret * 30).toInt()
    }
}


