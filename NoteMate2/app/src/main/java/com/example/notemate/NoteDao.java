package com.example.notemate;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    // Menyimpan catatan atau tugas baru
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    // Mengambil semua data (Catatan + Tugas)
    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();

    // --- FITUR HALAMAN TERPISAH ---

    // Mengambil Catatan saja (isTask = 0)
    @Query("SELECT * FROM notes WHERE isTask = 0 ORDER BY id DESC")
    List<Note> getAllNotesOnly();

    // Mengambil Daftar Tugas saja (isTask = 1)
    @Query("SELECT * FROM notes WHERE isTask = 1 ORDER BY id DESC")
    List<Note> getAllTasksOnly();

    // --- FITUR PENCARIAN ---
    @Query("SELECT * FROM notes WHERE title LIKE :query OR content LIKE :query ORDER BY id DESC")
    List<Note> searchNotes(String query);

    // --- FITUR FILTER KATEGORI ---
    @Query("SELECT * FROM notes WHERE category = :category ORDER BY id DESC")
    List<Note> getNotesByCategory(String category);

    // --- FITUR TUGAS ---
    // Mengubah status selesai/belum tugas tanpa harus update seluruh objek
    @Query("UPDATE notes SET isCompleted = :completed WHERE id = :noteId")
    void updateTaskStatus(int noteId, boolean completed);

    // Memperbarui data secara keseluruhan
    @Update
    void update(Note note);

    // Menghapus data
    @Delete
    void delete(Note note);

    // Mengambil satu data berdasarkan ID
    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    Note getNoteById(int noteId);
}