package com.experimental.minimalist_p2p;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Declare your RecyclerView and Adapter here
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);

        // Set up the button click listener
        setupSendButton();
    }

    private void setupSendButton() {
        EditText editTextMessage = findViewById(R.id.editTextMessage);
        Button buttonSend = findViewById(R.id.buttonSend);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = editTextMessage.getText().toString();
                if (!messageText.isEmpty()) {
                    sendMessage(messageText); // Call the method to send the message
                    editTextMessage.setText(""); // Clear the input field
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

    private void sendMessage(String messageText) {
        Message message = new Message(messageText, System.currentTimeMillis());
        messageList.add(message); // Add the message to the list
        adapter.notifyItemInserted(messageList.size() - 1); // Notify the adapter
        recyclerView.scrollToPosition(messageList.size() - 1); // Scroll to the new message
    }
}
