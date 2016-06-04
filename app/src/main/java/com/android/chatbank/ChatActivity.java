package com.android.chatbank;

import android.app.ProgressDialog;
import android.os.AsyncTask;
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
import android.widget.Toast;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class ChatActivity extends AppCompatActivity {
    public static final String ACCOUNT_NUMBER = "ABC123";
    public static final String CPIN = "123456";

    private EditText messageET;
    private ListView messagesContainer;
    private FloatingActionButton fab;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    private Scanner sc ;
    private BankServices bks;
    private static final String BOTNAME = "chatbank";
    private String path,clientResponse,response;

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

                File fileExt = new File(getExternalFilesDir(null).getAbsolutePath() + "/bots");

                if (!fileExt.exists()) {
                    ZipFileExtraction extract = new ZipFileExtraction();

                    try {
                        extract.unZipIt(getAssets().open("bots.zip"), getExternalFilesDir(null).getAbsolutePath() + "/");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                path = getExternalFilesDir(null).getAbsolutePath();
                Bot bot = new Bot(BOTNAME, path);
                Chat chatSession = new Chat(bot);
                String request = messageText;
                //String request = "What is your name?";
                response = chatSession.multisentenceRespond(request);

                Log.d("response", response);

                sc = new Scanner(response);
                sc.useDelimiter("\\|");


                //Response to be given to the client
                clientResponse = sc.next();
                Log.d("res",clientResponse);

                //Remaining part of string to detect the appropriate function
                if(sc.hasNext()) {
                    String functionSelection = sc.next();
                    Log.d("fres", functionSelection);
                    functionCall(functionSelection);
                }

                ChatMessage chatResponse = new ChatMessage();
                chatResponse.setId(125);//dummy
                chatResponse.setMessage(clientResponse);
                chatResponse.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatResponse.setMe(false);


                displayMessage(chatResponse);

            }
        });


    }

    private void functionCall(String functionSelection) {

        String mobileNo,amt;

        sc = new Scanner(functionSelection);
        sc.useDelimiter("\\s");
        String functionPattern = sc.next();//Function Alphabet
        Log.d("f1res", functionPattern);

        bks = new BankServices(adapter,messagesContainer,ChatActivity.this);
        switch(functionPattern)
        {
            case "j": bks.billPayment();
                      break;
            case "k": mobileNo = sc.next();
                      amt = sc.next();
                      bks.recharge(mobileNo, amt);
                      break;
            case "l": amt = sc.next();
                      bks.recharge(amt);
                      break;
        }

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






