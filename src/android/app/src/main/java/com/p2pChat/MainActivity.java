package com.p2pChat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.experimental.myapplication.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConnectionListener {
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private Peer peer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);

        ConnectionEventsManager.addReadListener(this);

        setupSendButton();

        startPeer();
    }

    private void setupSendButton() {
        EditText editTextMessage = findViewById(R.id.editTextMessage);
        Button buttonSend = findViewById(R.id.buttonSend);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = editTextMessage.getText().toString();
                if (!messageText.isEmpty()) {
                    try {
                        sendMessage(messageText);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    editTextMessage.setText("");
                }
            }
        });
    }

    private void receiveMessage(String messageText) {
        Message message = new Message(messageText, System.currentTimeMillis());
        messageList.add(message);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void sendMessage(String messageText) throws Exception{
        Message message = new Message(messageText, System.currentTimeMillis());
        messageList.add(message);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        System.out.println("[MainActivity] Sending message");

        ConnectionEventsManager.notifyOnWrite(messageText);
    }

    private void startPeer() {
        new Thread(() -> {
            try {
                ServerConnection serverConnection = new ServerConnection();
                peer = new Peer();
                serverConnection.run();
                peer.run();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public void onNewConnection() throws IOException {

    }

    @Override
    public void onRead(String message) throws IOException {
        System.out.println("[MainActivity] Message received: " + message);
        receiveMessage(message);
    }

    @Override
    public void onWrite(String message) throws IOException {

    }
}
