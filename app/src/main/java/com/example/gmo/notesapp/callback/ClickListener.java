package com.example.gmo.notesapp.callback;

import android.view.View;

/**
 * Created by GMO on 4/9/2018.
 */

public interface ClickListener {
    void onClick(View view, int position);
    void onLongClick(View view, int position);
}
