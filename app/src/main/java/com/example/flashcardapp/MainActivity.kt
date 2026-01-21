package com.example.flashcardapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isGone
import androidx.core.view.isVisible
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    // Holds all flashcards currently in memory
    private val flashcards: MutableList<Flashcard> = mutableListOf()

    // Tracks which card is being shown
    private var currentIndex: Int = 0

    // UI references
    private lateinit var tvQuestion: TextView
    private lateinit var tvAnswer: TextView
    private lateinit var btnToggleAnswer: Button
    private lateinit var btnNext: Button
    private lateinit var btnAdd: Button
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    // Launcher used to receive result from AddFlashcardActivity or EditFlashcardActivity
    private val addCardLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // If user saved or edited a card, reload from storage and show it
        if (result.resultCode == RESULT_OK) {
            loadCards()
            if (flashcards.isNotEmpty()) {
                if (currentIndex >= flashcards.size) currentIndex = 0
                showCard(currentIndex)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect UI elements from XML to Kotlin variables
        tvQuestion = findViewById(R.id.tvQuestion)
        tvAnswer = findViewById(R.id.tvAnswer)
        btnToggleAnswer = findViewById(R.id.btnToggleAnswer)
        btnNext = findViewById(R.id.btnNext)
        btnAdd = findViewById(R.id.btnAdd)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        // Load cards from storage; if none exist, seed starter cards
        loadCards()
        if (flashcards.isEmpty()) {
            seedStarterCards()
            saveCards()
        }

        // Show the first card
        showCard(currentIndex)

        // Button: show/hide the answer text
        btnToggleAnswer.setOnClickListener {
            toggleAnswerVisibility()
        }

        // Button: advance to next card
        btnNext.setOnClickListener {
            goToNextCard()
        }

        // Button: navigate to the Add screen
        btnAdd.setOnClickListener {
            openAddCardScreen()
        }

        btnEdit.setOnClickListener {
            openEditCardScreen()
        }

        btnDelete.setOnClickListener {
            deleteCurrentCard()
        }
    }

    // Displays the question/answer for the given index
    private fun showCard(index: Int) {
        val card = flashcards[index]
        tvQuestion.text = card.question
        tvAnswer.text = card.answer

        // Reset answer visibility each time a new card is shown
        tvAnswer.isGone = true
        btnToggleAnswer.text = getString(R.string.show_answer)
    }

    // Toggles between showing and hiding the answer
    private fun toggleAnswerVisibility() {
        if (tvAnswer.isGone) {
            tvAnswer.isVisible = true
            btnToggleAnswer.text = getString(R.string.hide_answer)
        } else {
            tvAnswer.isGone = true
            btnToggleAnswer.text = getString(R.string.show_answer)
        }
    }

    // Moves to the next card and updates the UI
    private fun goToNextCard() {
        if (flashcards.isEmpty()) return
        currentIndex = (currentIndex + 1) % flashcards.size
        showCard(currentIndex)
    }

    // Opens the screen where the user can add a new flashcard
    private fun openAddCardScreen() {
        val intent = Intent(this, AddFlashcardActivity::class.java)
        addCardLauncher.launch(intent)
    }

    // Opens the Edit screen and passes the current card index + values
    private fun openEditCardScreen() {
        if (flashcards.isEmpty()) return

        val card = flashcards[currentIndex]
        val intent = Intent(this, EditFlashcardActivity::class.java).apply {
            putExtra("index", currentIndex)
            putExtra("question", card.question)
            putExtra("answer", card.answer)
        }
        addCardLauncher.launch(intent)
    }

    // Deletes the current card, saves, and updates the UI safely
    private fun deleteCurrentCard() {
        if (flashcards.isEmpty()) return

        flashcards.removeAt(currentIndex)
        saveCards()

        if (flashcards.isEmpty()) {
            tvQuestion.text = getString(R.string.no_cards_message)
            tvAnswer.text = ""
            tvAnswer.isGone = true
            btnToggleAnswer.text = getString(R.string.show_answer)
            currentIndex = 0
            return
        }

        // Keep index in range after deletion
        if (currentIndex >= flashcards.size) currentIndex = 0
        showCard(currentIndex)
    }

    // Adds some starter cards the first time the app runs
    private fun seedStarterCards() {
        flashcards.clear()
        flashcards.add(Flashcard("How do you say “Hello” in Italian?", "Ciao"))
        flashcards.add(Flashcard("How do you say “Good morning” in Italian?", "Buongiorno"))
        flashcards.add(Flashcard("How do you say “Good evening” in Italian?", "Buonasera"))
        flashcards.add(Flashcard("How do you say “Thank you” in Italian?", "Grazie"))
        flashcards.add(Flashcard("How do you say “Please” in Italian?", "Per favore"))
        flashcards.add(Flashcard("How do you say “Yes” in Italian?", "Sì"))
        flashcards.add(Flashcard("How do you say “No” in Italian?", "No"))
        flashcards.add(Flashcard("How do you say “My name is…” in Italian?", "Mi chiamo…"))
        flashcards.add(Flashcard("How do you say “Nice to meet you” in Italian?", "Piacere di conoscerti"))
        flashcards.add(Flashcard("How do you say “Goodbye” in Italian?", "Arrivederci"))
    }

    // Loads flashcards from SharedPreferences (JSON format)
    private fun loadCards() {
        flashcards.clear()
        val prefs = getSharedPreferences("flashcard_prefs", MODE_PRIVATE)
        val jsonString = prefs.getString("cards_json", "[]") ?: "[]"

        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val q = obj.getString("question")
            val a = obj.getString("answer")
            flashcards.add(Flashcard(q, a))
        }
    }

    // Saves flashcards to SharedPreferences (JSON format)
    private fun saveCards() {
        val jsonArray = JSONArray()
        for (card in flashcards) {
            val obj = JSONObject()
            obj.put("question", card.question)
            obj.put("answer", card.answer)
            jsonArray.put(obj)
        }

        val prefs = getSharedPreferences("flashcard_prefs", MODE_PRIVATE)
        prefs.edit {
            putString("cards_json", jsonArray.toString())
        }
    }
}