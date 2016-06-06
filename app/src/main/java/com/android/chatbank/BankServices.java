package com.android.chatbank;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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

    private BankDbHelper dbHelper;
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
                showConfirmDialogBox("r",amt);


    }

    public void recharge(String amt)  {

                showConfirmDialogBox("r",amt);

    }

    public void transfer(String ben_acc_no, String amt) {
                showConfirmDialogBox("tr",ben_acc_no,amt);
    }

    public void creditCardInfo() {
        showConfirmDialogBox("cri");
    }


    public void chatResponseDisplay(String s, int i,boolean isMe) {
        ChatMessage chatResponse = new ChatMessage();
        chatResponse.setId(i);//dummy
        chatResponse.setMessage(s);
        chatResponse.setDate(sdf.format(new Date()));
        chatResponse.setMe(isMe);

        displayMessageAndInsert(chatResponse);
    }

    public void displayMessageAndInsert(ChatMessage message) {
        cAdapter.add(message);
        cAdapter.notifyDataSetChanged();
        scroll();
        dbHelper = new BankDbHelper(context);
        SQLiteDatabase bankDb = dbHelper.getWritableDatabase();
        long result = dbHelper.addChat(message,bankDb);
        if(result<0)
        {
            Log.d("Error1","Error in inserting..");
        }
        else
        {
            Log.d("Success1","Chat message inserted successfully..");
        }
    }

    private void scroll() {
        msgContainer.setSelection(msgContainer.getCount() - 1);
    }


    private void showConfirmDialogBox(final String... par)
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
                                switch (par[0]) {
                                    case "r":
                                                 int val = Integer.parseInt(par[1]);
                                                 if (checkTransactionValue(val)) {

                                                     new RechargeAndBill().execute(par[1]);
                                                 } else
                                                     chatResponseDisplay("Sorry sir, transaction limit for the day exceeded! Please increase the daily transaction limit in settings.", 125, false);
                                                 break;
                                    case "tr":
                                                int valu = Integer.parseInt(par[2]);
                                                if (checkTransactionValue(valu)) {
                                                    new MoneyTransfer().execute(par[1], par[2]);
                                                } else
                                                    chatResponseDisplay("Sorry sir, transaction limit for the day exceeded! Please increase the daily transaction limit in settings.", 125, false);
                                                break;
                                    case "cri": new CreditCardInfo().execute();
                                                break;
                                }
                                dialog.dismiss();


                            } else {
                                Toast.makeText(context, "Invalid cPIN !", Toast.LENGTH_SHORT).show();
                                //cpinConfirmFlag = false;

                                return;

                            }
                        }
                    });





    }

    void incrementTransactionValue(int value){
        CURRENT_TRANSACTION_VALUE = CURRENT_TRANSACTION_VALUE + value;
        Log.d("t_val", "Value is :" + CURRENT_TRANSACTION_VALUE);
        SharedPreferences trChange = context.getSharedPreferences("TransactionChange", Context.MODE_PRIVATE);
        SharedPreferences.Editor trChangeEitor = trChange.edit();
        trChangeEitor.putInt("CurrentTransactionValue", CURRENT_TRANSACTION_VALUE);
        trChangeEitor.commit();
    }

    boolean checkTransactionValue(int value){
        boolean check = true;
        CURRENT_TRANSACTION_VALUE+=value;
        if(CURRENT_TRANSACTION_VALUE>TRANSACTION_LIMIT ){

            check = false;
        }
        CURRENT_TRANSACTION_VALUE-=value;
        return check;
    }




    class RechargeAndBill extends AsyncTask<String, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String LOGIN_URL = "http://"+LoginActivity.NETWORK_IP_ADDRESS+"/bank_recharge_bill.php";
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
                incrementTransactionValue(Integer.parseInt(amount));

            }else{
                Log.d("Failure", message);
            }
        }

    }
    class MoneyTransfer extends AsyncTask<String, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String LOGIN_URL = "http://"+LoginActivity.NETWORK_IP_ADDRESS+"/bank_money_transfer.php";
        private  String current_balance,amount,ben_acc_no;
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
                params.put("ben_acc_no", args[0]);
                params.put("amt", args[1]);
                amount = args[1];
                ben_acc_no = args[0];
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
                chatResponseDisplay("Rs. "+amount+" transfered to a/c. no.: "+ben_acc_no+" successfully!", 126, false);
                chatResponseDisplay("Dear user, Rs." + amount + " has been debited from your account " + ChatActivity.ACCOUNT_NUMBER + " and your current balance is Rs." + current_balance, 126, false);

            }else{

                Log.d("Failure", message);
                if(message.equals("Insufficient balance !"))
                    chatResponseDisplay("Dear user, your account "+ChatActivity.ACCOUNT_NUMBER+" don't have sufficient amount to be transfered.Sorry the transfer cannot be completed !", 127, false);
                else if(message.equals("Invalid account number !"))
                    chatResponseDisplay("Sorry, the transfer cannot be completed due to invalid  account no.: "+ben_acc_no+". Please check the beneficiary account number !", 128, false);
                else
                    chatResponseDisplay("Sorry, the transfer cannot be completed due to some internal error! Try again later.", 129, false);
            }
        }

    }


    class CreditCardInfo extends AsyncTask<String, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;
        private String credit_card_no,expiry_date,limit,payment_due;
        private static final String LOGIN_URL = "http://"+LoginActivity.NETWORK_IP_ADDRESS+"/bank_card_info.php";
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";


        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Fetching data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("acc_no",ChatActivity.ACCOUNT_NUMBER);

                Log.d("request", "starting");
                Log.d("request1",params.toString());

                JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);

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
                try {
                    credit_card_no= json.getString("credit_card_no");
                    expiry_date = json.getString("expiry_date");
                    limit = json.getString("credit_limit");
                    payment_due = json.getString("paymen_left");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                chatResponseDisplay("Dear user,your Credit Card Details are as follows :\nCredit Card No.: "+credit_card_no+"\nExpiry Date : "+expiry_date+"\nCard Limit : Rs."+limit+"\nPayment Due : Rs."+payment_due, 125, false);

            }else{
                Log.d("Failure", message);
            }
        }

    }

}
