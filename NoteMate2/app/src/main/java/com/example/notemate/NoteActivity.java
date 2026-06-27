package com.example.notemate;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// Import untuk Gemini AI
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class NoteActivity extends AppCompatActivity {

    private static final int SPEECH_REQUEST_CODE = 100;

    private EditText editTextTitle;
    private EditText editTextContent;
    private Spinner spinnerCategory;
    private Button btnSave;
    private ImageButton btnBack, btnDelete, btnAiSummarize, btnVoiceInput;
    private ProgressBar progressBarAi;

    private AppDatabase db;
    private int noteId = -1;
    private boolean isEditing = false;
    private boolean isTaskPage = false; // Menentukan apakah ini tugas atau catatan

    private final List<String> categories = Arrays.asList("Umum", "Pribadi", "Kerja", "Kuliah");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        db = AppDatabase.getInstance(this);

        // Inisialisasi View
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextContent = findViewById(R.id.edit_text_content);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSave = findViewById(R.id.btn_save_note);
        btnBack = findViewById(R.id.btn_back);
        btnDelete = findViewById(R.id.btn_delete_note);
        btnAiSummarize = findViewById(R.id.btn_ai_summarize);
        btnVoiceInput = findViewById(R.id.btn_voice_input);
        progressBarAi = findViewById(R.id.progress_bar_ai);

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Cek apakah ini Catatan atau Tugas dari Intent
        isTaskPage = getIntent().getBooleanExtra("IS_TASK", false);

        // Mode Edit
        if (getIntent().hasExtra("NOTE_ID")) {
            isEditing = true;
            noteId = getIntent().getIntExtra("NOTE_ID", -1);
            editTextTitle.setText(getIntent().getStringExtra("NOTE_TITLE"));
            editTextContent.setText(getIntent().getStringExtra("NOTE_CONTENT"));

            String savedCategory = getIntent().getStringExtra("NOTE_CATEGORY");
            if (savedCategory != null) {
                spinnerCategory.setSelection(adapter.getPosition(savedCategory));
            }

            btnSave.setText(isTaskPage ? "Perbarui Tugas" : "Perbarui Catatan");
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnSave.setText(isTaskPage ? "Simpan Tugas" : "Simpan Catatan");
            btnDelete.setVisibility(View.GONE);
        }

        // Listeners
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveNoteToDatabase());
        btnDelete.setOnClickListener(v -> deleteNoteFromDatabase());
        btnAiSummarize.setOnClickListener(v -> summarizeWithAI());
        btnVoiceInput.setOnClickListener(v -> startVoiceRecognition());
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Silakan bicara...");
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input tidak didukung", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String currentText = editTextContent.getText().toString();
                editTextContent.setText(currentText.isEmpty() ? result.get(0) : currentText + " " + result.get(0));
            }
        }
    }

    private void summarizeWithAI() {
        String originalText = editTextContent.getText().toString().trim();
        if (originalText.length() < 20) {
            Toast.makeText(this, "Teks terlalu pendek untuk AI", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBarAi.setVisibility(View.VISIBLE);
        btnAiSummarize.setEnabled(false);

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "AIzaSyA7J--G2fAO1LFpjqfmA03xKJp0SyjaFEQ");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText("Ringkas teks berikut menjadi 3 poin singkat bahasa Indonesia:\n\n" + originalText)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    progressBarAi.setVisibility(View.GONE);
                    btnAiSummarize.setEnabled(true);
                    editTextContent.append("\n\n--- Ringkasan AI ---\n" + result.getText());
                });
            }
            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    progressBarAi.setVisibility(View.GONE);
                    btnAiSummarize.setEnabled(true);
                    Toast.makeText(NoteActivity.this, "Gagal AI", Toast.LENGTH_SHORT).show();
                });
            }
        }, Executors.newSingleThreadExecutor());
    }

    private void saveNoteToDatabase() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Isi tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // Menggunakan 6 parameter: title, content, timestamp, category, isTask, isCompleted
            Note note = new Note(title, content, System.currentTimeMillis(), category, isTaskPage, false);

            if (isEditing) {
                note.setId(noteId);
                db.noteDao().update(note);
            } else {
                db.noteDao().insert(note);
            }

            runOnUiThread(() -> {
                String type = isTaskPage ? "Tugas" : "Catatan";
                Toast.makeText(this, type + (isEditing ? " diperbarui!" : " disimpan!"), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void deleteNoteFromDatabase() {
        if (noteId == -1) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            Note note = new Note(editTextTitle.getText().toString(), editTextContent.getText().toString(),
                    System.currentTimeMillis(), spinnerCategory.getSelectedItem().toString(), isTaskPage, false);
            note.setId(noteId);
            db.noteDao().delete(note);
            runOnUiThread(() -> {
                Toast.makeText(this, (isTaskPage ? "Tugas" : "Catatan") + " dihapus!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}