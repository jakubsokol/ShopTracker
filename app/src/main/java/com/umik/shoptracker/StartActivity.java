package com.umik.shoptracker;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;

public class StartActivity extends Activity {

    private boolean         flag = true;
    private EditText        edit_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        edit_id = (EditText)findViewById(R.id.set_id);
    }

    public void onClickButtonIn(View v) {
        flag = true;
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        String id = edit_id.getText().toString();
        intent.putExtra("id",id);
        intent.putExtra("flag", flag);
        startActivity(intent);
    }

    public void onClickButtonOut(View v) {
        flag = false;
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        String id = edit_id.getText().toString();
        intent.putExtra("id",id);
        intent.putExtra("flag",flag);
        startActivity(intent);
    }
}