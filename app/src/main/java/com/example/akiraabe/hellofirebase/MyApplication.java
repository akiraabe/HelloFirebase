package com.example.akiraabe.hellofirebase;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * AndroidのアクティビティなどからFirebaseを使えるようにします。
 *
 * Created by akiraabe on 16/04/18.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
