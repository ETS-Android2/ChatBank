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
import java.util.concurrent.CountDownLatch;

/**
 * Created by satishc on 03/06/16.
 */
public class BankServices extends ChatActivity{

    private ListView msgContainer;
    private ChatAdapter cAdapter;
    private Activity context;
    private CountDownLatch latch;
    private static final String CPIN = "123456";
   // private EditText cpin_input;
    private boolean cpinConfirmFlag = false,loop=true;

    BankServices(ChatAdapter cAdapter,ListView msgContainer,Activity context){
        this.cAdapter = cAdapter;
        this.msgContainer = msgContainer;
        this.context = context;
    }

    public void billPayment() {

        //showConfirmDialogBox();
       // new RechargeAndBill().execute();
        chatResponseDisplay("Bill paid successfully!", 125, false);


    }



    public void recharge(String mobileNo, String amt) {
            if(mobileNo.length()!=10)
                chatResponseDisplay("I think you entered wrong mobile number! Please check", 124, false);
            else
                showConfirmDialogBox(amt);


    }

    public void recharge(String amt)  {

                showConfirmDialogBox(amt);

    }

    public void chatResponseDisplay(String s, int i,boolean isMe) {
        ChatMessage chatResponse = new ChatMessage();
        chatResponse.setId(i);//dummy
        chatResponse.setMessage(s);
        chatResponse.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatResponse.setMe(isMe);

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


    private void showConfirmDialogBox(final String amt)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Transaction ");
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_box_prompt, null);

        // Set up the input
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text

        builder.setView(promptsView);

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cpinConfirmFlag = false;
                    loop = false;
                    dialog.cancel();
                }
            });

            builder.setPositiveButton("OK", null);
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

                                //cpinConfirmFlag = true;
                                new RechargeAndBill().execute(amt);
                                dialog.dismiss();


                            } else {
                                Toast.makeText(context, "Invalid cPIN !", Toast.LENGTH_SHORT).show();
                                //cpinConfirmFlag = false;

                                return;

                            }
                        }
                    });





    }
    class RechargeAndBill extends AsyncTask<String, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String LOGIN_URL = "http://192.168.0.104/bank_recharge_bill.php";
        private  String current_balance,amount;
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";


        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Attempting transaction...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("acc_no",ChatActivity.ACCOUNT_NUMBER);
                params.put("amt", args[0]);
                amount = args[0];
                Log.d("request", "starting");
                Log.d("request1",params.toString());

                JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);

                if (json != null) {
                    Log.d("JSON result", json.toString());

                    return json;
                }

            } catch (Exception e) {
                Log.d("ee1", "exception error");
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
                //Toast.makeText(context, json.toString(),
                  //      Toast.LENGTH_LONG).show();

                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                    current_balance = json.getString("balance");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                chatResponseDisplay("Recharge done successfully!", 125, false);
                chatResponseDisplay("Dear user, Rs."+amount+" has been debited from your account "+ChatActivity.ACCOUNT_NUMBER+" and your current balance is Rs."+current_balance, 126, false);

            }else{
                Log.d("Failure", message);
            }
        }

    }

}
