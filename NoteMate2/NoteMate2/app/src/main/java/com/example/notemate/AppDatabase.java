package com.example.notemate;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 1. Definisikan Entity (Note) dan Versi Database
@Database(entities = {Note.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    // 2. Hubungkan dengan DAO yang tadi dibuat
    public abstract NoteDao noteDao();

    private static AppDatabase instance;

    // 3. Gunakan Singleton Pattern agar database tidak boros memori
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "notemate_db" // Nama file database di memori HP
                    )
                    .fallbackToDestructiveMigration() // Menghindari crash jika ada perubahan struktur tabel
                    .build();
        }
        return instance;
    }
}