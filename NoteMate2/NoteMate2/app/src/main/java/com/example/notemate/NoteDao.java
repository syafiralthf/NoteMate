package com.example.notemate;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    // Menyimpan catatan baru
    // OnConflictStrategy.REPLACE berguna untuk menghindari error jika ada ID yang sama
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    // Mengambil semua data.
    // Jika class Note tidak punya field 'timestamp', gunakan 'id DESC' agar catatan terbaru di atas
    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();

    // Memperbarui catatan yang sudah ada (berdasarkan ID)
    @Update
    void update(Note note);

    // Menghapus catatan tertentu
    @Delete
    void delete(Note note);

    // Tambahan: Mengambil satu catatan berdasarkan ID (berguna jika ingin verifikasi data)
    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    Note getNoteById(int noteId);
}