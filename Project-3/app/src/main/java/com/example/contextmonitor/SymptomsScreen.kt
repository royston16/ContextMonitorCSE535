

package com.example.contextmonitor



import android.os.Bundle

import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView



class SymptomsScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get heart rate and respiratory rate from Intent
        val heartRate = intent.getIntExtra("heartrate", 0)
        val respiratoryRate = intent.getIntExtra("respiratoryrate", 0)

        setContent {
            SymptomLoggingScreen(heartRate, respiratoryRate)
        }
    }
}

@Composable
fun SymptomLoggingScreen(heartRate: Int, respiratoryRate: Int) {
    val symptoms = listOf(
        "Fever", "Nausea", "Headache", "Diarrhea", "Soar Throat", "Muscle Ache",
        "Loss of Smell or Taste", "Cough", "Shortness of Breath", "Feeling Tired"
    )

    var selectedSymptom by remember { mutableStateOf(symptoms[0]) }
    var ratingMap by remember { mutableStateOf(mutableMapOf<String, Float>()) }
    var currentRating by remember { mutableStateOf(0f) }
    val context = LocalContext.current
    val dbHelper = DatabaseHelper(context)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        BasicText("Symptom Logging Page")

        // Dropdown to select symptom
        DropdownMenu(
            selectedSymptom = selectedSymptom,
            onSymptomSelected = { symptom ->
                selectedSymptom = symptom
                currentRating = ratingMap[symptom] ?: 0f  // Retrieve previously stored rating or 0
            },
            symptoms = symptoms
        )

        Spacer(modifier = Modifier.height(16.dp))

        BasicText("Rate $selectedSymptom")

        AndroidView(
            modifier = Modifier.wrapContentWidth(),
            factory = { context ->
                RatingBar(context).apply {
                    numStars = 5
                    stepSize = 1.0f
                    rating = currentRating  // Initialize with the current rating
                    setOnRatingBarChangeListener { _, ratingValue, _ ->
                        currentRating = ratingValue
                        ratingMap[selectedSymptom] = ratingValue  // Store rating for selected symptom
                    }
                }
            },
            update = { ratingBar -> ratingBar.rating = currentRating }  // Update the rating when symptom changes
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Upload Symptoms Button
        Button(onClick = {
            saveSymptomData(heartRate, respiratoryRate, ratingMap.mapValues { it.value.toInt() }, context, dbHelper)
        }) {
            Text(text = "Upload Symptoms")
        }
    }
}

@Composable
fun DropdownMenu(
    selectedSymptom: String,
    onSymptomSelected: (String) -> Unit,
    symptoms: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { expanded = true }
    ) {
        BasicText(selectedSymptom, modifier = Modifier.padding(16.dp))
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            symptoms.forEach { symptom ->
                DropdownMenuItem(onClick = {
                    onSymptomSelected(symptom)
                    expanded = false
                }, text =  {
                    BasicText(symptom)
                })
            }
        }
    }
}

fun saveSymptomData(
    heartRate: Int,
    respiratoryRate: Int,
    ratingMap: Map<String, Int>,
    context: android.content.Context,
    dbHelper: DatabaseHelper
) {
    val result = dbHelper.insertSymptoms(heartRate, respiratoryRate, ratingMap)
    if (result != -1L) {
        Toast.makeText(context, "Data saved successfully", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Failed to save data", Toast.LENGTH_SHORT).show()
    }
}









