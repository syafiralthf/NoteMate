package com.example.notemate;

import android.graphics.Paint;import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.concurrent.Executors;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public NotesAdapter(List<Note> noteList, OnNoteClickListener listener) {
        this.noteList = noteList;
        this.listener = listener;
    }

    // 1. Menentukan jenis layout berdasarkan status isTask
    @Override
    public int getItemViewType(int position) {
        return noteList.get(position).isTask() ? 1 : 0;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            // Layout untuk halaman TUGAS
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task, parent, false);
        } else {
            // Layout untuk halaman CATATAN
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_note, parent, false);
        }
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);

        if (note.isTask()) {
            // --- LOGIKA UNTUK ITEM TUGAS ---
            holder.titleTextView.setText(note.getTitle());
            holder.tvCategory.setText(note.getCategory());

            if (holder.checkBox != null) {
                holder.checkBox.setChecked(note.isCompleted());

                // Efek coret teks jika tugas selesai
                updateStrokeText(holder.titleTextView, note.isCompleted());

                // Listener klik Checkbox
                holder.checkBox.setOnClickListener(v -> {
                    boolean checked = holder.checkBox.isChecked();
                    note.setCompleted(checked);
                    updateStrokeText(holder.titleTextView, checked);

                    // Simpan perubahan status ke database secara background
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(holder.itemView.getContext())
                                .noteDao().updateTaskStatus(note.getId(), checked);
                    });
                });
            }
        } else {
            // --- LOGIKA UNTUK ITEM CATATAN BIASA ---
            holder.titleTextView.setText(note.getTitle());
            if (holder.contentPreviewTextView != null) {
                holder.contentPreviewTextView.setText(note.getContent());
            }

            String category = note.getCategory();
            holder.tvCategory.setText((category == null || category.isEmpty()) ? "Umum" : category);
        }

        // Klik pada seluruh item untuk Edit (berlaku untuk Catatan & Tugas)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note);
            }
        });
    }

    // Fungsi untuk memberi efek coret pada teks
    private void updateStrokeText(TextView textView, boolean isCompleted) {
        if (isCompleted) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView contentPreviewTextView;
        public TextView tvCategory;
        public CheckBox checkBox;

        public NoteViewHolder(View itemView) {
            super(itemView);
            // ID dari list_item_note.xml
            titleTextView = itemView.findViewById(R.id.note_title);
            contentPreviewTextView = itemView.findViewById(R.id.note_content_preview);
            tvCategory = itemView.findViewById(R.id.tv_item_category);

            // ID tambahan dari item_task.xml (akan null jika sedang di halaman catatan)
            checkBox = itemView.findViewById(R.id.task_checkbox);

            // Fallback ID jika di item_task judulnya menggunakan ID berbeda
            if (titleTextView == null) titleTextView = itemView.findViewById(R.id.task_title);
            if (tvCategory == null) tvCategory = itemView.findViewById(R.id.task_category);
        }
    }
}