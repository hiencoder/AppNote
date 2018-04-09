package com.example.gmo.notesapp.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.gmo.notesapp.R;
import com.example.gmo.notesapp.network.ApiService;
import com.example.gmo.notesapp.network.model.Note;
import com.example.gmo.notesapp.view.adapter.NoteAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ApiService apiService;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private NoteAdapter noteAdapter;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.recycler_view)
    RecyclerView rvNotes;

    @BindView(R.id.txt_empty_notes_view)
    TextView tvEmptyNote;

    @BindView(R.id.fab)
    FloatingActionButton fabAddNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.activity_title_home));
        setSupportActionBar(toolbar);

        //Set background cho status bar
        whiteNotificationBar(fabAddNote);

    }

    /**
     * @param fabAddNote
     */
    private void whiteNotificationBar(FloatingActionButton fabAddNote) {

    }

    @OnClick(R.id.fab)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                //Hiển thị dialog add note
                showDialogAddNote(false, null, -1);
                break;
        }
    }

    /**Hiển thị dialog để thêm note
     * @param shouldUpdate
     * @param note
     * @param position
     */
    private void showDialogAddNote(boolean shouldUpdate, Note note, int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.note_dialog,null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setView(view);
        EditText edNote = view.findViewById(R.id.note);
        TextView tvTitle = view.findViewById(R.id.dialog_title);
        tvTitle.setText(!shouldUpdate ? "New Note" : "Edit Note");

        /*Nếu là update note thì hiển thị nội dung note cần update*/
        if (shouldUpdate && note != null){
            edNote.setText(note.getNote());
        }
        dialog.setCancelable(false)
        .setPositiveButton(shouldUpdate ? "Update" : "Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


    }
}
