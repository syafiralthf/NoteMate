package com.example.notemate;import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 1. Naikkan Versi ke 3 karena kita menambahkan fitur Tugas (isTask & isCompleted)
@Database(entities = {Note.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // 2. Hubungkan dengan DAO
    public abstract NoteDao noteDao();

    private static AppDatabase instance;

    // 3. Singleton Pattern
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "notemate_db"
                    )
                    /*
                       PENTING: .fallbackToDestructiveMigration() akan menghapus data lama
                       dan membuat ulang tabel dengan struktur baru (kolom isTask & isCompleted).
                       Tanpa menaikkan versi ke 3, baris ini tidak akan berjalan dan aplikasi akan tetap CRASH.
                    */
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}