package com.example.notemate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Import untuk Gemini AI
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executors;

public class NoteActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextContent;
    private Button btnSave;
    private ImageButton btnBack;
    private ImageButton btnDelete;
    private ImageButton btnAiSummarize; // Tombol AI
    private ProgressBar progressBarAi; // Loading indicator

    private AppDatabase db;
    private int noteId = -1;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // 1. Inisialisasi Database
        db = AppDatabase.getInstance(this);

        // 2. Inisialisasi View
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextContent = findViewById(R.id.edit_text_content);
        btnSave = findViewById(R.id.btn_save_note);
        btnBack = findViewById(R.id.btn_back);
        btnDelete = findViewById(R.id.btn_delete_note);
        btnAiSummarize = findViewById(R.id.btn_ai_summarize);
        progressBarAi = findViewById(R.id.progress_bar_ai);

        // 3. Cek apakah sedang mengedit catatan yang sudah ada
        if (getIntent().hasExtra("NOTE_ID")) {
            isEditing = true;
            noteId = getIntent().getIntExtra("NOTE_ID", -1);
            editTextTitle.setText(getIntent().getStringExtra("NOTE_TITLE"));
            editTextContent.setText(getIntent().getStringExtra("NOTE_CONTENT"));
            btnSave.setText("Perbarui");
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // Listener Tombol
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveNoteToDatabase());
        btnDelete.setOnClickListener(v -> deleteNoteFromDatabase());
        btnAiSummarize.setOnClickListener(v -> summarizeWithAI());
    }

    private void summarizeWithAI() {
        String originalText = editTextContent.getText().toString().trim();

        if (originalText.isEmpty() || originalText.length() < 20) {
            Toast.makeText(this, "Teks terlalu pendek untuk diringkas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading
        progressBarAi.setVisibility(View.VISIBLE);
        btnAiSummarize.setEnabled(false);

        // Inisialisasi Gemini Model (Ganti API KEY di sini)
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "AIzaSyA7J--G2fAO1LFpjqfmA03xKJp0SyjaFEQ");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Membuat instruksi untuk AI
        Content content = new Content.Builder()
                .addText("Ringkas teks catatan berikut menjadi maksimal 3 poin singkat dalam bahasa Indonesia:\n\n" + originalText)
                .build();

        // Panggil AI secara asynchronous
        com.google.firebase.crashlytics.buildtools.reloc.com.google.common.util.concurrent.ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new androidx.test.espresso.web.util.concurrent.FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                runOnUiThread(() -> {
                    progressBarAi.setVisibility(View.GONE);
                    btnAiSummarize.setEnabled(true);

                    // Menambahkan hasil ringkasan ke dalam EditText
                    editTextContent.append("\n\n--- Ringkasan AI ---\n" + resultText);
                    Toast.makeText(NoteActivity.this, "AI berhasil meringkas!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    progressBarAi.setVisibility(View.GONE);
                    btnAiSummarize.setEnabled(true);
                    Toast.makeText(NoteActivity.this, "Gagal memanggil AI: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }, Executors.newSingleThreadExecutor());
    }

    private void saveNoteToDatabase() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Catatan kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            Note note = new Note(title, content, System.currentTimeMillis());

            if (isEditing) {
                note.setId(noteId);
                db.noteDao().update(note);
            } else {
                db.noteDao().insert(note);
            }

            runOnUiThread(() -> {
                String msg = isEditing ? "Catatan diperbarui!" : "Catatan disimpan!";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void deleteNoteFromDatabase() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Note note = new Note(editTextTitle.getText().toString(),
                    editTextContent.getText().toString(),
                    System.currentTimeMillis());
            note.setId(noteId);

            db.noteDao().delete(note);

            runOnUiThread(() -> {
                Toast.makeText(this, "Catatan dihapus!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}