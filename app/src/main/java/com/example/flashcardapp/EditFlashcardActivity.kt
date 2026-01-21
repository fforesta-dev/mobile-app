package com.example.flashcardapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class EditFlashcardActivity : AppCompatActivity() {

    private lateinit var etEditQuestion: EditText
    private lateinit var etEditAnswer: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnCancelEdit: Button
    private lateinit var tvEditError: TextView

    // Which card we are editing
    private var index: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_flashcard)

        etEditQuestion = findViewById(R.id.etEditQuestion)
        etEditAnswer = findViewById(R.id.etEditAnswer)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnCancelEdit = findViewById(R.id.btnCancelEdit)
        tvEditError = findViewById(R.id.tvEditError)

        // Read extras passed from MainActivity
        index = intent.getIntExtra("index", -1)
        val question = intent.getStringExtra("question") ?: ""
        val answer = intent.getStringExtra("answer") ?: ""

        // Pre-fill fields
        etEditQuestion.setText(question)
        etEditAnswer.setText(answer)

        btnUpdate.setOnClickListener { updateFlashcard() }
        btnCancelEdit.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    // Updates the chosen flashcard in SharedPreferences JSON
    private fun updateFlashcard() {
        val newQuestion = etEditQuestion.text.toString().trim()
        val newAnswer = etEditAnswer.text.toString().trim()

        if (newQuestion.isEmpty() || newAnswer.isEmpty()) {
            tvEditError.text = "Please enter both a question and an answer."
            return
        }

        if (index < 0) {
            tvEditError.text = "Error: missing card index."
            return
        }

        val prefs = getSharedPreferences("flashcard_prefs", MODE_PRIVATE)
        val jsonString = prefs.getString("cards_json", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)

        if (index >= jsonArray.length()) {
            tvEditError.text = "Error: card no longer exists."
            return
        }

        val obj = JSONObject().apply {
            put("question", newQuestion)
            put("answer", newAnswer)
        }

        // Replace the object at the target index
        jsonArray.put(index, obj)

        prefs.edit().putString("cards_json", jsonArray.toString()).apply()

        setResult(RESULT_OK)
        finish()
    }
}
