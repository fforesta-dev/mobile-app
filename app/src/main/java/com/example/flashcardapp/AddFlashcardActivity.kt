package com.example.flashcardapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class AddFlashcardActivity : AppCompatActivity() {

    private lateinit var etQuestion: EditText
    private lateinit var etAnswer: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_flashcard)

        // Connect UI elements from XML to Kotlin variables
        etQuestion = findViewById(R.id.etQuestion)
        etAnswer = findViewById(R.id.etAnswer)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        tvError = findViewById(R.id.tvError)

        // Save button: validate inputs, store card, return to MainActivity
        btnSave.setOnClickListener {
            saveFlashcard()
        }

        // Cancel button: close this screen without saving
        btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    // Validates inputs and writes the new card into SharedPreferences
    private fun saveFlashcard() {
        val question = etQuestion.text.toString().trim()
        val answer = etAnswer.text.toString().trim()

        // Basic validation: both fields must be filled
        if (question.isEmpty() || answer.isEmpty()) {
            tvError.text = getString(R.string.error_empty_fields)
            return
        }

        // Load existing JSON array from SharedPreferences
        val prefs = getSharedPreferences("flashcard_prefs", MODE_PRIVATE)
        val jsonString = prefs.getString("cards_json", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)

        // Add the new card
        val obj = JSONObject()
        obj.put("question", question)
        obj.put("answer", answer)
        jsonArray.put(obj)

        // Save back to SharedPreferences using KTX extension
        prefs.edit {
            putString("cards_json", jsonArray.toString())
        }

        // Indicate success and close this activity
        setResult(RESULT_OK)
        finish()
    }
}