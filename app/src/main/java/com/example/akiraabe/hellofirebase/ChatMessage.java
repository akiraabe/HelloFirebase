package com.example.akiraabe.hellofirebase;

import java.io.Serializable;

/**
 * ChatMessageのデータを保持するValueObjectです。
 * Created by akiraabe on 2016/04/27.
 */
public class ChatMessage implements Serializable{
    private String sender;
    private String body;
    private String timestamp;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
