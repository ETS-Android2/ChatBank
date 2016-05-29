package com.android.chatbank;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    private EditText messageET;
    private ListView messagesContainer;
    private FloatingActionButton fab;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;

    private static final String BOTNAME = "chatbank";
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        initialControls();
    }

    private void initialControls() {
        messagesContainer = (ListView) findViewById(R.id.messageContainer);
        messageET = (EditText) findViewById(R.id.chatMessage);



        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        loadDummyHistory();


        fab = (FloatingActionButton) findViewById(R.id.chatSendButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                chatMessage.setMessage(messageText);
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setMe(true);
                messageET.setText("");

                displayMessage(chatMessage);

                File fileExt = new File(getExternalFilesDir(null).getAbsolutePath()+"/bots");

                if(!fileExt.exists())
                {
                    ZipFileExtraction extract = new ZipFileExtraction();

                    try
                    {
                        extract.unZipIt(getAssets().open("bots.zip"), getExternalFilesDir(null).getAbsolutePath()+"/");
                    } catch (Exception e) { e.printStackTrace(); }
                }

                path = getExternalFilesDir(null).getAbsolutePath();
                Bot bot = new Bot(BOTNAME, path);
                Chat chatSession = new Chat(bot);
                String request = messageText;
                //String request = "What is your name?";
                String response = chatSession.multisentenceRespond(request);

                Log.d("response", response);

                ChatMessage chatResponse = new ChatMessage();
                chatResponse.setId(125);//dummy
                chatResponse.setMessage(response);
                chatResponse.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatResponse.setMe(false);


                displayMessage(chatResponse);

            }
        });


    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void loadDummyHistory(){

        chatHistory = new ArrayList<ChatMessage>();

        ChatMessage msg = new ChatMessage();
        msg.setId(1);
        msg.setMe(false);
        msg.setMessage("Hi");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);
        ChatMessage msg1 = new ChatMessage();
        msg1.setId(2);
        msg1.setMe(false);
        msg1.setMessage("How r u doing???");
        msg1.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg1);

        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);
        Log.d("trial","Trying....");
        for(int i=0; i<chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }
    }

}



