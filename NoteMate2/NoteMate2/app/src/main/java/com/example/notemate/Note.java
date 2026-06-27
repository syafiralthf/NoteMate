package com.example.notemate;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// 1. Tambahkan anotasi Entity untuk membuat tabel bernama "notes"
@Entity(tableName = "notes")
public class Note {

    // 2. Tambahkan ID sebagai Primary Key yang bertambah otomatis
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private long timestamp;

    // Constructor
    public Note(String title, String content, long timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    // 3. Getter dan Setter (PENTING: Room butuh setter untuk id)
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
}