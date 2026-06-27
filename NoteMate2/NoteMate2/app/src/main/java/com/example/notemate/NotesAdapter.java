package com.example.notemate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private OnNoteClickListener listener; // 1. Tambahkan variabel listener

    // 2. Buat Interface untuk menangani klik dari MainActivity
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    // 3. Update Constructor agar menerima listener
    public NotesAdapter(List<Note> noteList, OnNoteClickListener listener) {
        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Pastikan file layout list_item_note.xml menggunakan background kuning
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.titleTextView.setText(note.getTitle());

        // 4. LOGIKA KLIK: Mengirim data ke listener di MainActivity
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;

        public NoteViewHolder(View itemView) {
            super(itemView);
            // Pastikan ID note_title ada di list_item_note.xml
            titleTextView = itemView.findViewById(R.id.note_title);
        }
    }
}