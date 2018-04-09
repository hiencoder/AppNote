package com.example.gmo.notesapp.view.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gmo.notesapp.R;
import com.example.gmo.notesapp.network.model.Note;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by GMO on 4/9/2018.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    private Context context;
    private List<Note> notes;

    public NoteAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    @Override
    public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list_row, parent, false);
        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteHolder holder, int position) {
        Note note = notes.get(position);
        holder.bindNote(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class NoteHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.note)
        TextView tvNote;

        @BindView(R.id.dot)
        TextView tvDot;

        @BindView(R.id.timestamp)
        TextView tvTimestamp;

        public NoteHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindNote(Note note) {
            tvNote.setText(note.getNote());
            tvDot.setText(Html.fromHtml("&#8226;"));
            tvDot.setTextColor(getRandomMaterialColor("400"));
            tvTimestamp.setText(formatDate(note.getTimestamp()));
        }
    }

    /**
     * @param timestamp
     * @return
     */
    private String formatDate(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(timestamp);
            SimpleDateFormat sdfOutPut = new SimpleDateFormat("MMM d");
            return sdfOutPut.format(date);
        } catch (ParseException ex) {
            Log.d("Error", ex.getMessage());
        }
        return "";
    }

    /**
     * @param color
     * @return lấy về màu ngẫu nhiên được định nghĩa trong array.xml
     */
    private int getRandomMaterialColor(String color) {
        int returnColor = Color.GRAY;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + color,"array",context.getPackageName());

        if (arrayId != 0){
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index,Color.GRAY);
            colors.recycle();/*Để sử dụng cho lần gọi sau*/
        }
        return returnColor;
    }
}
