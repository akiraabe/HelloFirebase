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
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * メッセージングアプリのメインアクティビティーです。
 * Firebaseとのやり取りは今のところこのクラスで行っています。
 */
public class MainActivity extends AppCompatActivity {

    ChatMessageAdaptor adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Activityのライフサイクルの理解
        Log.i("LifeCycle", "onCreate");

        // Firebaseのノードツリーへの参照を取得します。
        Firebase ref = new Firebase(Constant.MY_APP_HOME + "messages");
        Query queryRef = ref.orderByKey().limitToLast(3); // limitは1以上の数値を指定する必要があります。

        // Firebaseから取得したメッセージのリストを表示するためにAdaptorを生成します。
        final ArrayList<ChatMessage> messages = new ArrayList<>();
        adapter = new ChatMessageAdaptor(this, 0, messages);

        // 以下は一度だけ実行されるイベントです。
        /*
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                Iterator it = snapshot.getChildren().iterator() ;
                while(it.hasNext()){
                    DataSnapshot snap = (DataSnapshot) it.next();
                    ChatMessage chatMessage = snap.getValue(ChatMessage.class);
                    Log.i("SingleValueEvent", String.format("onChildAdded, sender:%s, body:%s",  chatMessage.getSender(), chatMessage.getBody()));
                    messages.add(0, chatMessage);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
        */

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

        // Postボタンを押下した時の処理です。
        findViewById(R.id.postButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 画面からの入力内容を取得
                EditText eTxtName = (EditText) findViewById(R.id.eTxtName);
                EditText eTxtMessage = (EditText) findViewById(R.id.eTxtMessage);
                String name = eTxtName.getText().toString();
                String message = eTxtMessage.getText().toString();

                Log.i("MainActivity", "name : " + name + ", message : " + message);

                // Firebaseへの参照を保持する
                Firebase firebaseRef = new Firebase(Constant.MY_APP_HOME + "messages");

                Map<String, String> post = new HashMap<String, String>();
                post.put("sender", name);
                post.put("body", message);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String timestamp = sdf.format(new Date());
                post.put("timestamp", timestamp);
                Firebase newPostRef = firebaseRef.push();
                newPostRef.setValue(post);

                String postId = newPostRef.getKey();
                Log.i("*** getKey() : ", postId + "***");
            }
        });


        // Clearボタンを押下した時の処理です。
        findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText eTxtName = (EditText) findViewById(R.id.eTxtName);
                EditText eTxtMessage = (EditText) findViewById(R.id.eTxtMessage);
                eTxtName.setText("");
                eTxtMessage.setText("");
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
    public void onStart() {
        super.onStart();
        Log.i("LifeCycle", "onStart");
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
