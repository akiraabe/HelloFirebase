package com.example.akiraabe.hellofirebase;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * メッセージングアプリのメインアクティビティーです。
 * Firebaseとのやり取りは今のところこのクラスで行っています。
 */
public class MainActivity extends AppCompatActivity {

    private ChatMessageAdaptor adapter;
    private Firebase ref;
    private Query queryRef;
    // 初期表示するメッセージの上限数です。
    private static final int MESSAGE_LIMIT = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Activityのライフサイクルの理解
        Log.i("LifeCycle", "onCreate");

        // Firebaseのノードツリーへの参照を取得します。
        ref = new Firebase(Constant.MY_APP_HOME + "messages");
        queryRef = ref.orderByKey().limitToLast(MESSAGE_LIMIT); // limitは1以上の数値を指定する必要があります。

        // Postボタンを押下した時の処理です。
        findViewById(R.id.postButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postMessage();


            }
        });

        // Clearボタンを押下した時の処理です。
        findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clear();
            }
        });

    }

    private void postMessage() {
        // 画面から入力内容を取得します。
        EditText eTxtName = (EditText) findViewById(R.id.eTxtName);
        EditText eTxtMessage = (EditText) findViewById(R.id.eTxtMessage);
        String name = eTxtName.getText().toString();
        String message = eTxtMessage.getText().toString();

        Log.i("MainActivity", "name : " + name + ", message : " + message);

        // ChatMessageのVo生成します。
        ChatMessage post = new ChatMessage();
        post.setSender(name);
        post.setBody(message);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        post.setTimestamp(sdf.format(new Date()));

        // Firebaseへの書き込みます。
        Firebase newPostRef = ref.push();
        newPostRef.setValue(post);

        // 自動採番のキーの取得と表示をします。
        String postId = newPostRef.getKey();
        Log.i("**** getKey() : ", postId + "***");
    }

    private void clear() {
        EditText eTxtName = (EditText) findViewById(R.id.eTxtName);
        EditText eTxtMessage = (EditText) findViewById(R.id.eTxtMessage);
        eTxtName.setText("");
        eTxtMessage.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("LifeCycle", "onStart");

        // Firebaseから取得したメッセージのリストを表示するためにAdaptorを生成します。
        final ArrayList<ChatMessage> messages = new ArrayList<>();
        adapter = new ChatMessageAdaptor(this, 0, messages);

        // Firebaseにデータが追加された時の処理です。
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousKey) {
                if (previousKey != null) {
                    Log.i("previousKey", previousKey);
                }
                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                Log.i("Firebase(onChildAdded)", String.format("onChildAdded, sender:%s, body:%s",  chatMessage.getSender(), chatMessage.getBody()));
                messages.add(0, chatMessage);

                // 以下のエラー対応のために、notifyDataSetChanged()の呼び出しを追加してみました。
                // The content of the adapter has changed but ListView did not receive a notification.
                // Make sure the content of your adapter is not modified from a background thread, but only　from the UI thread.
                Log.i("MainActivity : ", "notifyDataSetChanged");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                String sender = chatMessage.getSender();
                String body = chatMessage.getBody();
                Log.d("(onChildRemoved)", String.format("onChildRemoved, sender:%s, body:%s", sender, body));

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });

        // ListViewにアダプターを設定します。
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);

        // ListViewをクリックした際の処理です。
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent , View view,int position, long id) {

                Log.i("#onItemClick", "position : " + new Integer(position));
                Class cls = view.getClass();
                Log.i("#onItemClick", "view : " + cls.getName());

                ListView listView = (ListView) parent;
                ChatMessage message = (ChatMessage) listView.getItemAtPosition(position);
                Log.i("#onItemClick", "body : " + message.getBody());
                Log.i("#onItemClick", "sender : " + message.getSender());
                Log.i("#onItemClick", "timestamp : " + message.getTimestamp());

                // 次画面へ遷移します。
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putExtra("message", message);
                startActivity(intent);
            }
        });


    }


    // チャットメッセージのアダプター（ほぼ「おまじない」です）
    public class ChatMessageAdaptor extends ArrayAdapter<ChatMessage> {

        private LayoutInflater layoutInflater;

        public ChatMessageAdaptor(Context c, int id, ArrayList<ChatMessage> messages) {
            super(c, id, messages);
            this.layoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(
                        R.layout.list_item, // レイアウト名はここで指定します。
                        parent,
                        false
                );
            }

            ChatMessage message = (ChatMessage) getItem(position);
//            ((ImageView) convertView.findViewById(R.id.icon))
//                    .setImageBitmap(message.getIcon());
            ((TextView) convertView.findViewById(R.id.sender))
                    .setText(message.getSender());
            ((TextView) convertView.findViewById(R.id.body))
                    .setText(message.getBody());
            return convertView;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i("LifeCycle", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("LifeCycle", "onPause");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.i("LifeCycle", "onRestart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("LifeCycle", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.clear();
        Log.i("LifeCycle", "onDestroy");
    }
}
