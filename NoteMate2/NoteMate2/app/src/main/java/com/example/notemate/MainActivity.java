package com.example.notemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<Note> noteList;
    private TextView tvEmptyMessage;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi Database
        db = AppDatabase.getInstance(this);

        // Inisialisasi View
        recyclerView = findViewById(R.id.notes_recycler_view);
        tvEmptyMessage = findViewById(R.id.tv_empty_state);
        FloatingActionButton fab = findViewById(R.id.fab_add_note);

        noteList = new ArrayList<>();

        // Setup Adapter dengan Listener untuk klik item (Edit Catatan)
        adapter = new NotesAdapter(noteList, note -> {
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            // Mengirim data catatan yang diklik ke NoteActivity
            intent.putExtra("NOTE_ID", note.getId());
            intent.putExtra("NOTE_TITLE", note.getTitle());
            intent.putExtra("NOTE_CONTENT", note.getContent());
            startActivity(intent);
        });

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Klik FAB untuk tambah catatan baru
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Memuat ulang data setiap kali kembali ke halaman ini
        loadNotesFromDatabase();
    }

    private void loadNotesFromDatabase() {
        // Menjalankan query database di background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Note> notes = db.noteDao().getAllNotes();

            // Kembali ke main thread untuk memperbarui UI
            runOnUiThread(() -> {
                noteList.clear();
                noteList.addAll(notes);
                adapter.notifyDataSetChanged();
                checkEmptyList();
            });
        });
    }

    private void checkEmptyList() {
        // Logika menampilkan pesan "Daftar kosong" jika tidak ada data
        if (noteList.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}