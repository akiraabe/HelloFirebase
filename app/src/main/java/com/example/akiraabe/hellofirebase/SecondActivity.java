package com.example.akiraabe.hellofirebase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * ChatMessageの詳細表示処理のアクティビティーです。
 */
public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Activityのライフサイクルの理解
        Log.i("LifeCycle", "onCreate");

        // 前頁からのセットされたテキストを表示します。
        showText();

        // 戻るボタンの処理です。
        goBack();
    }

    private void showText() {
        TextView txtViewSender = (TextView) findViewById(R.id.txtViewSender);
        TextView txtViewBody = (TextView) findViewById(R.id.txtViewBody);
        TextView txtViewTimestamp = (TextView) findViewById(R.id.txtViewTimestamp);
        ChatMessage message = (ChatMessage) getIntent().getSerializableExtra("message");
        txtViewSender.setText(message.getSender());
        txtViewBody.setText(message.getBody());
        txtViewTimestamp.setText(message.getTimestamp());
    }

    private void goBack() {
        // backボタンを押下した時の処理。
        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
