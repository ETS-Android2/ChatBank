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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    public void dispAccountStatements(String no,String what)  {

        showConfirmDialogBox("as",no,what);

    }

    public void dispAccountBalance()  {

        showConfirmDialogBox("ab");

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
                                        new RechargeAndBill().execute(par[1]);
                                        break;
                                    case "tr":
                                        new MoneyTransfer().execute(par[1], par[2]);
                                        break;
                                    case "ab":
                                        new AccountBalance().execute();
                                        break;
                                    case "as":
                                        new AccountStatements().execute(par[1],par[2]);
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


    class AccountStatements extends AsyncTask<String, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String LOGIN_URL = "http://192.168.0.104/bank_account_statements.php";
        private  String current_balance,amount;
        private static final String TAG_SUCCESS = "success";
        private int count=0;
        JSONArray myListsAll;
        JSONObject jsonobject;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Fetching");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("acc_no",ChatActivity.ACCOUNT_NUMBER);
                params.put("no_of",args[0]);
                params.put("what",args[1]);
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

            //if (success == 1) {
                String statement="Date\tDescription\t\tRef\t\tWithdrawals\tDeposits\tBalance\n";
try {
    myListsAll = json.getJSONArray("jsonData");
    Log.d("result123",myListsAll.toString());
}
catch (JSONException e){
    e.printStackTrace();
}
                for(int i=0;i<myListsAll.length();i++){
                    try {
                        jsonobject = (JSONObject) myListsAll.get(i);

                        statement=statement.concat(jsonobject.optString("date") + "\t" + jsonobject.optString("description") +
                                "\t\t" + jsonobject.optString("ref") + "\t" + jsonobject.optString("withdrawals") + "\t"
                                + jsonobject.optString("deposits") + "\t" + jsonobject.optString("balance") + "\n");
                    }catch (JSONException e){
                        e.printStackTrace();
                    }


                }

                chatResponseDisplay(statement, 125, false);
            //else{
              //  Log.d("Failure", message);
            //}
        }

    }
    class AccountBalance extends AsyncTask<String, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String LOGIN_URL = "http://192.168.0.104/bank_check_balance.php";
        private  String current_balance;
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";


        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Fetching...");
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
                Log.d("request2","here1");
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
                    Log.d("err1","here2");
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
                String fDate = sdf.format(date);// 12/01/2011 4:48:16 PM
                chatResponseDisplay("Your account balance at "+fDate+" is Rs."+current_balance, 125, false);
            }else{
                Log.d("Failure", message);
            }
        }

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
    class MoneyTransfer extends AsyncTask<String, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String LOGIN_URL = "http://192.168.0.104/bank_money_transfer.php";
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
                chatResponseDisplay("Dear user, Rs."+amount+" has been debited from your account "+ChatActivity.ACCOUNT_NUMBER+" and your current balance is Rs."+current_balance, 126, false);

            }else{
                Log.d("Failure", message);
            }
        }

    }

}
