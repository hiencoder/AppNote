package com.example.gmo.notesapp.view;

import android.content.DialogInterface;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

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
    private void showDialogAddNote(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.note_dialog,null);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setView(view);
        final EditText edNote = view.findViewById(R.id.note);
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

        final AlertDialog alertDialog = dialog.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hiển thị toast khi chưa có text nhập vào
                if (TextUtils.isEmpty(edNote.getText().toString())){
                    Toast.makeText(MainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    //Đóng dialog
                    alertDialog.dismiss();
                }

                /*Check là tạo note mới hay update note*/
                if (shouldUpdate && note != null){
                    //Nếu là update thì update note theo id
                    updateNote(note.getId(),edNote.getText().toString(),position);
                }else {
                    //Thêm một note mới
                    createNote(edNote.getText().toString());
                }
            }
        });
    }

    /**
     * Create new note
     * @param note
     */
    private void createNote(String note) {

    }

    /**
     * Cập nhật note theo id
     * @param id
     * @param note
     * @param position
     */
    private void updateNote(int id, String note, int position) {

    }
}
