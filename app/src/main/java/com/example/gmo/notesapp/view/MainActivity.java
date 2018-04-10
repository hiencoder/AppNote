package com.example.gmo.notesapp.view;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.gmo.notesapp.MyDividerItemDecoration;
import com.example.gmo.notesapp.R;
import com.example.gmo.notesapp.RecyclerTouchListener;
import com.example.gmo.notesapp.callback.ClickListener;
import com.example.gmo.notesapp.network.ApiClient;
import com.example.gmo.notesapp.network.ApiService;
import com.example.gmo.notesapp.network.model.Note;
import com.example.gmo.notesapp.network.model.User;
import com.example.gmo.notesapp.utils.PrefUtils;
import com.example.gmo.notesapp.view.adapter.NoteAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

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

    private List<Note> noteList = new ArrayList<>();

    private CompositeDisposable disposable = new CompositeDisposable();

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

        apiService = ApiClient.getClient(this)
                .create(ApiService.class);

        noteAdapter = new NoteAdapter(this, noteList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvNotes.setLayoutManager(layoutManager);
        rvNotes.setItemAnimator(new DefaultItemAnimator());
        rvNotes.addItemDecoration(new MyDividerItemDecoration(this, LinearLayout.VERTICAL, 16));
        rvNotes.setAdapter(noteAdapter);

        /*Su kien long click khi thi se mo ra 2 option edit va delete*/
        rvNotes.addOnItemTouchListener(new RecyclerTouchListener(this, rvNotes, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                //hien thi dialog de lua chon edit hoac delele note
                showDialogWithAction(position);
            }
        }));

        /*Kiem tra viec luu tru api key trong shared
        * Neu hien tai chua co thi thuc hien viec dang ky cho device
        * Viec nay duoc thuc hien khi app lan dau duoc cai dat hoac data bi xoa
        * boi setting*/
        if (TextUtils.isEmpty(PrefUtils.getApiKey(this))) {
            registerUser();
        } else {
            fetchAllNote();
        }
    }

    /**
     * Lay ve danh sahc note do len recylcerview
     * Nhan ve cac item se duoc sap xep ngau nhien
     * toan tu map() duoc su dung de sap xep cac item theo thu tu giam dan id
     */
    private void fetchAllNote() {
        disposable.add(apiService.fetchAllNotes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<List<Note>, List<Note>>() {
                    @Override
                    public List<Note> apply(List<Note> notes) throws Exception {
                        /*Sap xep cac note theo id giam dan*/
                        Collections.sort(notes, new Comparator<Note>() {
                            @Override
                            public int compare(Note o1, Note o2) {
                                return o1.getId() - o2.getId();
                            }
                        });
                        return notes;
                    }
                })
                .subscribeWith(new DisposableSingleObserver<List<Note>>() {
                    @Override
                    public void onSuccess(List<Note> notes) {
                        /*Danh sach note sau khi sap xep se duoc phat ra
                        * va lang nghe trong subscribe*/
                        noteList.clear();
                        noteList.addAll(notes);
                        noteAdapter.notifyDataSetChanged();

                        toggleEmptyNote();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                        showError(e);
                    }
                }));
    }

    /**
     *
     */
    private void toggleEmptyNote() {

    }

    /**
     * @param e
     */
    private void showError(Throwable e) {

    }

    /**
     * Register new user
     * gui unique id cua device
     */
    private void registerUser() {
        //Get unique id de dinh danh cho device
        final String uniqueId = UUID.randomUUID().toString();
        disposable.add(apiService.register(uniqueId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<User>() {
                    @Override
                    public void onSuccess(User user) {
                /*Neu dang ky thanh cong thi luu vao shared*/
                        PrefUtils.saveApiKey(MainActivity.this, user.getApiKey());
                        Toast.makeText(MainActivity.this,
                                "Thiet bi da duoc dang ky thanh cong voi id: " + uniqueId,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                /*Error*/
                        Log.e(TAG, "onError: " + e.getMessage());
                        showError(e);
                    }
                }));
    }

    /**
     * Hien thi dialog de lua chon edit hoac delete
     *
     * @param position
     */
    private void showDialogWithAction(int position) {

    }

    /**
     * @param view
     */
    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flag = view.getSystemUiVisibility();
            flag |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flag);
            getWindow().setStatusBarColor(Color.WHITE);
        }
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

    /**
     * Hiển thị dialog để thêm note
     *
     * @param shouldUpdate
     * @param note
     * @param position
     */
    private void showDialogAddNote(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.note_dialog, null);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setView(view);
        final EditText edNote = view.findViewById(R.id.note);
        TextView tvTitle = view.findViewById(R.id.dialog_title);
        tvTitle.setText(!shouldUpdate ? "New Note" : "Edit Note");

        /*Nếu là update note thì hiển thị nội dung note cần update*/
        if (shouldUpdate && note != null) {
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
                if (TextUtils.isEmpty(edNote.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //Đóng dialog
                    alertDialog.dismiss();
                }

                /*Check là tạo note mới hay update note*/
                if (shouldUpdate && note != null) {
                    //Nếu là update thì update note theo id
                    updateNote(note.getId(), edNote.getText().toString(), position);
                } else {
                    //Thêm một note mới
                    createNote(edNote.getText().toString());
                }
            }
        });
    }

    /**
     * Create new note
     *
     * @param note
     */
    private void createNote(String note) {
        disposable.add(apiService.createNote(note)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableSingleObserver<Note>() {
            @Override
            public void onSuccess(Note note) {
                if (!TextUtils.isEmpty(note.getError())){
                    Toast.makeText(MainActivity.this,
                            note.getError(), Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "New note created: " + note.getId() + "," + note.getNote()
                + "," + note.getTimestamp());
                //Add vao danh sach va thogn bao cho adapter
                noteList.add(0,note);
                noteAdapter.notifyItemInserted(0);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
                showError(e);
            }
        }));
    }

    /**
     * Cập nhật note theo id
     *
     * @param id
     * @param note
     * @param position
     */
    private void updateNote(int id, String note, int position) {

    }
}
