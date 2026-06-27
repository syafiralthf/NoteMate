package com.example.notemate;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// 1. Anotasi Entity untuk membuat tabel bernama "notes"
// PENTING: Jangan taruh @Entity di belakang garis miring komentar (//)
@Entity(tableName = "notes")
public class Note {

    // 2. ID sebagai Primary Key yang bertambah otomatis
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private long timestamp;
    private String category;

    // FIELD BARU UNTUK FITUR TUGAS
    private boolean isTask;      // True jika ini adalah tugas, False jika catatan biasa
    private boolean isCompleted; // True jika tugas sudah selesai dicentang

    // Constructor diperbarui
    public Note(String title, String content, long timestamp, String category, boolean isTask, boolean isCompleted) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.category = category;
        this.isTask = isTask;
        this.isCompleted = isCompleted;
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Getter & Setter untuk Fitur Tugas
    public boolean isTask() {
        return isTask;
    }

    public void setTask(boolean task) {
        isTask = task;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}