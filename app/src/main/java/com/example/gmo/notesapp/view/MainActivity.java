package com.example.gmo.notesapp.view;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.HEAD;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ApiService apiService;
    private CompositeDisposable disposable = new CompositeDisposable();

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.recycler_view)
    RecyclerView rvNotes;

    @BindView(R.id.txt_empty_notes_view)
    TextView tvEmptyNote;

    @BindView(R.id.fab)
    FloatingActionButton fabAddNote;

    private List<Note> noteList = new ArrayList<>();

    private NoteAdapter noteAdapter;

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


        apiService = ApiClient.getClient(this).create(ApiService.class);


        noteAdapter = new NoteAdapter(this, noteList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvNotes.setLayoutManager(layoutManager);
        rvNotes.setItemAnimator(new DefaultItemAnimator());
        rvNotes.addItemDecoration(new MyDividerItemDecoration(this, LinearLayout.VERTICAL, 16));
        rvNotes.setAdapter(noteAdapter);

        /*Sự kiện click cho recyclerview*/
        /*Su kien long click khi thi se mo ra 2 option edit va delete*/

        rvNotes.addOnItemTouchListener(new RecyclerTouchListener(this, rvNotes, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                showDialogAction(position);
            }
        }));

        /*Kiểm tra apikey đã lưu vào shared preferences
        * Id của thiết bị sẽ được tạo lần đầu chạy app*/
        if (TextUtils.isEmpty(PrefUtils.getApiKey(this))) {
            registerUser();
        } else {
            //uid đã được tạo fetch all note
            fetchAllNotes();
        }

    }

    /**
     * Lấy ra danh sách note đã tạo,
     * map() sắp xếp lại danh sách note
     */
    private void fetchAllNotes() {
        disposable.add(apiService.fetchAllNotes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<List<Note>, List<Note>>() {
                    @Override
                    public List<Note> apply(List<Note> notes) throws Exception {
                /*Sắp xếp note theo id giảm dần*/
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
                        noteList.clear();
                        //Add notes
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
     * Hiển thị text thông báo note empty
     */
    private void toggleEmptyNote() {
        if (noteList.size() > 0) {
            tvEmptyNote.setVisibility(View.GONE);
        } else {
            tvEmptyNote.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Register user cho device
     */
    private void registerUser() {
        //Unique id định danh cho thiết bị
        final String uId = UUID.randomUUID().toString();
        disposable.add(apiService.register(uId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<User>() {
                    @Override
                    public void onSuccess(User user) {
                        //Lưu vào shared
                        PrefUtils.saveApiKey(MainActivity.this, user.getApiKey());
                        Toast.makeText(MainActivity.this, "Successfully: " + PrefUtils.getApiKey(MainActivity.this), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                        showError(e);
                    }
                }));
    }

    /**
     * Thông báo lỗi trên Snackbar
     *
     * @param e
     */
    private void showError(Throwable e) {
        String message = "";
        try {
            if (e instanceof IOException) {
                message = "No internet connection";
            } else if (e instanceof HttpException) {
                HttpException error = (HttpException) e;
                String errorBody = error.response().errorBody().string();
                JSONObject objError = new JSONObject(errorBody);

                message = objError.getString("error");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        if (TextUtils.isEmpty(message)) {
            message = "Check logcat";
        }

        //Snackbar
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        //Textview cho snackbar
        TextView tvError = sbView.findViewById(android.support.design.R.id.snackbar_text);
        tvError.setText(message);
        tvError.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    /**
     * Hiển thị dialog để chọn lựa cập nhật hay xóa note
     *
     * @param position
     */
    private void showDialogAction(final int position) {
        CharSequence[] actions = new CharSequence[]{"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        /*Hiển thị dialog thông tin note được sửa*/
                        showDialogAddNote(true, noteList.get(position), position);
                        break;
                    case 1:
                        //Delete note
                        deleteNote(noteList.get(position).getId(), position);
                        break;
                }
            }
        });
        builder.show();
    }

    /**
     * Delete note
     *
     * @param id
     * @param position
     */
    private void deleteNote(final int id, final int position) {
        disposable.add(apiService.deleteNote(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: Note detleted " + id);
                        noteList.remove(position);
                        noteAdapter.notifyItemRemoved(position);
                        Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();

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
                        if (!TextUtils.isEmpty(note.getError())) {
                            Toast.makeText(MainActivity.this, note.getError(), Toast.LENGTH_SHORT).show();
                        }

                        Log.d(TAG, "onSuccess: Note created " + note.getId() + "," + note.getNote() + ", " + note.getTimestamp());
                        //Thêm 1 item và thông báo cho adapter
                        noteList.add(0, note);
                        noteAdapter.notifyItemInserted(0);

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
     * Cập nhật note theo id
     *
     * @param id
     * @param note
     * @param position
     */
    private void updateNote(int id, final String note, final int position) {
        disposable.add(apiService.updateNote(id, note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: Note update");
                        //Lấy ra thằng đang được cập nhật
                        Note noteUpdate = noteList.get(position);
                        noteUpdate.setNote(note);

                        /*Cập nhật item và thông báo cho adapter*/
                        noteList.set(position, noteUpdate);
                        noteAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                        showError(e);
                    }
                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
