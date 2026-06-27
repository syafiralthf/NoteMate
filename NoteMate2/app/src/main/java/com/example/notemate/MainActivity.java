package com.example.notemate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<Note> noteList;
    private TextView tvEmptyMessage, tvTitlePage;
    private AppDatabase db;
    private SearchView searchView;
    private ChipGroup chipGroupFilter;
    private FloatingActionButton fab;
    private BottomNavigationView bottomNav;

    // Variabel untuk melacak halaman mana yang sedang aktif
    private boolean isTaskPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        // Inisialisasi View
        tvTitlePage = findViewById(R.id.tv_title_page);
        recyclerView = findViewById(R.id.notes_recycler_view);
        tvEmptyMessage = findViewById(R.id.tv_empty_state);
        searchView = findViewById(R.id.search_view);
        chipGroupFilter = findViewById(R.id.chip_group_filter);
        fab = findViewById(R.id.fab_add_note);
        bottomNav = findViewById(R.id.bottom_navigation);

        // Sembunyikan UI sebelum login
        setUIAppVisibility(View.GONE);
        checkBiometricAuth();
    }

    private void checkBiometricAuth() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(MainActivity.this, "Akses ditolak", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                setUIAppVisibility(View.VISIBLE);
                setupAppLogic();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Keamanan NoteMate")
                .setSubtitle("Gunakan wajah atau sidik jari Anda")
                .setNegativeButtonText("Batal")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void setupAppLogic() {
        noteList = new ArrayList<>();
        adapter = new NotesAdapter(noteList, note -> {
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtra("NOTE_ID", note.getId());
            intent.putExtra("NOTE_TITLE", note.getTitle());
            intent.putExtra("NOTE_CONTENT", note.getContent());
            intent.putExtra("NOTE_CATEGORY", note.getCategory());
            intent.putExtra("IS_TASK", note.isTask());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // --- Logika Bottom Navigation (Pindah Halaman) ---
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_notes) {
                isTaskPage = false;
                tvTitlePage.setText("NoteMate");
                searchView.setQueryHint("Cari catatan...");
                chipGroupFilter.setVisibility(View.VISIBLE); // Filter hanya untuk catatan
                loadNotesFromDatabase();
                return true;
            } else if (id == R.id.nav_tasks) {
                isTaskPage = true;
                tvTitlePage.setText("Daftar Tugas");
                searchView.setQueryHint("Cari tugas...");
                chipGroupFilter.setVisibility(View.GONE); // Sembunyikan filter di hal tugas
                loadNotesFromDatabase();
                return true;
            }
            return false;
        });

        // Logika Pencarian
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchNotesFromDatabase(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchNotesFromDatabase(newText);
                return true;
            }
        });

        // Logika Filter Kategori
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                loadNotesFromDatabase();
            } else {
                Chip selectedChip = findViewById(checkedId);
                if (selectedChip != null) {
                    filterByCategoryFromDatabase(selectedChip.getText().toString());
                }
            }
        });

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtra("IS_TASK", isTaskPage); // Kirim status apakah membuat catatan atau tugas
            startActivity(intent);
        });

        loadNotesFromDatabase();
    }

    private void setUIAppVisibility(int visibility) {
        findViewById(R.id.header_layout).setVisibility(visibility);
        searchView.setVisibility(visibility);
        chipGroupFilter.setVisibility(visibility);
        bottomNav.setVisibility(visibility);
        fab.setVisibility(visibility);
        recyclerView.setVisibility(visibility);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerView.getVisibility() == View.VISIBLE) {
            loadNotesFromDatabase();
        }
    }

    private void loadNotesFromDatabase() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Note> notes;
            if (isTaskPage) {
                notes = db.noteDao().getAllTasksOnly(); // Pastikan query ini ada di NoteDao
            } else {
                notes = db.noteDao().getAllNotesOnly(); // Pastikan query ini ada di NoteDao
            }
            runOnUiThread(() -> updateUI(notes));
        });
    }

    private void searchNotesFromDatabase(String query) {
        if (query.isEmpty()) {
            loadNotesFromDatabase();
            return;
        }
        String searchQuery = "%" + query + "%";
        Executors.newSingleThreadExecutor().execute(() -> {
            // Note: Untuk pencarian tugas, bisa ditambahkan filter isTask di DAO jika perlu
            List<Note> filteredNotes = db.noteDao().searchNotes(searchQuery);
            runOnUiThread(() -> updateUI(filteredNotes));
        });
    }

    private void filterByCategoryFromDatabase(String category) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Note> notesByCategory = db.noteDao().getNotesByCategory(category);
            runOnUiThread(() -> updateUI(notesByCategory));
        });
    }

    private void updateUI(List<Note> notes) {
        noteList.clear();
        noteList.addAll(notes);
        adapter.notifyDataSetChanged();
        checkEmptyList();
    }

    private void checkEmptyList() {
        if (noteList.isEmpty()) {
            tvEmptyMessage.setText(isTaskPage ? "Belum ada tugas nih" : "Daftar catatan kosong");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}