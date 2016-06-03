package com.android.chatbank;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by satishc on 03/06/16.
 */
public class BankServices extends ChatActivity{

    private ListView msgContainer;
    private ChatAdapter cAdapter;
    private Activity context;
    private static final String CPIN = "123456";
   // private EditText cpin_input;

    BankServices(ChatAdapter cAdapter,ListView msgContainer,Activity context){
        this.cAdapter = cAdapter;
        this.msgContainer = msgContainer;
        this.context = context;
    }

    public void billPayment() {

        showConfirmDialogBox("Bill paid successfully!");

    }

    public void recharge(String mobileNo, Double amt) {

        ChatMessage chatResponse = new ChatMessage();
        chatResponse.setId(125);//dummy
        chatResponse.setMessage("Recharge done successfully!");
        chatResponse.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatResponse.setMe(false);

        displayMessage(chatResponse);
    }

    public void recharge(Double amt) {
        ChatMessage chatResponse = new ChatMessage();
        chatResponse.setId(125);//dummy
        chatResponse.setMessage("Recharge done successfully!");
        chatResponse.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatResponse.setMe(false);

        displayMessage(chatResponse);
    }

    public void displayMessage(ChatMessage message) {
        cAdapter.add(message);
        cAdapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        msgContainer.setSelection(msgContainer.getCount() - 1);
    }


    private void showConfirmDialogBox(final String mesg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Transaction ");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_box_prompt, null);

// Set up the input
       // final EditText cpin_input = (EditText) dialog.findViewById(R.id.cpin_pass);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text

        builder.setView(promptsView);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("OK", null);
        //builder.setCancelable(false);
// Set up the buttons
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        EditText cpin_input = (EditText) dialog.findViewById(R.id.cpin_pass);
                        String inputPass = cpin_input.getText().toString();
                        if (inputPass.equals(CPIN)) {
                            dialog.dismiss();
                            ChatMessage chatResponse = new ChatMessage();
                            chatResponse.setId(125);//dummy
                            chatResponse.setMessage(mesg);
                            chatResponse.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                            chatResponse.setMe(false);

                            displayMessage(chatResponse);

                        } else {
                            Toast.makeText(context, "Invalid cPIN !", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
        /*builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputPass = cpin_input.getText().toString();

                if (inputPass.equals(CPIN)) {
                    dialog.dismiss();
                    ChatMessage chatResponse = new ChatMessage();
                    chatResponse.setId(125);//dummy
                    chatResponse.setMessage(mesg);
                    chatResponse.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                    chatResponse.setMe(false);

                    displayMessage(chatResponse);

                }
                else
                {
                    Toast.makeText(context,"Invalid cPIN !",Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });*/


        //builder.show();
    }
    class RechargeAndBill extends AsyncTask<String, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String LOGIN_URL = "http://www.example.com/testPost.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";


        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Attempting login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("name", args[0]);
                params.put("password", args[1]);

                Log.d("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                if (json != null) {
                    Log.d("JSON result", json.toString());

                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONObject json) {

            int success = 0;
            String message = "";

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (json != null) {
                Toast.makeText(context, json.toString(),
                        Toast.LENGTH_LONG).show();

                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Log.d("Success!", message);
            }else{
                Log.d("Failure", message);
            }
        }

    }

}
